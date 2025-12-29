// CameraURLSetting.cpp : implementation file
//
#pragma once

#include "stdafx.h"
#include "FaceRecognitionMFC.h"
#include "CameraURLSetting.h"
#include "afxdialogex.h"
#include <iostream>

#include "HTTPRequest.hpp"
#include "json.hpp"



// CameraURLSetting dialog

IMPLEMENT_DYNAMIC(CameraURLSetting, CDialogEx)

CameraURLSetting::CameraURLSetting(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_CAMERAURL_SETTING, pParent)
	, mCameraName(_T(""))
	, mWebCameraIndex(0)
	, mStreamURL(_T(""))
	, mUserIDForCamera(_T(""))
{
	mCameraIndex = 0;
	mWebCameraIndex = 0;
	mCameraType = 0;
}

CameraURLSetting::~CameraURLSetting()
{
}

void CameraURLSetting::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT6, mCameraName);
	DDX_Text(pDX, IDC_EDIT1, mWebCameraIndex);
	DDV_MinMaxInt(pDX, mWebCameraIndex, 0, 10);
	DDX_Text(pDX, IDC_EDIT5, mStreamURL);
	DDX_Control(pDX, IDC_COMBO1, mUserList);
	DDX_CBString(pDX, IDC_COMBO1, mUserIDForCamera);
}


BEGIN_MESSAGE_MAP(CameraURLSetting, CDialogEx)
	ON_BN_CLICKED(IDOK, &CameraURLSetting::OnBnClickedOk)
	ON_BN_CLICKED(ID_CANCEL, &CameraURLSetting::OnBnClickedCancel)
	ON_BN_CLICKED(IDC_RADIO1, &CameraURLSetting::OnBnClickedRadio1)
	ON_BN_CLICKED(IDC_RADIO2, &CameraURLSetting::OnBnClickedRadio2)
	ON_CBN_SELCHANGE(IDC_COMBO1, &CameraURLSetting::OnSelchangeCombo1)
END_MESSAGE_MAP()


// CameraURLSetting message handlers


void CameraURLSetting::OnBnClickedOk()
{
	// TODO: Add your control notification handler code here
	CDialogEx::OnOK();
}


void CameraURLSetting::OnBnClickedCancel()
{
	// TODO: Add your control notification handler code here
	CDialogEx::OnCancel();
}


BOOL CameraURLSetting::OnInitDialog()
{
	CDialogEx::OnInitDialog();
	CButton* pButton = (CButton*)GetDlgItem(IDC_RADIO1);
	pButton->SetCheck(true);

	GetDlgItem(IDC_EDIT5)->EnableWindow(false);
	GetDlgItem(IDC_COMBO1)->EnableWindow(false);
	GetDlgItem(IDC_EDIT6)->EnableWindow(false);

	std::string server_url = mServerURL + "/AgentUserSearch";
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url);																		  // pass parameters as a map
		std::map<std::string, std::string> parameters = { { "user_id", mUserID},{ "admin_id", mAdminID } };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		std::string result = jsonObject["searchlists"];
		
		if (result == "none")
		{
			MessageBox("No user for setting camera info!", "Warning!", MB_OK | MB_ICONQUESTION);
		}
		else
		{
			std::string delimiter = " ";

			size_t pos = 0;
			std::string token;
			while ((pos = result.find(delimiter)) != std::string::npos) {
				token = result.substr(0, pos);
				std::cout << token << std::endl;
				mUserList.AddString(token.c_str());
				result.erase(0, pos + delimiter.length());
			}
			mUserList.SetCurSel(0);

			mUserList.GetLBText(0, mUserIDForCamera);
			std::string server_url = mServerURL + "/ReadCameraInfo";
			try
			{
				// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
				http::Request request(server_url);		  // pass parameters as a map
				CT2A ascii(mUserIDForCamera);
				char * userid = ascii.m_psz;
				std::string user_id = std::string(userid);
				std::map<std::string, std::string> parameters = { { "user_id", user_id },{ "admin_id", mAdminID },{ "camera_id", to_string(mCameraIndex) } };
				const http::Response response = request.send("POST", parameters, {
					"Content-Type: application/x-www-form-urlencoded"
				});
				std::string resultString = std::string(response.body.begin(), response.body.end());
				json::jobject jsonObject = json::jobject::parse(resultString);

				std::string result = jsonObject["searchlists"];

				if (result == "none")
				{
					MessageBox("There is no camera for this user!", "Warning!", MB_OK | MB_ICONQUESTION);
				}
				else
				{
					std::vector<std::string> lists = json::parsing::parse_array(result);
					std::vector<json::jobject> results;
					for (size_t i = 0; i < lists.size(); i++) results.push_back(json::jobject::parse(lists[i]));
					if (results.size() < 1) return TRUE;
					std::string name = results[0]["camera_name"];
					CString ss(name.c_str());
					mCameraName = ss;

					std::string url = results[0]["camera_url"];
					CString sss(url.c_str());
					mStreamURL = sss;

					UpdateData(FALSE);
				}
			}
			catch (const std::exception& e)
			{
				std::cerr << "There is no camera for this user!" << e.what() << '\n';
			}
		}
	}
	catch (const std::exception& e)
	{
		std::cerr << "Request failed, error: " << e.what() << '\n';
	}
	// TODO:  Add extra initialization here

	return TRUE;  // return TRUE unless you set the focus to a control
				  // EXCEPTION: OCX Property Pages should return FALSE
}

