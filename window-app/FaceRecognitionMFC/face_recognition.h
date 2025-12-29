#include <string>
#include <chrono>
//#include <gflags/gflags.h>
#include <cmath>
#include <inference_engine.hpp>
#include <ie_plugin_cpp.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/video/video.hpp>

#include <ext_list.hpp>
#include <ie_infer_request.hpp>
#include <ie_core.hpp>
#include <ie_cnn_net_reader.h>
#include <ie_plugin_dispatcher.hpp>

//#include <common_ie.hpp>
#include <iomanip>

struct recog_result {
	cv::Rect rectangle;
	float value;
	std::string name;
	std::string emotion;
};

class FaceRecognitionClass
{
 	public:
	 	int initialize(std::string model_path, std::string root_dir);
	 	void load_frame(cv::Mat input_frame);
	 	std::vector<float> do_infer();
		cv::Mat prewhitten(cv::Mat input);
	 	~FaceRecognitionClass();
	private:
 		
 		cv::Mat input_frame; 
 		float * input_buffer;
 		float * output_buffer;
 		cv::Mat* output_frames ;
 		cv::Mat frameInfer;
 		cv::Mat frameInfer_prewhitten;

 		InferenceEngine::CNNNetReader networkReader;
 		InferenceEngine::ExecutableNetwork executable_network;
 		InferenceEngine::Core ie;

 		InferenceEngine::InferRequest::Ptr   async_infer_request;
 		InferenceEngine::InputsDataMap input_info; 
 		InferenceEngine::OutputsDataMap output_info; 
}; 