
// FaceRecognitionMFCDlg.cpp : implementation file
//

#include "stdafx.h"
#include "FaceRecognitionMFC.h"
#include "FaceRecognitionMFCDlg.h"
#include "afxdialogex.h"
#include "face_detection.h"
#include "face_recognition.h"
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
#include <chrono>
#include <map>
#include <inference_engine.hpp>
#include <slog.hpp>
#include <ext_list.hpp>
#include <opencv2/opencv.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "HTTPRequest.hpp"
#include "json.hpp"
#include "RegisterPerson.h"
#include "SettingDlg.h"
#include "LogInDlg.h"

using namespace cv;
using namespace std::chrono;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

std::string gIP1Camera = "", gIP2Camera = "", gIP3Camera = "", gIP4Camera = "", gCameraName1, gCameraName2, gCameraName3, gCameraName4;
VideoCapture gCapture1, gCapture2, gCapture3, gCapture4;
bool run_ip_camera1 = false;
bool run_ip_camera2 = false;
bool run_ip_camera3 = false;
bool run_ip_camera4 = false;

std::string ROOTDIR = "";
// CAboutDlg dialog used for App About

#define RESIZE_WIDTH 300
#define SKIP_NUM 2

int frame_count1 = 0;
int frame_count2 = 0; 
int frame_count3 = 0;
int frame_count4 = 0;

typedef struct _tagFace
{
	cv::Rect rt;
	cv::Rect pos;
	int id;
	int fail_num = 0;
	int show_count = 0;
	std::string access = "Unknown";
	std::string alarm_id = "";
	std::string name = "Unknown";
	std::string gender = "Unknown";
	std::string person_id = "Unknown";
	std::string home_address = "Unknown";
	std::string birthday = "Unknown";
	std::string email = "Unknown";
	std::string phone = "123456789";
	std::string feature = "";
	int score = 0;
	cv::Mat face;
	cv::Mat verify_image;
	cv::Mat full_image;
	int added = 0;
	int removed = 0;
	int recognized = 0;
}Face;

std::vector<Face> face_detected_list1;
std::vector<Face> face_detected_list2;
std::vector<Face> face_detected_list3;
std::vector<Face> face_detected_list4;
std::vector<Face> total_detected_face_list;

std::mutex locker;

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CFaceRecognitionMFCDlg dialog

using namespace InferenceEngine;
using namespace cv;
using namespace std;
using namespace InferenceEngine::details;
using namespace chrono;

FaceDetectionClass FaceDetection1;
FaceDetectionClass FaceDetectionRegister;
FaceDetectionClass FaceDetection2;
FaceDetectionClass FaceDetection3;
FaceDetectionClass FaceDetection4;
FaceRecognitionClass FaceRecognition;
FaceRecognitionClass FaceRecognitionRegister;

cv::Mat mGlob1(620, 480, CV_8UC3);
cv::Mat mGlob2(620, 480, CV_8UC3);
cv::Mat mGlob3(620, 480, CV_8UC3);
cv::Mat mGlob4(620, 480, CV_8UC3);

cv::Mat person_image;
cv::Mat live_frame1, live_frame2, live_frame3, live_frame4;

float thresh = 0.9;

HANDLE live1_Handle;
HANDLE live2_Handle; 

/**returns: the cropped square image of crop_size having same center of rectangle**/
/**style=0 : original face detection results with largest possible square
style>0 : larger square resized **/
Mat get_cropped(Mat input, Rect rectangle, int crop_size, int style) {
	int center_x = rectangle.x + rectangle.width / 2;
	int center_y = rectangle.y + rectangle.height / 2;

	int max_crop_size = min(rectangle.width, rectangle.height);

	int adjusted = max_crop_size * 3 / 2;

	std::vector<int> good_to_crop;
	good_to_crop.push_back(adjusted / 2);
	good_to_crop.push_back(input.size().height - center_y);
	good_to_crop.push_back(input.size().width - center_x);
	good_to_crop.push_back(center_x);
	good_to_crop.push_back(center_y);

	int final_crop = *(min_element(good_to_crop.begin(), good_to_crop.end()));

	Rect pre(center_x - max_crop_size / 2, center_y - max_crop_size / 2, max_crop_size, max_crop_size);
	Rect pre2(center_x - final_crop, center_y - final_crop, final_crop * 2, final_crop * 2);
	Mat r;
	if (style == 0)   r = input(pre);
	else  r = input(pre2);

	resize(r, r, Size(crop_size, crop_size));
	return r;
}


float calculate_dist(std::vector<float> v1, std::vector<float> v2, int len) {
	float sum = 0.0;
	float subtract = 0.0;
	for (int i = 0; i < 512; ++i)
	{
		subtract = v1[i] - v2[i];
		sum = sum + abs(subtract*subtract);
	}
	return sqrt(sum);
}

//requires : data of name-feature set / person to be recognized /threshold 
pair<float, string> recognize(vector<pair<string, vector<float> > > *data, vector <float> target, float threshold) {
	float dist = 3.0;
	float min_value = 3.0;
	string best_match = "Unknown";
	for (auto entry : *(data)) {
		dist = calculate_dist(target, entry.second, 512);
		if (dist < min_value) {
			min_value = dist;
			best_match = entry.first;
		}
	}
	if (min_value > threshold) best_match = "Unknown";
	return pair<float, string>(min_value, best_match);
}

//given: facedetection results that crosses sub-frame boundaries 
//returns: boundary-corssing rectangles from input after enlarged to max. square, then 9x 
vector<Rect> get_boundary_face_results(vector<Rect> *final_faces, vector<Rect> pre_results, int sub_width, int sub_height, int frame_width, int frame_height) {
	vector<Rect> enlarged_face_results;
	vector<Rect> need_redo;
	//deliminate bounds 
	vector<int> horizontal_bound, vertical_bound;
	int x = sub_width;
	int y = sub_height;
	while (x < frame_width)
	{
		horizontal_bound.push_back(x);
		x += sub_width;
	}
	while (y < frame_height) {
		vertical_bound.push_back(y);
		y += sub_height;
	}
	//find the smallest square that emcompasses the rect, and then enlarge 9x
	//disregard out-of bound
	for (auto rr : pre_results) {
		Rect r(rr.x, rr.y, rr.width, rr.height);
		if (r.width > r.height) {
			r.height = r.width;
		}
		else (r.width = r.height);
		//enlarge 3 times
		r.x = r.x - r.width;
		r.y = r.y - r.height;
		r.width = r.width * 3;
		r.height = r.height * 3;
		if (r.x<0) r.x = 0;
		if (r.y<0) r.y = 0;
		if (r.x + r.width > frame_width) {
			r.width = frame_width - r.x;
		}
		if (r.y + r.height > frame_height) {
			r.height = frame_height - r.y;
		}
		//now r is 9x as large
		//enlarged_face_results.push_back(r);
		bool cross_boundary = false;
		for (auto x_lim : horizontal_bound) {
			if ((r.x < x_lim) && (x_lim <r.x + r.width)) {
				cross_boundary = true;
			}
		}

		for (auto y_lim : vertical_bound) {
			if ((r.y < y_lim) && (y_lim<r.y + r.height)) {
				cross_boundary = true;
				//cout<< "rect.y" <<r.y << "y_lim"<<y_lim<<"rect_all"<<rect.y+rect.height<<endl;
			}
		}
		if (cross_boundary) {
			need_redo.push_back(r);
		}
		else {
			bool problem = false;

			if (rr.x + rr.width>frame_width) {
				rr.width = frame_width - rr.x;
				problem = true;
			}
			if (rr.y + rr.height> frame_height) {
				rr.height = frame_height - rr.y;
				problem = true;
			}
			if (rr.x < 0) {
				problem = true;
				rr.x = 0;
			}
			if (rr.y < 0) {
				rr.y = 0;
				problem = true;
			}
			if (problem) { cout << "problem in pre" << endl; }
			(*final_faces).push_back(rr);
		}
	}
	return need_redo;
}

void add_redo_rects_to_final(vector<Rect> *final_faces, vector<Rect> redo_rects, int sub_width, int sub_height, int frame_width, int frame_height) {
	vector<Rect> enlarged_face_results;
	//deliminate bounds 
	vector<int> horizontal_bound, vertical_bound;
	int x = sub_width;
	int y = sub_height;
	while (x < frame_width)
	{
		horizontal_bound.push_back(x);
		x += sub_width;
	}
	while (y < frame_height) {
		vertical_bound.push_back(y);
		y += sub_height;
	}

	for (auto r : redo_rects) {
		bool cross_boundary = false;
		for (auto x_lim : horizontal_bound) {
			if ((r.x < x_lim) && (x_lim <r.x + r.width)) {
				cross_boundary = true;
			}
		}

		for (auto y_lim : vertical_bound) {
			if ((r.y < y_lim) && (y_lim<r.y + r.height)) {
				cross_boundary = true;
			}
		}
		if (cross_boundary) {
			(*final_faces).push_back(r);
		}
		else {

		}
	}
	return;
}


void sanity_check_final_face(vector<Rect> *final_faces, int frame_width, int frame_height) {
	for (auto rect : *final_faces) {
		if (rect.x <0 || rect.y<0 || rect.x + rect.width> frame_width || rect.y + rect.height> frame_height
			)
			cout << "problem! in sanity_check_final_face()" << endl;
	}
	return;
}

template <typename Duration>
std::string get_date_time(tm t, Duration fraction) {
	using namespace std::chrono;
	char datetime[512];
	std::sprintf(datetime, "%04u-%02u-%02u %02u:%02u:%02u.%03u", t.tm_year + 1900,
		t.tm_mon + 1, t.tm_mday, t.tm_hour, t.tm_min, t.tm_sec,
		static_cast<unsigned>(fraction / milliseconds(1)));
	return std::string(datetime);
	// VS2013's library has a bug which may require you to replace
	// "fraction / milliseconds(1)" with
	// "duration_cast<milliseconds>(fraction).count()"
}

static const std::string base64_chars =
"ABCDEFGHIJKLMNOPQRSTUVWXYZ"
"abcdefghijklmnopqrstuvwxyz"
"0123456789+/";


static inline bool is_base64(unsigned char c) {
	return (isalnum(c) || (c == '+') || (c == '/'));
}

std::string base64_encode(unsigned char const* bytes_to_encode, unsigned int in_len) {
	std::string ret;
	int i = 0;
	int j = 0;
	unsigned char char_array_3[3];
	unsigned char char_array_4[4];

	while (in_len--) {
		char_array_3[i++] = *(bytes_to_encode++);
		if (i == 3) {
			char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
			char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
			char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
			char_array_4[3] = char_array_3[2] & 0x3f;

			for (i = 0; (i <4); i++)
				ret += base64_chars[char_array_4[i]];
			i = 0;
		}
	}

	if (i)
	{
		for (j = i; j < 3; j++)
			char_array_3[j] = '\0';

		char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
		char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
		char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);

		for (j = 0; (j < i + 1); j++)
			ret += base64_chars[char_array_4[j]];

		while ((i++ < 3))
			ret += '=';

	}

	return ret;

}

std::string base64_decode(std::string const& encoded_string) {
	int in_len = encoded_string.size();
	int i = 0;
	int j = 0;
	int in_ = 0;
	unsigned char char_array_4[4], char_array_3[3];
	std::string ret;

	while (in_len-- && (encoded_string[in_] != '=') && is_base64(encoded_string[in_])) {
		char_array_4[i++] = encoded_string[in_]; in_++;
		if (i == 4) {
			for (i = 0; i < 4; i++)
				char_array_4[i] = base64_chars.find(char_array_4[i]);

			char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
			char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
			char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];

			for (i = 0; (i < 3); i++)
				ret += char_array_3[i];
			i = 0;
		}
	}

	if (i) {
		for (j = i; j < 4; j++)
			char_array_4[j] = 0;

		for (j = 0; j < 4; j++)
			char_array_4[j] = base64_chars.find(char_array_4[j]);

		char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
		char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
		char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];

		for (j = 0; (j < i - 1); j++) ret += char_array_3[j];
	}

	return ret;
}


//requires: face detection , a frame to be processed 
//return: all faces detected by frame-splitting method. 
//note: a face could be detected multiple times.
CFaceRecognitionMFCDlg::CFaceRecognitionMFCDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_FACERECOGNITIONMFC_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CFaceRecognitionMFCDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIVE1_RUN_BTN, mLive1RunBtn);
	DDX_Control(pDX, IDC_LIVE2_RUN_BTN, mLive2RunBtn);
	DDX_Control(pDX, IDC_LIVE3_RUN_BTN, mLive3RunBtn);
	DDX_Control(pDX, IDC_LIVE4_RUN_BTN, mLive4RunBtn);
	DDX_Control(pDX, IDC_UNRECOGNIZED_FACES, mUnRecognizedFace);
	DDX_Control(pDX, IDC_RECOGNIZED_FACES, mRecognizedFace);
	DDX_Control(pDX, IDC_CLOSE, mCloseBtn);
	DDX_Control(pDX, IDC_SETTING, mSettingBtn);
}