void CameraURLSetting::ReadCameraURL(int index)
{
	std::string server_url = mServerURL + "/ReadCameraInfo";
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url);																		  // pass parameters as a map
		std::map<std::string, std::string> parameters = { { "user_id", mUserID },{ "admin_id", mAdminID }, {"camera_id", to_string(index)} };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		
		std::string result = jsonObject["searchlists"];
		
		if (result == "none")
		{
			MessageBox("There is no camera for this user!", "Warning!", MB_OK | MB_ICONQUESTION);
			mCameraType = -1;
		}
		else
		{
			std::vector<std::string> lists = json::parsing::parse_array(result);
			std::vector<json::jobject> results;
			for (size_t i = 0; i < lists.size(); i++) results.push_back(json::jobject::parse(lists[i]));
			if (results.size() < 1) return;
			std::string name = results[0]["camera_name"];
			CString ss(name.c_str());
			mCameraName = ss;

			std::string url = results[0]["camera_url"];
			if (url.length() == 1)
			{
				mWebCameraIndex = atoi(url.c_str());
				mCameraType = 0;
			}
			else
			{
				CString sss(url.c_str());
				mStreamURL = sss;
				mCameraType = 1;
			}
		}
	}
	catch (const std::exception& e)
	{
		std::cerr << "Request failed, error: " << e.what() << '\n';
	}
}


void CameraURLSetting::OnBnClickedRadio1()
{
	// TODO: Add your control notification handler code here
	mCameraType = 0;
	GetDlgItem(IDC_EDIT5)->EnableWindow(false);
	GetDlgItem(IDC_COMBO1)->EnableWindow(false);
	GetDlgItem(IDC_EDIT6)->EnableWindow(false);
	GetDlgItem(IDC_EDIT1)->EnableWindow(true);
}


void CameraURLSetting::OnBnClickedRadio2()
{
	// TODO: Add your control notification handler code here
	mCameraType = 1;
	GetDlgItem(IDC_EDIT5)->EnableWindow(true);
	GetDlgItem(IDC_COMBO1)->EnableWindow(true);
	GetDlgItem(IDC_EDIT6)->EnableWindow(true);
	GetDlgItem(IDC_EDIT1)->EnableWindow(false);
}

std::string CameraURLSetting::getCameraURL()
{
	CT2A ascii(mStreamURL);
	char * streamURL = ascii.m_psz;
	std::string stream_url = std::string(streamURL);
	return stream_url;
}

std::string CameraURLSetting::getCameraName()
{
	CT2A ascii(mCameraName);
	char * cameraName = ascii.m_psz;
	std::string camera_name = std::string(cameraName);
	return camera_name;
}

std::string CameraURLSetting::getUserIDForCameraSetting()
{
	CT2A ascii(mUserIDForCamera);
	char * userid = ascii.m_psz;
	std::string user_id = std::string(userid);
	return user_id;
}

void CameraURLSetting::OnSelchangeCombo1()
{
	int nSel = mUserList.GetCurSel();
	if (nSel != LB_ERR)
	{
		mUserList.GetLBText(nSel, mUserIDForCamera);
		CT2A ascii(mUserIDForCamera);
		char * userid = ascii.m_psz;
		std::string user_id = std::string(userid);
		std::string server_url = mServerURL + "/ReadCameraInfo";
		try
		{
			// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
			http::Request request(server_url);																		  // pass parameters as a map
			std::map<std::string, std::string> parameters = { { "user_id", user_id },{ "admin_id", mAdminID },{ "camera_id", to_string(mCameraIndex) } };
			const http::Response response = request.send("POST", parameters, {
				"Content-Type: application/x-www-form-urlencoded"
			});
			std::string resultString = std::string(response.body.begin(), response.body.end());
			json::jobject jsonObject = json::jobject::parse(resultString);
			
			std::string result = jsonObject["searchlists"];
			
			if (result == "none")
			{
				MessageBox("There is no camera for this user!", "Warning!", MB_OK | MB_ICONQUESTION);
			}
			else
			{

				std::vector<std::string> lists = json::parsing::parse_array(result);
				std::vector<json::jobject> results;
				for (size_t i = 0; i < lists.size(); i++) results.push_back(json::jobject::parse(lists[i]));
				if (results.size() < 1) return;
				std::string name = results[0]["camera_name"];
				CString ss(name.c_str());
				mCameraName = ss;

				std::string url = results[0]["camera_url"];
				CString sss(url.c_str());
				mStreamURL = sss;

				UpdateData(FALSE);
			}
		}
		catch (const std::exception& e)
		{
			std::cerr << "Request failed, error: " << e.what() << '\n';
		}
	}
	// TODO: Add your control notification handler code here
}
