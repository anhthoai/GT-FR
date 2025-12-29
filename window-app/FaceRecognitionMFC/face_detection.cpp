
#include "face_detection.h"
#include <iostream>
#include <sys/types.h>
#include <dirent.h>
#include <string.h>
#include <functional>
#include <fstream>
#include <memory>
#include <vector>
#include <utility>
#include <algorithm>
#include <iterator>
#include <map>
#include <inference_engine.hpp>

#include <slog.hpp>
//#include "mkldnn/mkldnn_extension_ptr.hpp"
#include <ext_list.hpp>
#include <opencv2/opencv.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <sys/stat.h>
#include <thread>

using namespace InferenceEngine;
using namespace cv;
using namespace std;
using namespace InferenceEngine::details;

void FaceDetectionClass::initialize(std::string path_to_faceDetection_model, std::string root_dir){
    cout<<endl<<"==============Initialize face_detection network============"<<endl;
    std::string device_for_faceDetection;
    std::vector<std::pair<std::string, std::string>> cmdOptions;
#ifdef GPU
	device_for_faceDetection = "GPU";
#elif CPU
    device_for_faceDetection="CPU";
#endif
    auto deviceName = device_for_faceDetection;

	Core ie;

	std::string gpu_config = root_dir + "/cldnn_global_custom_kernels/cldnn_global_custom_kernels.xml";

#ifdef GPU
	ie.SetConfig({ { PluginConfigParams::KEY_CONFIG_FILE, gpu_config } }, device_for_faceDetection);
#elif CPU
	ie.AddExtension(std::make_shared<Extensions::Cpu::CpuExtensions>(), device_for_faceDetection);
#endif
  
    //Load(FaceDetection).into(plugin);
    this->net= ie.LoadNetwork(this->read(path_to_faceDetection_model), device_for_faceDetection);
    this->ie= &ie;
    std::cout<<"==============successfully loaded face_detection plugin==========="<<std::endl<<endl;;

}
void FaceDetectionClass::submitRequest(){
    if (!enquedFrames) return;
    enquedFrames = 0;
    resultsFetched = false;
    results.clear();
	if (request == nullptr) return;
	request->StartAsync();
}

void FaceDetectionClass::enqueue(const cv::Mat &frame) {
    if (!request) {
        request = net.CreateInferRequestPtr();
    }

    width = frame.cols;
    height = frame.rows;

    auto  inputBlob = request->GetBlob(input);

    matU8ToBlob<uint8_t >(frame, inputBlob);

    enquedFrames = 1;
}

void FaceDetectionClass::fetchResults() {
    results.clear();
    if (resultsFetched) return;
    resultsFetched = true;
    const float *detections = request->GetBlob(output)->buffer().as<float *>();

    for (int i = 0; i < maxProposalCount; i++) {
        float image_id = detections[i * objectSize + 0];
        Result r;
        r.label = static_cast<int>(detections[i * objectSize + 1]);
        r.confidence = detections[i * objectSize + 2];
        r.location.x = detections[i * objectSize + 3] * width;
        r.location.y = detections[i * objectSize + 4] * height;
        r.location.width = detections[i * objectSize + 5] * width - r.location.x;
        r.location.height = detections[i * objectSize + 6] * height - r.location.y;
        if (image_id < 0) {
            break;
        }
        results.push_back(r);
    }
}

InferenceEngine::CNNNetwork FaceDetectionClass::read(std::string path_to_faceDetection_model) {
    std::cout << "Loading network files for Face Detection" << std::endl;
    InferenceEngine::CNNNetReader netReader;
    /** Read network model **/
    netReader.ReadNetwork(path_to_faceDetection_model);
    /** Set batch size to 1 **/
    std::cout << "Batch size is set to  "<< 1 << std::endl;
    netReader.getNetwork().setBatchSize(1);
    /** Extract model name and load it's weights **/
	auto pos = path_to_faceDetection_model.rfind('.');
	std::string binFileName;

	binFileName = path_to_faceDetection_model.substr(0, pos) + ".bin";
	std::cout << "binFileName=" << binFileName << std::endl;
	netReader.ReadWeights(binFileName);

    /** Read labels (if any)**/
    std::string labelFileName = ".labels";

    std::ifstream inputFile(labelFileName);
    std::copy(std::istream_iterator<std::string>(inputFile),
              std::istream_iterator<std::string>(),
              std::back_inserter(labels));
    // -----------------------------------------------------------------------------------------------------

    /** SSD-based network should have one input and one output **/
    // ---------------------------Check inputs ------------------------------------------------------
    std::cout << "Checking Face Detection inputs" << std::endl;
    InferenceEngine::InputsDataMap inputInfo(netReader.getNetwork().getInputsInfo());
    if (inputInfo.size() != 1) {
        throw std::logic_error("Face Detection network should have only one input");
    }
    auto& inputInfoFirst = inputInfo.begin()->second;
    inputInfoFirst->setPrecision(Precision::U8);
    inputInfoFirst->getInputData()->setLayout(Layout::NCHW);
    // -----------------------------------------------------------------------------------------------------

    // ---------------------------Check outputs ------------------------------------------------------
    std::cout << "Checking Face Detection outputs" << std::endl;
    InferenceEngine::OutputsDataMap outputInfo(netReader.getNetwork().getOutputsInfo());
    if (outputInfo.size() != 1) {
        throw std::logic_error("Face Detection network should have only one output");
    }
    auto& _output = outputInfo.begin()->second;
    output = outputInfo.begin()->first;

    const auto outputLayer = netReader.getNetwork().getLayerByName(output.c_str());
    if (outputLayer->type != "DetectionOutput") {
        throw std::logic_error("Face Detection network output layer(" + outputLayer->name +
            ") should be DetectionOutput, but was " +  outputLayer->type);
    }

    if (outputLayer->params.find("num_classes") == outputLayer->params.end()) {
        throw std::logic_error("Face Detection network output layer (" +
            output + ") should have num_classes integer attribute");
    }

    const int num_classes = outputLayer->GetParamAsInt("num_classes");
    if (labels.size() != num_classes) {
        if (labels.size() == (num_classes - 1))  // if network assumes default "background" class, having no label
            labels.insert(labels.begin(), "fake");
        else
            labels.clear();
    }
    const InferenceEngine::SizeVector outputDims = _output->getTensorDesc().getDims();
    maxProposalCount = outputDims[2];
    objectSize = outputDims[3];
    if (objectSize != 7) {
        throw std::logic_error("Face Detection network output layer should have 7 as a last dimension");
    }
    if (outputDims.size() != 4) {
        throw std::logic_error("Face Detection network output dimensions not compatible shoulld be 4, but was " +
                                       std::to_string(outputDims.size()));
    }
    _output->setPrecision(Precision::FP32);
    _output->setLayout(Layout::NCHW);
    input = inputInfo.begin()->first;
    std::cout<<"finished reading network"<<std::endl;
    return netReader.getNetwork();
}