BEGIN_MESSAGE_MAP(CFaceRecognitionMFCDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_LIVE1_RUN_BTN, &CFaceRecognitionMFCDlg::OnBnClickedLive1RunBtn)
	ON_BN_CLICKED(IDC_LIVE2_RUN_BTN, &CFaceRecognitionMFCDlg::OnBnClickedLive2RunBtn)
	ON_BN_CLICKED(IDC_LIVE3_RUN_BTN, &CFaceRecognitionMFCDlg::OnBnClickedLive3RunBtn)
	ON_BN_CLICKED(IDC_LIVE4_RUN_BTN, &CFaceRecognitionMFCDlg::OnBnClickedLive4RunBtn)
	ON_WM_CLOSE()
	ON_WM_ERASEBKGND()
	ON_WM_CTLCOLOR()
	ON_BN_CLICKED(IDC_CLOSE, &CFaceRecognitionMFCDlg::OnBnClickedClose)
	ON_BN_CLICKED(IDC_SETTING, &CFaceRecognitionMFCDlg::OnBnClickedSetting)
END_MESSAGE_MAP()


// CFaceRecognitionMFCDlg message handlers

BOOL CFaceRecognitionMFCDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	LogInDlg dlg;
	dlg.DoModal();
	if (!dlg.getLogIn())
	{
		EndDialog(-1);
		return FALSE;
	}

	CRect rectWorkArea;
	//CRect rectWorkArea(0, 0, 1360, 760);
	SystemParametersInfo(SPI_GETWORKAREA, 0, &rectWorkArea, 0);

	mMemberType = dlg.getMemberType();
	mUserID = dlg.getUserID();
	mGroupToPush = dlg.getGroupToPush();
	mServerURL = dlg.getServerURL();
	mAdminID = dlg.getAdminID();

	// TODO: Add extra initialization here
	if (mMemberType == "admin")
	{
		mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_PLAY),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_PLAY),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_PLAY),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_PLAY),
			IMAGE_BITMAP, 45, 45, LR_COLOR));
	}
	else
	{
		mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_ADD),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_ADD),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_ADD),
			IMAGE_BITMAP, 45, 45, LR_COLOR));

		mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
			MAKEINTRESOURCE(IDB_ADD),
			IMAGE_BITMAP, 45, 45, LR_COLOR));
	}

	mCloseBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
		MAKEINTRESOURCE(IDB_CLOSE),
		IMAGE_BITMAP, 50, 50, LR_COLOR));

	mSettingBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance,
		MAKEINTRESOURCE(IDB_SETTING),
		IMAGE_BITMAP, 50, 50, LR_COLOR));

	UpdateData(FALSE);

	char chRootDir[MAX_PATH];
	GetModuleFileName(NULL, chRootDir, MAX_PATH);
	char *p = strrchr(chRootDir, '\\');
	p++; *p = '\0';
	ROOTDIR = std::string(chRootDir);
	//-----------------------Initialize Detections -----------------------------
	FaceRecognition.initialize(ROOTDIR + "/model/20180402-114759.xml", ROOTDIR);
	FaceRecognitionRegister.initialize(ROOTDIR + "/model/20180402-114759.xml", ROOTDIR);
	FaceDetection1.initialize(ROOTDIR + "/model/face-detection-retail-0004.xml", ROOTDIR);
	FaceDetectionRegister.initialize(ROOTDIR + "/model/face-detection-retail-0004.xml", ROOTDIR);
	FaceDetection2.initialize(ROOTDIR + "/model/face-detection-retail-0004.xml", ROOTDIR);
	FaceDetection3.initialize(ROOTDIR + "/model/face-detection-retail-0004.xml", ROOTDIR);
	FaceDetection4.initialize(ROOTDIR + "/model/face-detection-retail-0004.xml", ROOTDIR);

	mCameraURLSetting.setServerURL(mServerURL);
	mCameraURLSetting.setMemberType(mMemberType);
	mCameraURLSetting.setAdminID(mAdminID);
	mCameraURLSetting.setUserID(mUserID);

	THREADSTRUCT *_param = new THREADSTRUCT;
	_param->_this = this;
	AfxBeginThread(thread_recognition, _param);

	DWORD dwStyle = mUnRecognizedFace.GetExtendedStyle();
	dwStyle |= LVS_EX_CHECKBOXES | LVS_EX_FULLROWSELECT;
	mUnRecognizedFace.SetExtendedStyle(dwStyle);
	mRecognizedFace.SetExtendedStyle(dwStyle);

	LPTSTR lpszHeader[] = { _T("UnKnown Faces"),  NULL };
	for (int i = 0; lpszHeader[i] != NULL; i++) {

		mUnRecognizedFace.InsertColumn(i, lpszHeader[i], LVCFMT_LEFT, 200);
	}

	m_UnRecognizedFaceList.Create(200, 140, ILC_COLOR24 | ILC_MASK, 8, 1);

	LPTSTR lpszHeader1[] = { _T("Recognized Faces"),  NULL };
	for (int i = 0; lpszHeader1[i] != NULL; i++) {

		mRecognizedFace.InsertColumn(i, lpszHeader1[i], LVCFMT_LEFT, 200);
	}

	m_RecognizedFaceList.Create(200, 140, ILC_COLOR24 | ILC_MASK, 8, 1);


	MoveWindow(0, 0, rectWorkArea.Width(), rectWorkArea.Height());
	GetDlgItem(IDC_RECOGNIZED_FACES)->MoveWindow(5, 83, 225, rectWorkArea.Height() - 90);
	GetDlgItem(IDC_UNRECOGNIZED_FACES)->MoveWindow(rectWorkArea.Width() - 235, 83, 225, rectWorkArea.Height() - 90);

	int w = ((rectWorkArea.Width() - 570) / 2) / 4;
	w = w * 4;

	GetDlgItem(IDC_LIVE1)->MoveWindow(235, 83, w, (rectWorkArea.Height() - 90) / 2 - 10);
	GetDlgItem(IDC_LIVE2)->MoveWindow((rectWorkArea.Width() - 570) / 2 + 285, 83, w, (rectWorkArea.Height() - 90) / 2 - 10);

	GetDlgItem(IDC_LIVE3)->MoveWindow(235, (rectWorkArea.Height() - 90) / 2 + 87, w, (rectWorkArea.Height() - 90) / 2 - 10);
	GetDlgItem(IDC_LIVE4)->MoveWindow((rectWorkArea.Width() - 570) / 2 + 285, (rectWorkArea.Height() - 90) / 2 + 87, w, (rectWorkArea.Height() - 90) / 2 - 10);

	GetDlgItem(IDC_CLOSE)->MoveWindow(rectWorkArea.Width() - 60, 10, 50, 50);
	GetDlgItem(IDC_SETTING)->MoveWindow(rectWorkArea.Width() - 120, 10, 50, 50);

	GetDlgItem(IDC_LIVE1_RUN_BTN)->MoveWindow((rectWorkArea.Width() - 570) / 2 + 237, 83, 45, 45);
	GetDlgItem(IDC_LIVE2_RUN_BTN)->MoveWindow((rectWorkArea.Width() - 570) + 287, 83, 45, 45);
	GetDlgItem(IDC_LIVE3_RUN_BTN)->MoveWindow((rectWorkArea.Width() - 570) / 2 + 237, (rectWorkArea.Height() - 90) / 2 + 87, 45, 45);
	GetDlgItem(IDC_LIVE4_RUN_BTN)->MoveWindow((rectWorkArea.Width() - 570) + 287, (rectWorkArea.Height() - 90) / 2 + 87, 45, 45);

	person_image = imread(ROOTDIR + "/res/face.png");
	resize(person_image, person_image, Size(100, 100));

	if (mMemberType != "admin")
	{
		face_detected_list1.clear();

		mCameraURLSetting.ReadCameraURL(1);
		if (mCameraURLSetting.getCameraType() == 0)
		{
			gCapture1.open(mCameraURLSetting.getWebCameraIndex());
		}
		else
		{
			gIP1Camera = mCameraURLSetting.getCameraURL();
			gCapture1.open(gIP1Camera);
		}

		if (!gCapture1.isOpened())
		{
			run_ip_camera1 = false;
		}
		else
		{
			run_ip_camera1 = true;

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func1, _param);
		}

		face_detected_list2.clear();

		mCameraURLSetting.ReadCameraURL(2);
		if (mCameraURLSetting.getCameraType() == 0)
		{
			gCapture2.open(mCameraURLSetting.getWebCameraIndex());
		}
		else
		{
			gIP2Camera = mCameraURLSetting.getCameraURL();
			gCapture2.open(gIP2Camera);
		}

		if (!gCapture2.isOpened())
		{
			run_ip_camera2 = false;
		}
		else
		{
			run_ip_camera2 = true;

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func2, _param);
		}

		face_detected_list3.clear();

		mCameraURLSetting.ReadCameraURL(3);
		if (mCameraURLSetting.getCameraType() == 0)
		{
			gCapture3.open(mCameraURLSetting.getWebCameraIndex());
		}
		else
		{
			gIP3Camera = mCameraURLSetting.getCameraURL();
			gCapture3.open(gIP3Camera);
		}

		if (!gCapture3.isOpened())
		{
			run_ip_camera3 = false;
		}
		else
		{
			run_ip_camera3 = true;

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func3, _param);
		}

		face_detected_list4.clear();

		mCameraURLSetting.ReadCameraURL(4);
		if (mCameraURLSetting.getCameraType() == 0)
		{
			gCapture4.open(mCameraURLSetting.getWebCameraIndex());
		}
		else
		{
			gIP4Camera = mCameraURLSetting.getCameraURL();
			gCapture4.open(gIP4Camera);
		}

		if (!gCapture4.isOpened())
		{
			run_ip_camera4 = false;
		}
		else
		{
			run_ip_camera4 = true;

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func4, _param);
		}

		GetDlgItem(IDC_SETTING)->ShowWindow(SW_HIDE);
	}
	else
	{
		if (mGroupToPush.size() < 1)
		{
			MessageBox("Please set group to push notification in setting!", "Warning!", MB_OK | MB_ICONQUESTION);
		}
	}
	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CFaceRecognitionMFCDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CFaceRecognitionMFCDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CFaceRecognitionMFCDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


int Mat2CImage1(Mat mat, CImage &img) {
	if (mat.empty())
		return -1;
	int nBPP = mat.channels() * 8;
	img.Create(mat.cols, mat.rows, nBPP);
	if (nBPP == 8)
	{
		static RGBQUAD pRGB[256];
		for (int i = 0; i < 256; i++)
			pRGB[i].rgbBlue = pRGB[i].rgbGreen = pRGB[i].rgbRed = i;
		img.SetColorTable(0, 256, pRGB);
	}
	uchar* psrc = mat.data;
	uchar* pdst = (uchar*)img.GetBits();
	int imgPitch = img.GetPitch();
	for (int y = 0; y < mat.rows; y++)
	{
		if (pdst == NULL) continue;
		memcpy(pdst, psrc, mat.cols*mat.channels());//mat.step is incorrect for those images created by roi (sub-images!)
		
		psrc += mat.cols*mat.channels();// a.buf[0];
		pdst += imgPitch;
	}

	return 0;
}

int Mat2CImage2(Mat mat, CImage &img) {
	if (mat.empty())
		return -1;
	int nBPP = mat.channels() * 8;
	img.Create(mat.cols, mat.rows, nBPP);
	if (nBPP == 8)
	{
		static RGBQUAD pRGB[256];
		for (int i = 0; i < 256; i++)
			pRGB[i].rgbBlue = pRGB[i].rgbGreen = pRGB[i].rgbRed = i;
		img.SetColorTable(0, 256, pRGB);
	}
	uchar* psrc = mat.data;
	uchar* pdst = (uchar*)img.GetBits();
	int imgPitch = img.GetPitch();
	for (int y = 0; y < mat.rows; y++)
	{
		memcpy(pdst, psrc, mat.cols*mat.channels());//mat.step is incorrect for those images created by roi (sub-images!)
		psrc += mat.cols*mat.channels();
		pdst += imgPitch;
	}

	return 0;
}

