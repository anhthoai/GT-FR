// RegisterPerson.cpp : implementation file
//

#include "stdafx.h"
#include "FaceRecognitionMFC.h"
#include "RegisterPerson.h"
#include "afxdialogex.h"
#include <iostream>

#include "HTTPRequest.hpp"
#include "json.hpp"

int Bpp1(cv::Mat img) { return 8 * img.channels(); }
void FillBitmapInfo1(BITMAPINFO* bmi, int width, int height, int bpp, int origin)
{
	//assert(bmi && width >= 0 && height >= 0 && (bpp == 8 || bpp == 24 || bpp == 32));

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

int Mat2CImage11(cv::Mat mat, CImage &img) {
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

static const std::string base64_chars1 =
"ABCDEFGHIJKLMNOPQRSTUVWXYZ"
"abcdefghijklmnopqrstuvwxyz"
"0123456789+/";

static inline bool is_base641(unsigned char c) {
	return (isalnum(c) || (c == '+') || (c == '/'));
}

std::string base64_encode1(unsigned char const* bytes_to_encode, unsigned int in_len) {
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
				ret += base64_chars1[char_array_4[i]];
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
			ret += base64_chars1[char_array_4[j]];

		while ((i++ < 3))
			ret += '=';

	}

	return ret;

}

// CRegisterPerson dialog

IMPLEMENT_DYNAMIC(CRegisterPerson, CDialogEx)

CRegisterPerson::CRegisterPerson(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_REGISTER_PERSON, pParent)
	, mGroupName(_T(""))
	, mPersonName(_T(""))
	, mPersonEmail(_T(""))
	, mPersonPhone(_T(""))
{

}

CRegisterPerson::~CRegisterPerson()
{
}

void CRegisterPerson::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_GROUP_COMBO, mGroupList);
	DDX_CBString(pDX, IDC_GROUP_COMBO, mGroupName);
	DDX_Text(pDX, IDC_NAME_EDIT, mPersonName);
	DDX_Text(pDX, IDC_EMAIL_EDIT, mPersonEmail);
	DDX_Text(pDX, IDC_PHONE_EDIT, mPersonPhone);
}


BEGIN_MESSAGE_MAP(CRegisterPerson, CDialogEx)
	ON_BN_CLICKED(IDC_REGISTER_CLOSE, &CRegisterPerson::OnBnClickedRegisterClose)
	ON_BN_CLICKED(IDC_REGISTER, &CRegisterPerson::OnBnClickedRegister)
	ON_BN_CLICKED(IDC_SHOW_IMAGE, &CRegisterPerson::OnBnClickedShowImage)
END_MESSAGE_MAP()


// CRegisterPerson message handlers


BOOL CRegisterPerson::OnInitDialog()
{
	CDialogEx::OnInitDialog();
	
	// TODO:  Add extra initialization here
	UpdateData(TRUE);
	GetDlgItem(IDC_PERSON_IMAGE)->MoveWindow(10, 10, 200, 200);
	std::string server_url = mServerURL + "/GroupSearch";

	mGroupList.Clear();
	mGroupList.AddString(_T("Nogroup"));
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url);																		  // pass parameters as a map
		std::map<std::string, std::string> parameters = { {"userid", mUserID},  { "adminid", mAdminID } };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		std::string result = jsonObject["searchlists"];

		if (result == "none")
		{
			MessageBox("No group for register person!", "Warning!", MB_OK | MB_ICONQUESTION);
		}
		else
		{
			std::vector<std::string> lists = json::parsing::parse_array(result);
			std::vector<json::jobject> results;
			for (size_t i = 0; i < lists.size(); i++) 
				results.push_back(json::jobject::parse(lists[i]));
			for (int i = 0; i < results.size(); i++)
			{
				std::string group_name = results[i]["group_name"];
				mGroupList.AddString(group_name.c_str());
			}
		}
	}
	catch (const std::exception& e)
	{
		std::cerr << "Request failed, error: " << e.what() << '\n';
	}
	mGroupList.SetCurSel(0);

	mGroupList.GetLBText(0, mGroupName);
	return TRUE;  // return TRUE unless you set the focus to a control
				  // EXCEPTION: OCX Property Pages should return FALSE
}


void CRegisterPerson::OnBnClickedRegisterClose()
{
	// TODO: Add your control notification handler code here
	EndDialog(-1);
}


void CRegisterPerson::OnBnClickedRegister()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	CT2A personname(mPersonName);
	CT2A personemail(mPersonEmail);
	CT2A personphone(mPersonPhone);
	std::string person_name(personname.m_psz);
	std::string person_email(personemail.m_psz);
	std::string person_phone(personphone.m_psz);

	if (mGroupList.GetCount() < 1)
	{
		mGroupName = _T("Nogroup");
	}
	else
	{
		mGroupList.GetLBText(mGroupList.GetCurSel(), mGroupName);
	}
	
	CT2A group(mGroupName);
	std::string group_name(group.m_psz);
	std::string server_url = mServerURL + "/RegisterFromUser";
	std::string query = "INSERT INTO person_info (name,email,phone,person_img, feature, adminid, group_name, average) VALUES (?, ?,?,?,?,?,?,?)";
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url);	
		
		std::vector<uchar> buf;
		cv::imencode(".jpg", person_image, buf);
		auto *enc_msg = reinterpret_cast<unsigned char*>(buf.data());
		std::string encoded_face_img = base64_encode1(enc_msg, buf.size());

		// pass parameters as a map
		std::map<std::string, std::string> parameters = { { "userid", mUserID },{ "name", person_name }, {"email", person_email}, {"phone", person_phone},
														{"group_name", group_name }, {"query", query}, {"image", encoded_face_img}, {"adminid", mAdminID}, {"feature", feature} };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		std::string result = jsonObject["registerlists"];

		if (result == "ok")
		{
			MessageBox("Successfully registered person!", "Info!", MB_OK | MB_ICONQUESTION);
			EndDialog(0);
		}
		else
		{
			MessageBox("Register failed!", "Error!", MB_OK | MB_ICONQUESTION);
		}
	}
	catch (const std::exception& e)
	{
		std::cerr << "Request failed, error: " << e.what() << '\n';
	}
}


void CRegisterPerson::OnBnClickedShowImage()
{
	// TODO: Add your control notification handler code here
	CDC *pDC = GetDlgItem(IDC_PERSON_IMAGE)->GetDC();
	HDC hDC = pDC->GetSafeHdc();
	CRect rect;
	GetDlgItem(IDC_PERSON_IMAGE)->GetClientRect(&rect);
	int rw = rect.right - rect.left;     //the width of your picture control
	int rh = rect.bottom - rect.top;
	cv::Mat img;
	person_image.copyTo(img);
	cv::rectangle(img, face_rect, cv::Scalar(0, 255, 0), 2);
	if (!img.data) {
		MessageBox(_T("read picture fail"));
		return;
	}
	cv::resize(img, img, cv::Size(rw, rh));
	CImage image;
	Mat2CImage11(img, image);
	image.Draw(hDC, rect);
}