int Mat2CImage3(Mat mat, CImage &img) {
	if (mat.empty())
		return -1;
	int nBPP = mat.channels() * 8;
	img.Create(mat.cols, mat.rows, nBPP);
	if (nBPP == 8)
	{
		static RGBQUAD pRGB[256];
		for (int i = 0; i < 256; i++)
			pRGB[i].rgbBlue = pRGB[i].rgbGreen = pRGB[i].rgbRed = i;
		img.SetColorTable(0, 256, pRGB);
	}
	uchar* psrc = mat.data;
	uchar* pdst = (uchar*)img.GetBits();
	int imgPitch = img.GetPitch();
	for (int y = 0; y < mat.rows; y++)
	{
		memcpy(pdst, psrc, mat.cols*mat.channels());//mat.step is incorrect for those images created by roi (sub-images!)
		psrc += mat.cols*mat.channels();
		pdst += imgPitch;
	}

	return 0;
}


int Mat2CImage4(Mat mat, CImage &img) {
	if ( mat.empty())
		return -1;
	int nBPP = mat.channels() * 8;
	img.Create(mat.cols, mat.rows, nBPP);
	if (nBPP == 8)
	{
		static RGBQUAD pRGB[256];
		for (int i = 0; i < 256; i++)
			pRGB[i].rgbBlue = pRGB[i].rgbGreen = pRGB[i].rgbRed = i;
		img.SetColorTable(0, 256, pRGB);
	}
	uchar* psrc = mat.data;
	uchar* pdst = (uchar*)img.GetBits();
	int imgPitch = img.GetPitch();
	for (int y = 0; y < mat.rows; y++)
	{
		memcpy(pdst, psrc, mat.cols*mat.channels());//mat.step is incorrect for those images created by roi (sub-images!)
		psrc += mat.cols*mat.channels();
		pdst += imgPitch;
	}

	return 0;
}

int Mat2CImageRecog(Mat mat, CImage &img) {
	if (mat.empty())
		return -1;
	int nBPP = mat.channels() * 8;
	img.Create(mat.cols, mat.rows, nBPP);
	if (nBPP == 8)
	{
		static RGBQUAD pRGB[256];
		for (int i = 0; i < 256; i++)
			pRGB[i].rgbBlue = pRGB[i].rgbGreen = pRGB[i].rgbRed = i;
		img.SetColorTable(0, 256, pRGB);
	}
	uchar* psrc = mat.data;
	uchar* pdst = (uchar*)img.GetBits();
	int imgPitch = img.GetPitch();
	for (int y = 0; y < mat.rows; y++)
	{
		if (pdst == NULL) continue;
		memcpy(pdst, psrc, mat.cols*mat.channels());//mat.step is incorrect for those images created by roi (sub-images!)
		psrc += mat.cols*mat.channels();
		pdst += imgPitch;
	}

	return 0;
}
UINT CFaceRecognitionMFCDlg::thread_recognition(LPVOID param)
{
	Mat inputFrame;
	THREADSTRUCT*    ts = (THREADSTRUCT*)param;
	int count = 0;
	int un_count = 0;
	while (1)
	{
		if (total_detected_face_list.size() < 1) continue;
		locker.lock();
		if (total_detected_face_list.size() < 1) continue;
		Face temp = total_detected_face_list[0];
		total_detected_face_list.erase(total_detected_face_list.begin());
		locker.unlock();

		Mat cropped = temp.full_image(temp.pos);

		resize(cropped, cropped, Size(160, 160));
		FaceRecognition.load_frame(cropped);

		vector<float> output_vector;
		output_vector = FaceRecognition.do_infer();
		if (output_vector.size() != 512) continue;
		std::string result_str = "";
		for (int i = 0; i < 512; i++) {
			result_str += std::to_string(output_vector[i]) + " ";
		}

		int match_score = 0;
		std::string tmp_name = "Unknown";
		std::string tmp_sex = "Unknown";
		std::string tmp_birthday = "1900-01-01";
		std::string tmp_home = "Unknown";
		std::string tmp_email = "Unknown";
		std::string tmp_phone = "123456789";
		std::string tmp_country = "Unknown";
		std::string tmp_city = "Unknown";
		std::string tmp_group = "Unknown";
		if (ts->_this == NULL) break;
		std::string server_url = ts->_this->mServerURL + "/PersonSearch";
		std::vector<std::string> group_lists = ts->_this->mGroupToPush;
		try
		{
			// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
			http::Request request(server_url);																		  // pass parameters as a map
			std::map<std::string, std::string> parameters = { { "adminid", ts->_this->mAdminID },{ "query", "Select * from person_info where 1" },{ "feature", result_str }, {"average", "0"} };
			const http::Response response = request.send("POST", parameters, {
				"Content-Type: application/x-www-form-urlencoded"
			});
			std::string resultString = std::string(response.body.begin(), response.body.end());
			json::jobject jsonObject = json::jobject::parse(resultString);

			std::string result = jsonObject["result"];

			

			if (result == "fail" || result == "none")
			{
				CImage image;
				cv::resize(cropped, cropped, Size(100, 100));
				Mat extended_mat = Mat(140, 200, CV_8UC3, cv::Scalar(0, 0, 0));
				cropped.copyTo(Mat(extended_mat, Rect(0, 0, 100, 100)));
				person_image.copyTo(Mat(extended_mat, Rect(100, 0, 100, 100)));
				std::string tmp = "Unknown" ;
				cv::putText(extended_mat, tmp, cv::Point(1, 110), 0, 0.4, cv::Scalar(255, 255, 255));
				cv::putText(extended_mat, temp.access, cv::Point(1, 130), 0, 0.4, cv::Scalar(255, 255, 255));
				/*cv::imwrite(ROOTDIR + "/recog_temp.jpg", extended_mat);
				std::string str = ROOTDIR + "/recog_temp.jpg";
				image.Load(CString(str.c_str()));*/

				Mat2CImageRecog(extended_mat, image);
				CBitmap* pBitmap = CBitmap::FromHandle(image);
				ts->_this->m_UnRecognizedFaceList.Add(pBitmap, 1);
				ts->_this->mUnRecognizedFace.SetImageList(&(ts->_this->m_UnRecognizedFaceList), LVSIL_SMALL);
				un_count++;
				
				ts->_this->mUnRecognizedFace.InsertItem(0, "", ts->_this->mUnRecognizedFace.GetItemCount());

				//save to server database for alarm search
				std::vector<uchar> buf;
				cv::imencode(".jpg", cropped, buf);
				auto *enc_msg = reinterpret_cast<unsigned char*>(buf.data());
				std::string encoded_face_img = base64_encode(enc_msg, buf.size());
				std::vector<uchar> buf1;
				Mat resized;
				resize(temp.full_image, resized, Size(320, 240));
				cv::imencode(".jpg", resized, buf1);
				auto *enc_msg1 = reinterpret_cast<unsigned char*>(buf1.data());
				std::string encoded_full_img = base64_encode(enc_msg1, buf1.size());

				std::vector<uchar> buf2;
				cv::imencode(".jpg", person_image, buf2);
				auto *enc_msg2 = reinterpret_cast<unsigned char*>(buf2.data());
				std::string encoded_verify_img = base64_encode(enc_msg2, buf2.size());

				std::string query = "INSERT INTO alarm_info (alarm_id,access,verify_state, score,name, sex, birthday, home_address, city, country, email, phone, adminid, group_name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				if (ts->_this == NULL) break;
				std::string save_server_url = ts->_this->mServerURL + "/SaveToDatabase";
				try
				{
					http::Request save_request(save_server_url);																		  // pass parameters as a map
					std::map<std::string, std::string> save_parameters;
					if (std::find(group_lists.begin(), group_lists.end(), "Unregistered") != group_lists.end())
					{
						save_parameters = { { "alarm_id", temp.alarm_id },{ "access", temp.access },{ "verify_state", "0" },{ "score", "0" },
						{ "name", tmp_name },{ "sex", tmp_sex },{ "birthday", tmp_birthday },{ "home_address", tmp_home },{ "email", tmp_email },{ "to_push", "1" },
						{ "phone", tmp_phone },{ "query", query },{ "adminid", ts->_this->mAdminID },{ "city", tmp_city },{ "group_name", tmp_group },{ "country", tmp_country },{ "detected", encoded_face_img },{ "recognized", encoded_verify_img },{ "full", encoded_full_img } };
					}
					else
					{
						save_parameters = { { "alarm_id", temp.alarm_id },{ "access", temp.access },{ "verify_state", "0" },{ "score", "0" },
						{ "name", tmp_name },{ "sex", tmp_sex },{ "birthday", tmp_birthday },{ "home_address", tmp_home },{ "email", tmp_email },{ "to_push", "0" },
						{ "phone", tmp_phone },{ "query", query },{ "adminid", ts->_this->mAdminID },{ "city", tmp_city },{ "group_name", tmp_group },{ "country", tmp_country },{ "detected", encoded_face_img },{ "recognized", encoded_verify_img },{ "full", encoded_full_img } };
					}
					
					const http::Response save_response = save_request.send("POST", save_parameters, {
						"Content-Type: application/x-www-form-urlencoded"
					});
					std::string save_result = jsonObject["result"];
					if (save_result == "fail")
					{
						continue;
					}
				}
				catch (const std::exception& e)
				{
					std::cerr << "Request failed, error: " << e.what() << '\n';
					continue;
				}

				continue;
			}
			else
			{
				std::string tmp_name1 = jsonObject["name0"];
				tmp_name = tmp_name1;
				std::string tmp_sex1 = jsonObject["sex0"];
				tmp_sex = tmp_sex1;
				std::string tmp_birthday1 = jsonObject["birth0"];
				tmp_birthday = tmp_birthday1;
				//tmp_birthday = Convert.ToDateTime(tmp_birth_str);
				std::string tmp_home1 = jsonObject["home0"];
				tmp_home = tmp_home1;
				std::string tmp_email1 = jsonObject["email0"];
				tmp_email = tmp_email1;
				std::string res_img_string = jsonObject["imgstr0"];
				string decoded_verify_img = base64_decode(res_img_string);
				vector<uchar> data(decoded_verify_img.begin(), decoded_verify_img.end());
				Mat img = imdecode(data, IMREAD_UNCHANGED);
				
				/*std::string res_fullimg_string = jsonObject["fullimgstr0"];
				std::string decoded_full_img = base64_decode(res_fullimg_string);
				vector<uchar> data1(decoded_full_img.begin(), decoded_full_img.end());
				
				Mat full_img = imdecode(data1, IMREAD_UNCHANGED);*/

				std::string tmp_city1 = jsonObject["city0"];
				tmp_city = tmp_city1;
				std::string tmp_country1 = jsonObject["country0"];
				tmp_country = tmp_country1;

				std::string simscore = jsonObject["simscore0"];
				match_score = (int)(atof(simscore.c_str()));

				std::string tmp_phone1 = jsonObject["phone0"];
				tmp_phone = tmp_phone1;

				std::string tmp_group1 = jsonObject["group_name0"];
				tmp_group = tmp_group1;

				CImage image;
				cv::resize(cropped, cropped, Size(100, 100));
				cv::resize(img, img, Size(100, 100));
				Mat extended_mat = Mat(140, 200, CV_8UC3, cv::Scalar(0, 0, 0));
				cropped.copyTo(Mat(extended_mat, Rect(0, 0, 100, 100)));
				img.copyTo(Mat(extended_mat, Rect(100, 0, 100, 100)));
				std::string tmp = tmp_name + " : " + tmp_group;
				cv::putText(extended_mat, tmp, cv::Point(1, 110), 0, 0.4, cv::Scalar(255, 255, 255));
				cv::putText(extended_mat, temp.access, cv::Point(1, 130), 0, 0.4, cv::Scalar(255, 255, 255));
				Mat2CImageRecog(extended_mat, image);
				/*cv::imwrite(ROOTDIR + "/recog_temp.jpg", extended_mat);
				std::string str = ROOTDIR + "/recog_temp.jpg";
				image.Load(CString(str.c_str()));*/
				CBitmap* pBitmap = CBitmap::FromHandle(image);

				ts->_this->m_RecognizedFaceList.Add(pBitmap, 1);
				ts->_this->mRecognizedFace.SetImageList(&(ts->_this->m_RecognizedFaceList), LVSIL_SMALL);
				count++;

				char result[512];
				sprintf(result, "%s\n%s\n%s", tmp_name.c_str(), temp.access.c_str(), tmp_group.c_str());
				ts->_this->mRecognizedFace.InsertItem(0, "", ts->_this->mRecognizedFace.GetItemCount());

				//save to server database for alarm search
				std::vector<uchar> buf;
				cv::imencode(".jpg", cropped, buf);
				auto *enc_msg = reinterpret_cast<unsigned char*>(buf.data());
				std::string encoded_face_img = base64_encode(enc_msg, buf.size());
				std::vector<uchar> buf1;
				Mat resized;
				resize(temp.full_image, resized, Size(320, 240));
				cv::imencode(".jpg", resized, buf1);
				auto *enc_msg1 = reinterpret_cast<unsigned char*>(buf1.data());
				std::string encoded_full_img = base64_encode(enc_msg1, buf1.size());
				
				std::string query = "INSERT INTO alarm_info (alarm_id,access,verify_state, score,name, sex, birthday, home_address, city, country, email, phone, adminid, group_name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
				std::string save_server_url = ts->_this->mServerURL + "/SaveToDatabase";
				try
				{
					http::Request save_request(save_server_url);																		  // pass parameters as a map
					std::map<std::string, std::string> save_parameters;
					if (std::find(group_lists.begin(), group_lists.end(), tmp_group) != group_lists.end())
					{
						save_parameters = { { "alarm_id", temp.alarm_id },{ "access", temp.access },{ "verify_state", "1" },{ "score", to_string(match_score) },
						{ "name", tmp_name },{ "sex", tmp_sex },{ "birthday", tmp_birthday },{ "home_address", tmp_home },{ "email", tmp_email },{ "to_push", "1" },
						{ "phone", tmp_phone },{ "query", query },{ "adminid", ts->_this->mAdminID },{ "city", tmp_city },{ "group_name", tmp_group },{ "country", tmp_country },{ "detected", encoded_face_img },{ "recognized", res_img_string },{ "full", encoded_full_img } };
					}
					else
					{
						save_parameters = { { "alarm_id", temp.alarm_id },{ "access", temp.access },{ "verify_state", "1" },{ "score", to_string(match_score) },
						{ "name", tmp_name },{ "sex", tmp_sex },{ "birthday", tmp_birthday },{ "home_address", tmp_home },{ "email", tmp_email },{ "to_push", "0" },
						{ "phone", tmp_phone },{ "query", query },{ "adminid", ts->_this->mAdminID },{ "city", tmp_city },{ "group_name", tmp_group },{ "country", tmp_country },{ "detected", encoded_face_img },{ "recognized", res_img_string },{ "full", encoded_full_img } };
					}
					const http::Response save_response = save_request.send("POST", save_parameters, {
						"Content-Type: application/x-www-form-urlencoded"
					});
					std::string save_result = jsonObject["result"];
					if (save_result == "fail")
					{
						continue;
					}
				}
				catch (const std::exception& e)
				{
					std::cerr << "Request failed, error: " << e.what() << '\n';
					continue;
				}
			}
		}
		catch (const std::exception& e)
		{
			std::cerr << "Request failed, error: " << e.what() << '\n';
			continue;
		}

	}
	return 0;
}

int Bpp(cv::Mat img) { return 8 * img.channels(); }
void FillBitmapInfo(BITMAPINFO* bmi, int width, int height, int bpp, int origin)
{
	assert(bmi && width >= 0 && height >= 0 && (bpp == 8 || bpp == 24 || bpp == 32));

	BITMAPINFOHEADER* bmih = &(bmi->bmiHeader);

	memset(bmih, 0, sizeof(*bmih));
	bmih->biSize = sizeof(BITMAPINFOHEADER);
	bmih->biWidth = width;
	bmih->biHeight = origin ? abs(height) : -abs(height);
	bmih->biPlanes = 1;
	bmih->biBitCount = (unsigned short)bpp;
	bmih->biCompression = BI_RGB;

	if (bpp == 8)
	{
		RGBQUAD* palette = bmi->bmiColors;

		for (int i = 0; i < 256; i++)
		{
			palette[i].rgbBlue = palette[i].rgbGreen = palette[i].rgbRed = (BYTE)i;
			palette[i].rgbReserved = 0;
		}
	}
}

UINT CFaceRecognitionMFCDlg::thread_func1(LPVOID param)
{
	Mat inputFrame;
	THREADSTRUCT*    ts = (THREADSTRUCT*)param;
	frame_count1 = 0;
	CDC *pDC = ts->_this->GetDlgItem(IDC_LIVE1)->GetDC();
	HDC hDC = pDC->GetSafeHdc();
	CRect rect;
	ts->_this->GetDlgItem(IDC_LIVE1)->GetClientRect(&rect);

	Face temp;

	while (run_ip_camera1)
	{
		locker.lock();
		gCapture1.read(live_frame1);
		live_frame1.copyTo(inputFrame);
		locker.unlock();
		if (inputFrame.cols == 0 || inputFrame.rows == 0)
		{
			/*run_ip_camera1 = false;
			gCapture1.release();
			if (ts->_this->mMemberType == "admin")
			{
				ts->_this->mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 50, 50, LR_COLOR));
				break;
			}*/
			gCapture1.open(gIP1Camera);
			continue;
		}
		if (ts->_this == NULL) break;
		frame_count1++;
		if (frame_count1 % SKIP_NUM != 0)
		{
			if (frame_count1 > 120) frame_count1 = 0;

			int rw = rect.right - rect.left;     //the width of your picture control
			int rh = rect.bottom - rect.top;
			if (!inputFrame.data) {
				ts->_this->MessageBox(_T("read picture fail！"));
				return -1;
			}
			cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));

			//CImage image;
			////image.Load(CString(str.c_str()));
			//Mat2CImage1(inputFrame, image);
			//image.Draw(hDC, 0, 0, rw, rh);

			try
			{
				int height = inputFrame.rows;
				int width = inputFrame.cols;
				uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
				BITMAPINFO* bmi = (BITMAPINFO*)buffer;
				FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
				SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
					height, 0, 0, 0, height, inputFrame.data, bmi,
					DIB_RGB_COLORS);
			}
			catch (Exception e)
			{
				cout << e.what();
				continue;
			}
			//imshow("dfdf", inputFrame);
			waitKey(1);
			continue;
		}
		Mat frame;
		inputFrame.copyTo(frame);
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));


		//get all available faces for other detections
		//		vector<Rect> final_faces = get_final_faces();
		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetection1.enqueue(sub_frame);
			FaceDetection1.submitRequest();
			FaceDetection1.wait();
			FaceDetection1.fetchResults();

			for (auto r : FaceDetection1.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame;
		frame.copyTo(boundary_face_frame);
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src;
				frame(src_rect).copyTo(src);
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetection1.enqueue(boundary_face_frame);
		FaceDetection1.submitRequest();
		FaceDetection1.wait();
		FaceDetection1.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetection1.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}
		
		vector<Rect> detected_list;
		detected_list.clear();
		Mat org;
		inputFrame.copyTo(org);
		for (auto rect : final_faces) {
			//Mat cropped = get_cropped(frame, rect, 160, 2);
			//FaceRecognition.load_frame(cropped);
			//output_vector = FaceRecognition.do_infer();

			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			int center_x = face.x + face.width / 2;
			int center_y = face.y + face.height / 2;

			int max_crop_size = min(face.width, face.height);

			int adjusted = max_crop_size * 3 / 2;

			std::vector<int> good_to_crop;
			good_to_crop.push_back(adjusted / 2);
			good_to_crop.push_back(org.size().height - center_y);
			good_to_crop.push_back(org.size().width - center_x);
			good_to_crop.push_back(center_x);
			good_to_crop.push_back(center_y);

			int final_crop = *(min_element(good_to_crop.begin(), good_to_crop.end()));

			Rect pre(center_x - max_crop_size / 2, center_y - max_crop_size / 2, max_crop_size, max_crop_size);
			Rect pre2(center_x - final_crop, center_y - final_crop, final_crop * 2, final_crop * 2);
			detected_list.push_back(pre2);
		}
		if (detected_list.size() > 0)
		{
			if (face_detected_list1.size() < 1)
			{
				for (int i = 0; i < detected_list.size(); i++)
				{
					temp.rt = detected_list[i];
					temp.id = i;

					org.copyTo(temp.full_image);
					Mat face_mat = Mat(org, detected_list[i]);
					face_mat.copyTo(temp.face);

					system_clock::time_point now = system_clock::now();
					system_clock::duration tp = now.time_since_epoch();

					tp -= duration_cast<seconds>(tp);

					time_t tt = system_clock::to_time_t(now);
					std::string date_time = get_date_time(*localtime(&tt), tp);
					temp.access = date_time;
					temp.alarm_id = (gCameraName1 == "" ? "cam1" : gCameraName1) + "-" + to_string(temp.id);
					face_detected_list1.push_back(temp);

				}
			}
			else
			{
				for (int i = 0; i < face_detected_list1.size(); i++)
				{
					if (face_detected_list1[i].removed == 1) continue;
					int matched = -1;
					for (int j = 0; j < detected_list.size(); j++)
					{
						Rect intersect = detected_list[j] & face_detected_list1[i].rt;
						int area1 = detected_list[j].width * detected_list[j].height;
						int area2 = face_detected_list1[i].rt.width * face_detected_list1[i].rt.height;
						int max_area = (area1 > area2 ? area1 : area2);
						int area = intersect.width * intersect.height;
						if (area > (int)(max_area * 0.2f))
						{
							matched = j;
							break;
						}
					}
					if (matched >= 0)
					{

						face_detected_list1[i].rt = detected_list[matched];
						if (face_detected_list1[i].added == 0)
						{
							face_detected_list1[i].pos = detected_list[matched];
							org.copyTo(face_detected_list1[i].full_image);
							Mat face_mat = Mat(org, detected_list[matched]);
							face_mat.copyTo(face_detected_list1[i].face);
						}
						int a = face_detected_list1[i].show_count;
						a = a + 1;
						face_detected_list1[i].show_count = a;
					}
					else
					{
						int a = face_detected_list1[i].fail_num;
						a = a + 1;
						face_detected_list1[i].fail_num = a;
					}
				}

				for (int i = 0; i < detected_list.size(); i++)
				{
					int matched = -1;
					for (int j = 0; j < face_detected_list1.size(); j++)
					{
						if (face_detected_list1[j].removed == 1) continue;
						Rect intersect = detected_list[i] & face_detected_list1[j].rt;
						int area1 = detected_list[i].width * detected_list[i].height;
						int area2 = face_detected_list1[j].rt.width * face_detected_list1[j].rt.height;
						int max_area = (area1 > area2 ? area1 : area2);
						int area = intersect.width * intersect.height;
						if (area > (int)(max_area * 0.2f))
						{
							matched = j;
							break;
						}
					}
					if (matched < 0)
					{
						temp.id = face_detected_list1.size();
						temp.rt = detected_list[i];

						org.copyTo(temp.full_image);
						Mat face_mat = Mat(org, detected_list[i]);
						face_mat.copyTo(temp.face);

						system_clock::time_point now = system_clock::now();
						system_clock::duration tp = now.time_since_epoch();

						tp -= duration_cast<seconds>(tp);

						time_t tt = system_clock::to_time_t(now);
						std::string date_time = get_date_time(*localtime(&tt), tp);
						temp.access = date_time;
						temp.alarm_id = (gCameraName1 == "" ? "cam1" : gCameraName1) + "-" + to_string(temp.id);
						face_detected_list1.push_back(temp);
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < face_detected_list1.size(); i++)
			{
				if (face_detected_list1[i].removed == 1) continue;
				face_detected_list1[i].fail_num++;
			}
		}

		for (int i = 0; i < face_detected_list1.size(); i++)
		{
			if (face_detected_list1[i].removed == 1) continue;
			if (face_detected_list1[i].fail_num >(60 / SKIP_NUM))
			{
				face_detected_list1[i].removed = 1;
			}
		}
		

		for (int i = 0; i < face_detected_list1.size(); i++)
		{
			if (face_detected_list1[i].removed == 1) continue;
			if (face_detected_list1[i].show_count < (10  / SKIP_NUM)) continue;
			
			rectangle(inputFrame, face_detected_list1[i].rt, Scalar(0, 0, 255), 2);
			//putText(inputFrame, to_string(face_detected_list1[i].id), Point(face_detected_list1[i].rt.x, face_detected_list1[i].rt.y - 10), 0, 2, cv::Scalar(0, 0, 255), 2);
			if (face_detected_list1[i].added == 1) continue;
			locker.lock();

			Face temp1;
			temp1.access = face_detected_list1[i].access;
			temp1.alarm_id = face_detected_list1[i].alarm_id;
			temp1.id = face_detected_list1[i].id;
			temp1.full_image = face_detected_list1[i].full_image.clone();
			temp1.pos = face_detected_list1[i].pos;
			face_detected_list1[i].face.copyTo(temp1.face);
			total_detected_face_list.push_back(temp1);
			locker.unlock();
			face_detected_list1[i].added = 1;
		}

		int rw = rect.right - rect.left;     //the width of your picture control
		int rh = rect.bottom - rect.top;
		if (!inputFrame.data) {
			ts->_this->MessageBox(_T("read picture fail！"));
			return -1;
		}
		cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
		/*cv::imwrite(ROOTDIR + "/show_temp.jpg", inputFrame);
		std::string str = ROOTDIR + "/show_temp.jpg";*/
		//CImage image;
		////image.Load(CString(str.c_str()));
		//Mat2CImage1(inputFrame, image);
		//image.Draw(hDC, 0, 0, rw, rh);
		try
		{
			int height = inputFrame.rows;
			int width = inputFrame.cols;
			uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
			BITMAPINFO* bmi = (BITMAPINFO*)buffer;
			FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
			SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
				height, 0, 0, 0, height, inputFrame.data, bmi,
				DIB_RGB_COLORS);
		}
		catch (Exception e)
		{
			cout << e.what();
			continue;
		}
		//imshow("dfdf", inputFrame);
		waitKey(1);
	}
	return 0;
}

void CFaceRecognitionMFCDlg::OnBnClickedLive1RunBtn()
{
	// TODO: Add your control notification handler code here
	if (mMemberType == "admin")
	{
		face_detected_list1.clear();
		if (!run_ip_camera1)
		{
			mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PAUSE),
				IMAGE_BITMAP, 45, 45, LR_COLOR));

			if (mMemberType == "admin")
			{
				mCameraURLSetting.setCameraIndex(1);
				int nRet = mCameraURLSetting.DoModal();
				if (nRet == IDOK)
				{
					if (mCameraURLSetting.getCameraType() == 0) //web camera
					{
						//save camera url to server
						std::string server_url = mServerURL + "/SaveCameraUrl";
						try
						{
							// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
							http::Request request(server_url);																		  // pass parameters as a map
							std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
							{ "camera_name", mCameraURLSetting.getCameraName() },
							{ "camera_url", to_string(mCameraURLSetting.getWebCameraIndex()) },{ "camera_id", "1" } };
							const http::Response response = request.send("POST", parameters, {
								"Content-Type: application/x-www-form-urlencoded"
							});
							std::string resultString = std::string(response.body.begin(), response.body.end());
							json::jobject jsonObject = json::jobject::parse(resultString);

							std::string result = jsonObject["result"];

							if (result == "fail")
							{
								MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
								mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
									IMAGE_BITMAP, 45, 45, LR_COLOR));
								return;
							}
							else
							{
								MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
							}
						}
						catch (const std::exception& e)
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						gCapture1.open(mCameraURLSetting.getWebCameraIndex());
					}
					else if (mCameraURLSetting.getCameraType() == 1) //ip camera or video
					{
						//save camera url to server
						std::string server_url = mServerURL + "/SaveCameraUrl";
						try
						{
							// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
							http::Request request(server_url);																		  // pass parameters as a map
							std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
							{ "camera_name", mCameraURLSetting.getCameraName() },
							{ "camera_url", mCameraURLSetting.getCameraURL() },{ "camera_id", "1" } };
							const http::Response response = request.send("POST", parameters, {
								"Content-Type: application/x-www-form-urlencoded"
							});
							std::string resultString = std::string(response.body.begin(), response.body.end());
							json::jobject jsonObject = json::jobject::parse(resultString);

							std::string result = jsonObject["result"];

							if (result == "fail")
							{
								MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
								mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
									IMAGE_BITMAP, 45, 45, LR_COLOR));
								return;
							}
							else
							{
								MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
							}
						}
						catch (const std::exception& e)
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						gIP1Camera = mCameraURLSetting.getCameraURL();
						gCapture1.open(gIP1Camera);
						gCameraName1 = mCameraURLSetting.getCameraName();
					}
				}
				else
				{
					mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
						IMAGE_BITMAP, 45, 45, LR_COLOR));
					return;
				}
			}
			else
			{
				mCameraURLSetting.ReadCameraURL(1);
				if (mCameraURLSetting.getCameraType() == 0)
				{
					gCapture1.open(mCameraURLSetting.getWebCameraIndex());
				}
				else
				{
					gIP1Camera = mCameraURLSetting.getCameraURL();
					if (gIP1Camera == "")
					{
						mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gCapture1.open(gIP1Camera);
				}

				gCameraName1 = mCameraURLSetting.getCameraName();
			}


			if (!gCapture1.isOpened())
			{
				run_ip_camera1 = false;
				gCapture1.release();
				mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				return;
			}

			run_ip_camera1 = true;
			// 		DWORD tid;
			// 		live1_Handle = CreateThread(NULL, 0, thread_func1, 0, 0, &tid);

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func1, _param);
		}
		else
		{
			//gCapture1.release();
			run_ip_camera1 = false;
			mLive1RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			return;
		}
	}
	else
	{
		if (!run_ip_camera1) return;
		Mat register_image;
		locker.lock();
		live_frame1.copyTo(register_image);
		locker.unlock();

		Mat frame;
		register_image.copyTo(frame);
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));

		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetectionRegister.enqueue(sub_frame);
			FaceDetectionRegister.submitRequest();
			FaceDetectionRegister.wait();
			FaceDetectionRegister.fetchResults();

			for (auto r : FaceDetectionRegister.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame;
		frame.copyTo(boundary_face_frame);
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src;
				frame(src_rect).copyTo(src);
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetectionRegister.enqueue(boundary_face_frame);
		FaceDetectionRegister.submitRequest();
		FaceDetectionRegister.wait();
		FaceDetectionRegister.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetectionRegister.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		vector<Rect> detected_list;
		detected_list.clear();
		Mat org;
		register_image.copyTo(org);
		for (auto rect : final_faces) {
			//Mat cropped = get_cropped(frame, rect, 160, 2);
			//FaceRecognition.load_frame(cropped);
			//output_vector = FaceRecognition.do_infer();

			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			detected_list.push_back(face);
		}

		if (detected_list.size() < 1)
		{
			MessageBox("Can't detect face!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		else if (detected_list.size() > 1)
		{
			MessageBox("There are multiple faces!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}

		Mat cropped = get_cropped(register_image, detected_list[0], 160, 2);
		FaceRecognitionRegister.load_frame(cropped);

		vector<float> output_vector;
		output_vector = FaceRecognitionRegister.do_infer();
		if (output_vector.size() != 512)
		{
			MessageBox("Can't extract features!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::string result_str = "";
		for (int i = 0; i < 512; i++) {
			result_str += std::to_string(output_vector[i]) + " ";
		}

		CRegisterPerson registerDlg;
		registerDlg.setAdminID(mAdminID);
		registerDlg.setServerURL(mServerURL);
		registerDlg.setUserID(mUserID);
		registerDlg.setImage(cropped);
		registerDlg.setRegion(detected_list[0]);
		registerDlg.setFeature(result_str);
		registerDlg.DoModal();
	}
}


UINT CFaceRecognitionMFCDlg::thread_func2(LPVOID param)
{
	Mat inputFrame;
	THREADSTRUCT*    ts = (THREADSTRUCT*)param;
	frame_count2 = 0;

	CDC *pDC = ts->_this->GetDlgItem(IDC_LIVE2)->GetDC();
	HDC hDC = pDC->GetSafeHdc();
	CRect rect;
	ts->_this->GetDlgItem(IDC_LIVE2)->GetClientRect(&rect);

	Face temp;

	while (run_ip_camera2)
	{
		locker.lock();
		gCapture2.read(live_frame2);
		live_frame2.copyTo(inputFrame);
		locker.unlock();
		if (inputFrame.cols == 0 || inputFrame.rows == 0)
		{
			run_ip_camera2 = false;
			gCapture2.release();
			if (ts->_this->mMemberType == "admin")
			{
				ts->_this->mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
			}
			break;
		}
		if (ts->_this == NULL) break;
		frame_count2++;
		if (frame_count2 % SKIP_NUM != 0)
		{
			if (frame_count2 > 120) frame_count2 = 0;
			
			int rw = rect.right - rect.left;     //the width of your picture control
			int rh = rect.bottom - rect.top;
			if (!inputFrame.data) {
				ts->_this->MessageBox(_T("read picture fail！"));
				return -1;
			}
			cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
			try
			{
				int height = inputFrame.rows;
				int width = inputFrame.cols;
				uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
				BITMAPINFO* bmi = (BITMAPINFO*)buffer;
				FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
				SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
					height, 0, 0, 0, height, inputFrame.data, bmi,
					DIB_RGB_COLORS);
			}
			catch (Exception e)
			{
				cout << e.what();
				continue;
			}
			continue;
		}

		mGlob2 = inputFrame.clone();
		Mat frame = mGlob2.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));


		//get all available faces for other detections
		//		vector<Rect> final_faces = get_final_faces();
		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetection2.enqueue(sub_frame);
			FaceDetection2.submitRequest();
			FaceDetection2.wait();
			FaceDetection2.fetchResults();

			for (auto r : FaceDetection2.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetection2.enqueue(boundary_face_frame);
		FaceDetection2.submitRequest();
		FaceDetection2.wait();
		FaceDetection2.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetection2.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}
		Mat org;
		inputFrame.copyTo(org);
		vector<Rect> detected_list;
		for (auto rect : final_faces) {
			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			int center_x = face.x + face.width / 2;
			int center_y = face.y + face.height / 2;

			int max_crop_size = min(face.width, face.height);

			int adjusted = max_crop_size * 3 / 2;

			std::vector<int> good_to_crop;
			good_to_crop.push_back(adjusted / 2);
			good_to_crop.push_back(org.size().height - center_y);
			good_to_crop.push_back(org.size().width - center_x);
			good_to_crop.push_back(center_x);
			good_to_crop.push_back(center_y);

			int final_crop = *(min_element(good_to_crop.begin(), good_to_crop.end()));

			Rect pre(center_x - max_crop_size / 2, center_y - max_crop_size / 2, max_crop_size, max_crop_size);
			Rect pre2(center_x - final_crop, center_y - final_crop, final_crop * 2, final_crop * 2);
			detected_list.push_back(pre2);
			//rectangle(inputFrame, face, Scalar(0, 0, 255), 2);
		}

		if (face_detected_list2.size() < 1)
		{
			for (int i = 0; i < detected_list.size(); i++)
			{
				temp.rt = detected_list[i];
				temp.id = i;

				org.copyTo(temp.full_image);
				Mat face_mat = Mat(org, detected_list[i]);
				face_mat.copyTo(temp.face);

				system_clock::time_point now = system_clock::now();
				system_clock::duration tp = now.time_since_epoch();

				tp -= duration_cast<seconds>(tp);

				time_t tt = system_clock::to_time_t(now);
				std::string date_time = get_date_time(*localtime(&tt), tp);
				temp.access = date_time;
				temp.alarm_id = (gCameraName2 == "" ? "cam2" : gCameraName2) + "-" + to_string(temp.id);
				face_detected_list2.push_back(temp);

			}
		}
		else
		{
			for (int i = 0; i < face_detected_list2.size(); i++)
			{
				if (face_detected_list2[i].removed == 1) continue;
				if (face_detected_list2[i].fail_num > (60 / SKIP_NUM))
				{
					face_detected_list2[i].removed = 1;
				}
			}
			for (int i = 0; i < face_detected_list2.size(); i++)
			{
				if (face_detected_list2[i].removed == 1) continue;
				int matched = -1;
				for (int j = 0; j < detected_list.size(); j++)
				{
					Rect intersect = detected_list[j] & face_detected_list2[i].rt;
					int area1 = detected_list[j].width * detected_list[j].height;
					int area2 = face_detected_list2[i].rt.width * face_detected_list2[i].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched >= 0)
				{
					face_detected_list2[i].rt = detected_list[matched];
					if (face_detected_list2[i].added == 0)
					{
						face_detected_list2[i].pos = detected_list[matched];
						org.copyTo(face_detected_list2[i].full_image);
						Mat face_mat = Mat(org, detected_list[matched]);
						face_mat.copyTo(face_detected_list2[i].face);
					}
					face_detected_list2[i].show_count++;
				}
				else
				{
					face_detected_list2[i].fail_num++;
				}
			}

			for (int i = 0; i < detected_list.size(); i++)
			{
				int matched = -1;
				for (int j = 0; j < face_detected_list2.size(); j++)
				{
					if (face_detected_list2[j].removed == 1) continue;
					Rect intersect = detected_list[i] & face_detected_list2[j].rt;
					int area1 = detected_list[i].width * detected_list[i].height;
					int area2 = face_detected_list2[j].rt.width * face_detected_list2[j].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched < 0)
				{
					temp.id = face_detected_list2.size();
					temp.rt = detected_list[i];

					org.copyTo(temp.full_image);
					Mat face_mat = Mat(org, detected_list[i]);
					face_mat.copyTo(temp.face);

					system_clock::time_point now = system_clock::now();
					system_clock::duration tp = now.time_since_epoch();

					tp -= duration_cast<seconds>(tp);

					time_t tt = system_clock::to_time_t(now);
					std::string date_time = get_date_time(*localtime(&tt), tp);
					temp.access = date_time;
					temp.alarm_id = (gCameraName2 == "" ? "cam2" : gCameraName2) + "-" + to_string(temp.id);
					face_detected_list2.push_back(temp);

				}
			}
		}

		for (int i = 0; i < face_detected_list2.size(); i++)
		{
			if (face_detected_list2[i].removed == 1) continue;
			if (face_detected_list2[i].show_count < (10  / SKIP_NUM)) continue;

			rectangle(inputFrame, face_detected_list2[i].rt, Scalar(0, 0, 255), 2);
			//putText(inputFrame, to_string(face_detected_list2[i].id), Point(face_detected_list2[i].rt.x, face_detected_list2[i].rt.y - 10), 0, 2, cv::Scalar(0, 0, 255), 2);
			if (face_detected_list2[i].added == 1) continue;
			locker.lock();
			total_detected_face_list.push_back(face_detected_list2[i]);
			locker.unlock();
			face_detected_list2[i].added = 1;
		}

		int rw = rect.right - rect.left;     //the width of your picture control
		int rh = rect.bottom - rect.top;
		if (!inputFrame.data) {
			ts->_this->MessageBox(_T("read picture fail！"));
			return -1;
		}
		cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
		try
		{
			int height = inputFrame.rows;
			int width = inputFrame.cols;
			uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
			BITMAPINFO* bmi = (BITMAPINFO*)buffer;
			FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
			SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
				height, 0, 0, 0, height, inputFrame.data, bmi,
				DIB_RGB_COLORS);
		}
		catch (Exception e)
		{
			cout << e.what();
			continue;
		}
	}
	return 0;
}

void CFaceRecognitionMFCDlg::OnBnClickedLive2RunBtn()
{
	// TODO: Add your control notification handler code here
	if (mMemberType == "admin")
	{
		face_detected_list2.clear();

		if (!run_ip_camera2)
		{
			mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PAUSE),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			mCameraURLSetting.setCameraIndex(2);
			int nRet = mCameraURLSetting.DoModal();
			if (nRet == IDOK)
			{
				if (mCameraURLSetting.getCameraType() == 0) //web camera
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", to_string(mCameraURLSetting.getWebCameraIndex()) },{ "camera_id", "1" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gCapture2.open(mCameraURLSetting.getWebCameraIndex());
				}
				else if (mCameraURLSetting.getCameraType() == 1) //ip camera or video
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", mCameraURLSetting.getCameraURL() },{ "camera_id", "2" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gIP2Camera = mCameraURLSetting.getCameraURL();
					gCapture2.open(gIP2Camera);
					gCameraName2 = mCameraURLSetting.getCameraName();
				}
			}
			else
			{
				mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				return;
			}
			
			if (!gCapture2.isOpened())
			{
				run_ip_camera2 = false;
				mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				return;
			}

			run_ip_camera2 = true;
			// 		DWORD tid;
			// 		live2_Handle = CreateThread(NULL, 0, thread_func2, 0, 0, &tid);

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func2, _param);
		}
		else
		{
			//gCapture2.release();
			run_ip_camera2 = false;
			mLive2RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			return;
		}
	}
	else
	{
		if (!run_ip_camera2) return;
		Mat register_image;
		locker.lock();
		live_frame2.copyTo(register_image);
		locker.unlock();

		Mat frame = register_image.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));

		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetectionRegister.enqueue(sub_frame);
			FaceDetectionRegister.submitRequest();
			FaceDetectionRegister.wait();
			FaceDetectionRegister.fetchResults();

			for (auto r : FaceDetectionRegister.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetectionRegister.enqueue(boundary_face_frame);
		FaceDetectionRegister.submitRequest();
		FaceDetectionRegister.wait();
		FaceDetectionRegister.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetectionRegister.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		vector<Rect> detected_list;
		detected_list.clear();
		Mat org;
		register_image.copyTo(org);
		for (auto rect : final_faces) {
			//Mat cropped = get_cropped(frame, rect, 160, 2);
			//FaceRecognition.load_frame(cropped);
			//output_vector = FaceRecognition.do_infer();

			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			detected_list.push_back(face);
		}

		if (detected_list.size() < 1)
		{
			MessageBox("Can't detect face!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		else if (detected_list.size() > 1)
		{
			MessageBox("There are multiple faces!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}

		Mat cropped = get_cropped(register_image, detected_list[0], 160, 2);
		FaceRecognitionRegister.load_frame(cropped);

		vector<float> output_vector;
		output_vector = FaceRecognitionRegister.do_infer();
		if (output_vector.size() != 512)
		{
			MessageBox("Can't extract features!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::string result_str = "";
		for (int i = 0; i < 512; i++) {
			result_str += std::to_string(output_vector[i]) + " ";
		}

		CRegisterPerson registerDlg;
		registerDlg.setAdminID(mAdminID);
		registerDlg.setServerURL(mServerURL);
		registerDlg.setUserID(mUserID);
		registerDlg.setImage(cropped);
		registerDlg.setRegion(detected_list[0]);
		registerDlg.setFeature(result_str);
		registerDlg.DoModal();
	}
	
}


UINT CFaceRecognitionMFCDlg::thread_func3(LPVOID param)
{
	Mat inputFrame;
	THREADSTRUCT*    ts = (THREADSTRUCT*)param;
	frame_count3 = 0;

	CDC *pDC = ts->_this->GetDlgItem(IDC_LIVE3)->GetDC();
	HDC hDC = pDC->GetSafeHdc();
	CRect rect;
	ts->_this->GetDlgItem(IDC_LIVE3)->GetClientRect(&rect);

	Face temp;

	while (run_ip_camera3)
	{
		locker.lock();
		gCapture3.read(live_frame3);
		live_frame3.copyTo(inputFrame);
		locker.unlock();
		if (inputFrame.cols == 0 || inputFrame.rows == 0)
		{
			run_ip_camera3 = false;
			gCapture3.release();
			if (ts->_this->mMemberType == "admin")
			{
				ts->_this->mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
			}
			break;
		}
		if (ts->_this == NULL) break;
		frame_count3++;
		if (frame_count3 % SKIP_NUM != 0)
		{
			if (frame_count3 > 120) frame_count3 = 0;
			
			int rw = rect.right - rect.left;     //the width of your picture control
			int rh = rect.bottom - rect.top;
			if (!inputFrame.data) {
				ts->_this->MessageBox(_T("read picture fail！"));
				return -1;
			}
			cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
			try
			{
				int height = inputFrame.rows;
				int width = inputFrame.cols;
				uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
				BITMAPINFO* bmi = (BITMAPINFO*)buffer;
				FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
				SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
					height, 0, 0, 0, height, inputFrame.data, bmi,
					DIB_RGB_COLORS);
			}
			catch (Exception e)
			{
				cout << e.what();
				continue;
			}
			continue;
		}

		mGlob3 = inputFrame.clone();
		Mat frame = mGlob3.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));


		//get all available faces for other detections
		//		vector<Rect> final_faces = get_final_faces();
		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetection3.enqueue(sub_frame);
			FaceDetection3.submitRequest();
			FaceDetection3.wait();
			FaceDetection3.fetchResults();

			for (auto r : FaceDetection3.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetection3.enqueue(boundary_face_frame);
		FaceDetection3.submitRequest();
		FaceDetection3.wait();
		FaceDetection3.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetection3.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		Mat org;
		inputFrame.copyTo(org);

		vector<Rect> detected_list;
		for (auto rect : final_faces) {
			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			int center_x = face.x + face.width / 2;
			int center_y = face.y + face.height / 2;

			int max_crop_size = min(face.width, face.height);

			int adjusted = max_crop_size * 3 / 2;

			std::vector<int> good_to_crop;
			good_to_crop.push_back(adjusted / 2);
			good_to_crop.push_back(org.size().height - center_y);
			good_to_crop.push_back(org.size().width - center_x);
			good_to_crop.push_back(center_x);
			good_to_crop.push_back(center_y);

			int final_crop = *(min_element(good_to_crop.begin(), good_to_crop.end()));

			Rect pre(center_x - max_crop_size / 2, center_y - max_crop_size / 2, max_crop_size, max_crop_size);
			Rect pre2(center_x - final_crop, center_y - final_crop, final_crop * 2, final_crop * 2);
			detected_list.push_back(pre2);
		}

		if (face_detected_list3.size() < 1)
		{
			for (int i = 0; i < detected_list.size(); i++)
			{
				temp.rt = detected_list[i];
				temp.id = i;

				org.copyTo(temp.full_image);
				Mat face_mat = Mat(org, detected_list[i]);
				face_mat.copyTo(temp.face);

				system_clock::time_point now = system_clock::now();
				system_clock::duration tp = now.time_since_epoch();

				tp -= duration_cast<seconds>(tp);

				time_t tt = system_clock::to_time_t(now);
				std::string date_time = get_date_time(*localtime(&tt), tp);
				temp.access = date_time;
				temp.alarm_id = (gCameraName3 == "" ? "cam3" : gCameraName3) + "-" + to_string(temp.id);
				face_detected_list3.push_back(temp);

			}
		}
		else
		{
			for (int i = 0; i < face_detected_list3.size(); i++)
			{
				if (face_detected_list3[i].removed == 1) continue;
				if (face_detected_list3[i].fail_num > (60 / SKIP_NUM))
				{
					face_detected_list3[i].removed = 1;
				}
			}
			for (int i = 0; i < face_detected_list3.size(); i++)
			{
				if (face_detected_list3[i].removed == 1) continue;
				int matched = -1;
				for (int j = 0; j < detected_list.size(); j++)
				{
					Rect intersect = detected_list[j] & face_detected_list3[i].rt;
					int area1 = detected_list[j].width * detected_list[j].height;
					int area2 = face_detected_list3[i].rt.width * face_detected_list3[i].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched >= 0)
				{
					face_detected_list3[i].rt = detected_list[matched];
					if (face_detected_list3[i].added == 0)
					{
						face_detected_list3[i].pos = detected_list[matched];
						org.copyTo(face_detected_list3[i].full_image);
						Mat face_mat = Mat(org, detected_list[matched]);
						face_mat.copyTo(face_detected_list3[i].face);
					}
					face_detected_list3[i].show_count++;
				}
				else
				{
					face_detected_list3[i].fail_num++;
				}
			}

			for (int i = 0; i < detected_list.size(); i++)
			{
				int matched = -1;
				for (int j = 0; j < face_detected_list3.size(); j++)
				{
					if (face_detected_list3[j].removed == 1) continue;
					Rect intersect = detected_list[i] & face_detected_list3[j].rt;
					int area1 = detected_list[i].width * detected_list[i].height;
					int area2 = face_detected_list3[j].rt.width * face_detected_list3[j].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched < 0)
				{
					temp.id = face_detected_list3.size();
					temp.rt = detected_list[i];

					org.copyTo(temp.full_image);
					Mat face_mat = Mat(org, detected_list[i]);
					face_mat.copyTo(temp.face);

					system_clock::time_point now = system_clock::now();
					system_clock::duration tp = now.time_since_epoch();

					tp -= duration_cast<seconds>(tp);

					time_t tt = system_clock::to_time_t(now);
					std::string date_time = get_date_time(*localtime(&tt), tp);
					temp.access = date_time;
					temp.alarm_id = (gCameraName3 == "" ? "cam3" : gCameraName3) + "-" + to_string(temp.id);
					face_detected_list3.push_back(temp);

				}
			}
		}

		for (int i = 0; i < face_detected_list3.size(); i++)
		{
			if (face_detected_list3[i].removed == 1) continue;
			if (face_detected_list3[i].show_count < (10  / SKIP_NUM)) continue;

			rectangle(inputFrame, face_detected_list3[i].rt, Scalar(0, 0, 255), 2);
			//putText(inputFrame, to_string(face_detected_list3[i].id), Point(face_detected_list3[i].rt.x, face_detected_list3[i].rt.y - 10), 0, 2, cv::Scalar(0, 0, 255), 2);
			if (face_detected_list3[i].added == 1) continue;
			locker.lock();
			total_detected_face_list.push_back(face_detected_list3[i]);
			locker.unlock();
			face_detected_list3[i].added = 1;
		}

		int rw = rect.right - rect.left;     //the width of your picture control
		int rh = rect.bottom - rect.top;
		if (!inputFrame.data) {
			ts->_this->MessageBox(_T("read picture fail！"));
			return -1;
		}
		cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
		try
		{
			int height = inputFrame.rows;
			int width = inputFrame.cols;
			uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
			BITMAPINFO* bmi = (BITMAPINFO*)buffer;
			FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
			SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
				height, 0, 0, 0, height, inputFrame.data, bmi,
				DIB_RGB_COLORS);
		}
		catch (Exception e)
		{
			cout << e.what();
			continue;
		}
	}
	return 0;
}


void CFaceRecognitionMFCDlg::OnBnClickedLive3RunBtn()
{
	// TODO: Add your control notification handler code here
	if (mMemberType == "admin")
	{
		face_detected_list3.clear();

		if (!run_ip_camera3)
		{
			mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PAUSE),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			mCameraURLSetting.setCameraIndex(3);
			int nRet = mCameraURLSetting.DoModal();
			if (nRet == IDOK)
			{
				if (mCameraURLSetting.getCameraType() == 0) //web camera
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", to_string(mCameraURLSetting.getWebCameraIndex()) },{ "camera_id", "1" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gCapture3.open(mCameraURLSetting.getWebCameraIndex());
				}
				else if (mCameraURLSetting.getCameraType() == 1) //ip camera or video
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", mCameraURLSetting.getCameraURL() },{ "camera_id", "3" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gIP3Camera = mCameraURLSetting.getCameraURL();
					gCapture3.open(gIP3Camera);
					gCameraName3 = mCameraURLSetting.getCameraName();
				}
			}
			else
			{
				mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				run_ip_camera3 = false;
				return;
			}
			

			if (!gCapture3.isOpened())
			{
				mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				run_ip_camera3 = false;
				return;
			}

			run_ip_camera3 = true;
			// 		DWORD tid;
			// 		live3_Handle = CreateThread(NULL, 0, thread_func3, 0, 0, &tid);

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func3, _param);
		}
		else
		{
			run_ip_camera3 = false;
			mLive3RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			return;
		}
	}
	else
	{
		if (!run_ip_camera3) return;
		Mat register_image;
		locker.lock();
		live_frame3.copyTo(register_image);
		locker.unlock();

		Mat frame = register_image.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));

		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetectionRegister.enqueue(sub_frame);
			FaceDetectionRegister.submitRequest();
			FaceDetectionRegister.wait();
			FaceDetectionRegister.fetchResults();

			for (auto r : FaceDetectionRegister.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetectionRegister.enqueue(boundary_face_frame);
		FaceDetectionRegister.submitRequest();
		FaceDetectionRegister.wait();
		FaceDetectionRegister.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetectionRegister.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		vector<Rect> detected_list;
		detected_list.clear();
		Mat org;
		register_image.copyTo(org);
		for (auto rect : final_faces) {
			//Mat cropped = get_cropped(frame, rect, 160, 2);
			//FaceRecognition.load_frame(cropped);
			//output_vector = FaceRecognition.do_infer();

			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			detected_list.push_back(face);
		}

		if (detected_list.size() < 1)
		{
			MessageBox("Can't detect face!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		else if (detected_list.size() > 1)
		{
			MessageBox("There are multiple faces!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}

		Mat cropped = get_cropped(register_image, detected_list[0], 160, 2);
		FaceRecognitionRegister.load_frame(cropped);

		vector<float> output_vector;
		output_vector = FaceRecognitionRegister.do_infer();
		if (output_vector.size() != 512)
		{
			MessageBox("Can't extract features!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::string result_str = "";
		for (int i = 0; i < 512; i++) {
			result_str += std::to_string(output_vector[i]) + " ";
		}

		CRegisterPerson registerDlg;
		registerDlg.setAdminID(mAdminID);
		registerDlg.setServerURL(mServerURL);
		registerDlg.setUserID(mUserID);
		registerDlg.setImage(cropped);
		registerDlg.setRegion(detected_list[0]);
		registerDlg.setFeature(result_str);
		registerDlg.DoModal();
	}
}

UINT CFaceRecognitionMFCDlg::thread_func4(LPVOID param)
{
	Mat inputFrame;
	THREADSTRUCT*    ts = (THREADSTRUCT*)param;
	frame_count4 = 0;

	CDC *pDC = ts->_this->GetDlgItem(IDC_LIVE4)->GetDC();
	HDC hDC = pDC->GetSafeHdc();
	CRect rect;
	ts->_this->GetDlgItem(IDC_LIVE4)->GetClientRect(&rect);

	Face temp;

	while (run_ip_camera4)
	{
		locker.lock();
		gCapture4.read(live_frame4);
		live_frame4.copyTo(inputFrame);
		locker.unlock();
		if (inputFrame.cols == 0 || inputFrame.rows == 0)
		{
			run_ip_camera4 = false;
			gCapture4.release();
			if (ts->_this->mMemberType == "admin")
			{
				ts->_this->mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
			}
			break;
		}
		if (ts->_this == NULL) break;
		frame_count4++;
		if (frame_count4 % SKIP_NUM != 0)
		{
			if (frame_count4 > 120) frame_count4 = 0;
			
			int rw = rect.right - rect.left;     //the width of your picture control
			int rh = rect.bottom - rect.top;
			if (!inputFrame.data) {
				ts->_this->MessageBox(_T("read picture fail！"));
				return -1;
			}
			cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
			try
			{
				int height = inputFrame.rows;
				int width = inputFrame.cols;
				uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
				BITMAPINFO* bmi = (BITMAPINFO*)buffer;
				FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
				SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
					height, 0, 0, 0, height, inputFrame.data, bmi,
					DIB_RGB_COLORS);
			}
			catch (Exception e)
			{
				cout << e.what();
				continue;
			}
			continue;
		}

		mGlob4 = inputFrame.clone();
		Mat frame = mGlob4.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));


		//get all available faces for other detections
		//		vector<Rect> final_faces = get_final_faces();
		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetection4.enqueue(sub_frame);
			FaceDetection4.submitRequest();
			FaceDetection4.wait();
			FaceDetection4.fetchResults();

			for (auto r : FaceDetection4.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetection4.enqueue(boundary_face_frame);
		FaceDetection4.submitRequest();
		FaceDetection4.wait();
		FaceDetection4.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetection4.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		Mat org;
		inputFrame.copyTo(org);

		vector<Rect> detected_list;
		for (auto rect : final_faces) {
			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			int center_x = face.x + face.width / 2;
			int center_y = face.y + face.height / 2;

			int max_crop_size = min(face.width, face.height);

			int adjusted = max_crop_size * 3 / 2;

			std::vector<int> good_to_crop;
			good_to_crop.push_back(adjusted / 2);
			good_to_crop.push_back(org.size().height - center_y);
			good_to_crop.push_back(org.size().width - center_x);
			good_to_crop.push_back(center_x);
			good_to_crop.push_back(center_y);

			int final_crop = *(min_element(good_to_crop.begin(), good_to_crop.end()));

			Rect pre(center_x - max_crop_size / 2, center_y - max_crop_size / 2, max_crop_size, max_crop_size);
			Rect pre2(center_x - final_crop, center_y - final_crop, final_crop * 2, final_crop * 2);
			detected_list.push_back(pre2);
		}

		if (face_detected_list4.size() < 1)
		{
			for (int i = 0; i < detected_list.size(); i++)
			{
				temp.rt = detected_list[i];
				temp.id = i;

				org.copyTo(temp.full_image);
				Mat face_mat = Mat(org, detected_list[i]);
				face_mat.copyTo(temp.face);

				system_clock::time_point now = system_clock::now();
				system_clock::duration tp = now.time_since_epoch();

				tp -= duration_cast<seconds>(tp);

				time_t tt = system_clock::to_time_t(now);
				std::string date_time = get_date_time(*localtime(&tt), tp);
				temp.access = date_time;
				temp.alarm_id = (gCameraName4=="" ? "cam4": gCameraName4) + "-" + to_string(temp.id);
				face_detected_list4.push_back(temp);

			}
		}
		else
		{
			for (int i = 0; i < face_detected_list4.size(); i++)
			{
				if (face_detected_list4[i].removed == 1) continue;
				if (face_detected_list4[i].fail_num > (60 / SKIP_NUM))
				{
					face_detected_list4[i].removed = 1;
				}
			}
			for (int i = 0; i < face_detected_list4.size(); i++)
			{
				if (face_detected_list4[i].removed == 1) continue;
				int matched = -1;
				for (int j = 0; j < detected_list.size(); j++)
				{
					Rect intersect = detected_list[j] & face_detected_list4[i].rt;
					int area1 = detected_list[j].width * detected_list[j].height;
					int area2 = face_detected_list4[i].rt.width * face_detected_list4[i].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched >= 0)
				{
					face_detected_list4[i].rt = detected_list[matched];
					if (face_detected_list4[i].added == 0)
					{
						face_detected_list4[i].pos = detected_list[matched];

						org.copyTo(face_detected_list4[i].full_image);
						Mat face_mat = Mat(org, detected_list[matched]);
						face_mat.copyTo(face_detected_list4[i].face);
					}
					face_detected_list4[i].show_count++;
				}
				else
				{
					face_detected_list4[i].fail_num++;
				}
			}

			for (int i = 0; i < detected_list.size(); i++)
			{
				int matched = -1;
				for (int j = 0; j < face_detected_list4.size(); j++)
				{
					if (face_detected_list4[j].removed == 1) continue;
					Rect intersect = detected_list[i] & face_detected_list4[j].rt;
					int area1 = detected_list[i].width * detected_list[i].height;
					int area2 = face_detected_list4[j].rt.width * face_detected_list4[j].rt.height;
					int max_area = (area1 > area2 ? area1 : area2);
					int area = intersect.width * intersect.height;
					if (area > (int)(max_area * 0.2f))
					{
						matched = j;
						break;
					}
				}
				if (matched < 0)
				{
					temp.id = face_detected_list4.size();
					temp.rt = detected_list[i];

					org.copyTo(temp.full_image);
					Mat face_mat = Mat(org, detected_list[i]);
					face_mat.copyTo(temp.face);

					system_clock::time_point now = system_clock::now();
					system_clock::duration tp = now.time_since_epoch();

					tp -= duration_cast<seconds>(tp);

					time_t tt = system_clock::to_time_t(now);
					std::string date_time = get_date_time(*localtime(&tt), tp);
					temp.access = date_time;
					temp.alarm_id = (gCameraName4 == "" ? "cam4" : gCameraName4) + "-" + to_string(temp.id);
					face_detected_list4.push_back(temp);

				}
			}
		}

		for (int i = 0; i < face_detected_list4.size(); i++)
		{
			if (face_detected_list4[i].removed == 1) continue;
			if (face_detected_list4[i].show_count < (10  / SKIP_NUM)) continue;

			rectangle(inputFrame, face_detected_list4[i].rt, Scalar(0, 0, 255), 2);
			//putText(inputFrame, to_string(face_detected_list4[i].id), Point(face_detected_list4[i].rt.x, face_detected_list4[i].rt.y - 10), 0, 2, cv::Scalar(0, 0, 255), 2);
			if (face_detected_list4[i].added == 1) continue;
			locker.lock();
			total_detected_face_list.push_back(face_detected_list4[i]);
			locker.unlock();
			face_detected_list4[i].added = 1;
		}

		int rw = rect.right - rect.left;     //the width of your picture control
		int rh = rect.bottom - rect.top;
		if (!inputFrame.data) {
			ts->_this->MessageBox(_T("read picture fail！"));
			return -1;
		}
		cv::resize(inputFrame, inputFrame, cv::Size(rw, rh));
		try
		{
			int height = inputFrame.rows;
			int width = inputFrame.cols;
			uchar buffer[sizeof(BITMAPINFOHEADER) + 1024];
			BITMAPINFO* bmi = (BITMAPINFO*)buffer;
			FillBitmapInfo(bmi, width, height, Bpp(inputFrame), 0);
			SetDIBitsToDevice(pDC->GetSafeHdc(), 0, 0, width,
				height, 0, 0, 0, height, inputFrame.data, bmi,
				DIB_RGB_COLORS);
		}
		catch (Exception e)
		{
			cout << e.what();
			continue;
		}
	}
	return 0;
}


void CFaceRecognitionMFCDlg::OnBnClickedLive4RunBtn()
{
	// TODO: Add your control notification handler code here
	if (mMemberType == "admin")
	{
		face_detected_list4.clear();
		if (!run_ip_camera4)
		{
			mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PAUSE),
				IMAGE_BITMAP, 45, 45, LR_COLOR));

			mCameraURLSetting.setCameraIndex(4);
			int nRet = mCameraURLSetting.DoModal();
			if (nRet == IDOK)
			{
				if (mCameraURLSetting.getCameraType() == 0) //web camera
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", to_string(mCameraURLSetting.getWebCameraIndex()) },{ "camera_id", "1" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gCapture4.open(mCameraURLSetting.getWebCameraIndex());
				}
				else if (mCameraURLSetting.getCameraType() == 1) //ip camera or video
				{
					//save camera url to server
					std::string server_url = mServerURL + "/SaveCameraUrl";
					try
					{
						// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
						http::Request request(server_url);																		  // pass parameters as a map
						std::map<std::string, std::string> parameters = { { "user_id", mCameraURLSetting.getUserIDForCameraSetting() },{ "admin_id", mAdminID },
						{ "camera_name", mCameraURLSetting.getCameraName() },
						{ "camera_url", mCameraURLSetting.getCameraURL() },{ "camera_id", "4" } };
						const http::Response response = request.send("POST", parameters, {
							"Content-Type: application/x-www-form-urlencoded"
						});
						std::string resultString = std::string(response.body.begin(), response.body.end());
						json::jobject jsonObject = json::jobject::parse(resultString);

						std::string result = jsonObject["result"];

						if (result == "fail")
						{
							MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
							mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
								IMAGE_BITMAP, 45, 45, LR_COLOR));
							return;
						}
						else
						{
							MessageBox("Successfully saved camera info!", "Info!", MB_OK | MB_ICONQUESTION);
						}
					}
					catch (const std::exception& e)
					{
						MessageBox("Failed to save camera info!", "Error!", MB_OK | MB_ICONQUESTION);
						mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
							IMAGE_BITMAP, 45, 45, LR_COLOR));
						return;
					}
					gIP4Camera = mCameraURLSetting.getCameraURL();
					gCapture4.open(gIP4Camera);
					gCameraName4 = mCameraURLSetting.getCameraName();
				}
			}
			else
			{
				mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				run_ip_camera4 = false;
				return;
			}
			
			if (!gCapture4.isOpened())
			{
				mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
					IMAGE_BITMAP, 45, 45, LR_COLOR));
				run_ip_camera4 = false;
				return;
			}

			run_ip_camera4 = true;
			// 		DWORD tid;
			// 		live4_Handle = CreateThread(NULL, 0, thread_func4, 0, 0, &tid);

			THREADSTRUCT *_param = new THREADSTRUCT;
			_param->_this = this;
			AfxBeginThread(thread_func4, _param);
		}
		else
		{
			//gCapture4.release();
			run_ip_camera4 = false;
			mLive4RunBtn.SetBitmap((HBITMAP)LoadImage(AfxGetApp()->m_hInstance, MAKEINTRESOURCE(IDB_PLAY),
				IMAGE_BITMAP, 45, 45, LR_COLOR));
			return;
		}
	}
	else
	{
		if (!run_ip_camera4) return;
		Mat register_image;
		locker.lock();
		live_frame4.copyTo(register_image);
		locker.unlock();

		Mat frame = register_image.clone();
		float wscale = (float)frame.cols / RESIZE_WIDTH;
		float hscale = (float)frame.rows / RESIZE_WIDTH;
		resize(frame, frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));

		int sub_width;
		int sub_height;
		int sub_x, sub_y;
		vector<cv::Rect> sub_frame_rects;
		vector<Rect> final_faces; //collection of faces ready for recognition
		final_faces.clear();
		int num_x = frame.cols / RESIZE_WIDTH;
		int num_y = frame.rows / RESIZE_WIDTH;

		//the rightmost cols or botton rows may not have same cut as other subs
		//need to handle a little bit
		for (int i = 0; i < num_x; ++i)
		{
			for (int j = 0; j < num_y; ++j)
			{
				sub_width = frame.cols / num_x;
				sub_height = frame.rows / num_y;
				sub_x = i * sub_width;
				sub_y = j * sub_height;
				if (i == num_x - 1) {
					sub_width = frame.cols - (num_x - 1)*sub_width;
				}
				if (j == num_y - 1) {
					sub_height = frame.rows - (num_y - 1)*sub_height;
				}
				sub_frame_rects.push_back(Rect(sub_x, sub_y, sub_width, sub_height));
			}
		}
		//for each sub_frame do facedetection, upon fetching results, check faces 
		//near boudary, redo facedetection for these faces, and then for all 
		//detected faces, do face recognition
		//pair of sub_frame - face_detection results
		Mat sub_frame;
		vector <Rect> detected_faces, boundary_face_results;
		boundary_face_results.clear();
		for (auto rect : sub_frame_rects) {
			sub_frame = frame(rect);
			FaceDetectionRegister.enqueue(sub_frame);
			FaceDetectionRegister.submitRequest();
			FaceDetectionRegister.wait();
			FaceDetectionRegister.fetchResults();

			for (auto r : FaceDetectionRegister.results) {
				//cout << r.confidence <<endl;
				if (r.confidence > thresh) {
					detected_faces.push_back(Rect(r.location.x + rect.x, r.location.y + rect.y, r.location.width, r.location.height));
				}
			}
		}
		//some faces are at boundaries of each sub_frame, so need to count them in. 
		boundary_face_results = get_boundary_face_results(&final_faces, detected_faces, frame.cols / num_x, frame.rows / num_y, frame.cols, frame.rows);
		//for boundary faces, copy to a bigger mat, and do a face detection for them
		//in the boundary face restuls, form a good mat with all boundary faces, and then do facedetection once more. 
		//creata 300*300 frame 
		Mat boundary_face_frame = frame.clone();
		//white out the frame
		boundary_face_frame = Scalar(255, 255, 255);
		resize(boundary_face_frame, boundary_face_frame, Size(RESIZE_WIDTH, RESIZE_WIDTH));
		//split into equal squares to store boundary faces
		int side_num = (int)sqrt(boundary_face_results.size()) + 1;
		int side_len = boundary_face_frame.cols / side_num;
		int index = 0;
		vector <pair<Rect, Rect>> links;  //keeps track of mapping from original frame to boundary_face_frame
		vector <Rect> boundary_frame_squares; //collection of all nxn squares in the boundary_face_frame
		for (int i = 0; i < side_num; ++i)
		{
			for (int j = 0; j < side_num; ++j)
			{
				if (index == boundary_face_results.size())break;
				Rect src_rect = boundary_face_results[index];
				Rect dist_rect = Rect(i*side_len,
					j*side_len,
					side_len,
					side_len);
				boundary_frame_squares.push_back(dist_rect);
				Mat src = frame(src_rect).clone();
				Mat distROI = boundary_face_frame(dist_rect);
				resize(src, src, Size(side_len, side_len));
				src.copyTo(distROI);

				//first : original  second: square on boundary_frame
				links.push_back(pair<Rect, Rect>(src_rect, dist_rect));
				index++;
			}
		}

		FaceDetectionRegister.enqueue(boundary_face_frame);
		FaceDetectionRegister.submitRequest();
		FaceDetectionRegister.wait();
		FaceDetectionRegister.fetchResults();
		vector<Rect> extra_faces;
		for (auto result : FaceDetectionRegister.results) {
			//cout << r.confidence <<endl;   
			if (result.confidence > thresh) {
				//rectangle(boundary_face_frame, result.location, Scalar(160,16,163));
				//put result back to original frame
				for (auto link : links) {
					Rect boundary_square = link.second;
					Rect frame_square = link.first;
					Rect r = result.location;
					if ((boundary_square.x <= r.x) &&
						(boundary_square.x + boundary_square.width >= r.x + r.width) &&
						(boundary_square.y <= r.y) &&
						(boundary_square.y + boundary_square.height >= r.y + r.height))
					{
						//rectangle(boundary_face_frame, r, Scalar(160,16,163));
						float ratio = frame_square.width / (float)boundary_square.width;
						Rect actual_place;
						actual_place.x = (int)(frame_square.x + (r.x - boundary_square.x)*ratio);
						actual_place.y = (int)(frame_square.y + (r.y - boundary_square.y)*ratio);
						actual_place.width = (int)(r.width * ratio);
						actual_place.height = (int)(r.height * ratio);
						extra_faces.push_back(actual_place);
						final_faces.push_back(actual_place);
					}
				}
			}
		}

		vector<Rect> detected_list;
		detected_list.clear();
		Mat org;
		register_image.copyTo(org);
		for (auto rect : final_faces) {
			//Mat cropped = get_cropped(frame, rect, 160, 2);
			//FaceRecognition.load_frame(cropped);
			//output_vector = FaceRecognition.do_infer();

			Rect face((int)(rect.x * wscale), (int)(rect.y * hscale), (int)(rect.width * wscale), (int)(rect.height * hscale));
			detected_list.push_back(face);
		}

		if (detected_list.size() < 1)
		{
			MessageBox("Can't detect face!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		else if (detected_list.size() > 1)
		{
			MessageBox("There are multiple faces!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}

		Mat cropped = get_cropped(register_image, detected_list[0], 160, 2);
		FaceRecognitionRegister.load_frame(cropped);

		vector<float> output_vector;
		output_vector = FaceRecognitionRegister.do_infer();
		if (output_vector.size() != 512)
		{
			MessageBox("Can't extract features!", "Error!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::string result_str = "";
		for (int i = 0; i < 512; i++) {
			result_str += std::to_string(output_vector[i]) + " ";
		}

		CRegisterPerson registerDlg;
		registerDlg.setAdminID(mAdminID);
		registerDlg.setServerURL(mServerURL);
		registerDlg.setUserID(mUserID);
		registerDlg.setImage(cropped);
		registerDlg.setRegion(detected_list[0]);
		registerDlg.setFeature(result_str);
		registerDlg.DoModal();
	}
}


void CFaceRecognitionMFCDlg::OnClose()
{
	// TODO: Add your message handler code here and/or call default
	run_ip_camera1 = false;
	run_ip_camera2 = false;
	run_ip_camera3 = false;
	run_ip_camera4 = false;

	gCapture1.release();
	gCapture2.release();
	gCapture3.release();
	gCapture4.release();

	CDialogEx::OnClose();
}


BOOL CFaceRecognitionMFCDlg::OnEraseBkgnd(CDC* pDC)
{
	// TODO: Add your message handler code here and/or call default
	//CRect r;
	//pDC->GetClipBox(&r);

	//pDC->FillSolidRect(r, RGB(255, 0, 255)); //ugly magenta background
	//pDC->SetBkColor(RGB(0, 255, 0));
	return CDialogEx::OnEraseBkgnd(pDC);
}


HBRUSH CFaceRecognitionMFCDlg::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CDialogEx::OnCtlColor(pDC, pWnd, nCtlColor);

	// TODO:  Change any attributes of the DC here
	switch (nCtlColor)
	{
	case CTLCOLOR_STATIC:
		pDC->SetTextColor(RGB(255, 255, 255));
		pDC->SetBkColor(RGB(42, 42, 42));
		return (HBRUSH)GetStockObject(NULL_BRUSH);
	
	default:
		// TODO:  Return a different brush if the default is not desired
		hbr = CreateSolidBrush(RGB(42, 42, 42)); // <= create on initDialog
		return hbr;
	}
	
}


void CFaceRecognitionMFCDlg::OnBnClickedClose()
{
	// TODO: Add your control notification handler code here
	run_ip_camera1 = false;
	run_ip_camera2 = false;
	run_ip_camera3 = false;
	run_ip_camera4 = false;
	Sleep(1000);
	EndDialog(0);
}


void CFaceRecognitionMFCDlg::OnBnClickedSetting()
{
	// TODO: Add your control notification handler code here
	std::string config_file = ROOTDIR + "/config.txt";
	CSettingDlg settingDlg;
	settingDlg.setServerURL(mServerURL);
	settingDlg.setAdminID(mAdminID);
	INT_PTR response = settingDlg.DoModal();
	if (response == IDOK)
	{
		if (settingDlg.getGroupName().size() == 0)
		{
			MessageBox("Can't set group to push notification!", "Info!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::string outfile(config_file);
		ofstream fs1;
		fs1.open(outfile);
		fs1 << "adminid=" << mAdminID << endl;
		std::string group_lists = "";
		for (int i = 0; i < settingDlg.getGroupName().size(); i++)
		{
			group_lists += settingDlg.getGroupName()[i] + " ";
		}
		fs1 << "groupToPush=" << group_lists;
		fs1.close();

		mGroupToPush = settingDlg.getGroupName();
		MessageBox("Successfully set group to push notification!", "Info!", MB_OK | MB_ICONQUESTION);
	}
}
