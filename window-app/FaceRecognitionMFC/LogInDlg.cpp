// LogInDlg.cpp : implementation file
//
#pragma once
#include "stdafx.h"
#include "FaceRecognitionMFC.h"
#include "FaceRecognitionMFCDlg.h"
#include "LogInDlg.h"
#include "afxdialogex.h"
#include <windows.h>
#include <string>
#include <xstring>
#include <stdio.h>
#include <iostream>
#include <fstream>
#include "HTTPRequest.hpp"
#include "json.hpp"

using namespace std;

std::string admin_config;

inline bool file_exists(const std::string& name) {
	struct stat buffer;
	return (stat(name.c_str(), &buffer) == 0);
}

// LogInDlg dialog

IMPLEMENT_DYNAMIC(LogInDlg, CDialogEx)

LogInDlg::LogInDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_LOGIN, pParent)
	, mServerURL(_T(""))
	, mUserID(_T(""))
	, mPassword(_T(""))
{
	mAdminID = "";
	mGroupToPush.clear();
	login = false;
}

LogInDlg::~LogInDlg()
{
}

void LogInDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT4, mServerURL);
	DDX_Text(pDX, IDC_EDIT2, mUserID);
	DDX_Text(pDX, IDC_EDIT3, mPassword);
}


BEGIN_MESSAGE_MAP(LogInDlg, CDialogEx)
	ON_BN_CLICKED(IDC_LOG_IN, &LogInDlg::OnBnClickedLogIn)
	ON_BN_CLICKED(IDC_CLOSE_BTN, &LogInDlg::OnBnClickedCloseBtn)
END_MESSAGE_MAP()


// LogInDlg message handlers
BOOL LogInDlg::OnInitDialog()
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

	mServerURL = _T("http://localhost:8080/FDRServer/");
	UpdateData(FALSE);

	char chRootDir[MAX_PATH];
	GetModuleFileName(NULL, chRootDir, MAX_PATH);
	char *p = strrchr(chRootDir, '\\');
	p++; *p = '\0';
	std::string imgDir = std::string(chRootDir) + "/res/login.bmp";

	CImage img1;
	CRect rect;
	this->GetClientRect(&rect);

	int dimx = rect.Width(), dimy = rect.Height();
	img1.Load(imgDir.c_str());
	//filename = path on local system to the bitmap

	CDC *screenDC = GetDC();
	CDC mDC;
	mDC.CreateCompatibleDC(screenDC);
	CBitmap b;
	b.CreateCompatibleBitmap(screenDC, dimx, dimy);

	CBitmap *pob = mDC.SelectObject(&b);
	mDC.SetStretchBltMode(HALFTONE);
	img1.StretchBlt(mDC.m_hDC, 0, 0, dimx, dimy, 0, 0, img1.GetWidth(), img1.GetHeight(), SRCCOPY);
	mDC.SelectObject(pob);

	SetBackgroundImage((HBITMAP)b.Detach(), BACKGR_TOPLEFT, TRUE);
	ReleaseDC(screenDC);

	admin_config = std::string(chRootDir) + "/config.txt";
	if (!file_exists(admin_config))
	{
		mAdminID = "";
	}
	else
	{
		std::ifstream file(admin_config);
		std::string line;
		while (std::getline(file, line))
		{
			std::string read_line = std::string(line);
			std::string delimiter = "=";
			std::string key = read_line.substr(0, read_line.find(delimiter));
			if (key == "adminid")
			{
				mAdminID = read_line.substr(read_line.find(delimiter) + 1, read_line.length() - read_line.find(delimiter));
			}
			else if (key == "groupToPush")
			{
				std::string group_str = read_line.substr(read_line.find(delimiter) + 1, read_line.length() - read_line.find(delimiter));
				std::string delimiter1 = " ";
				size_t pos = 0;
				std::string group;
				while ((pos = group_str.find(delimiter1)) != std::string::npos) {
					group = group_str.substr(0, pos);
					mGroupToPush.push_back(group);
					group_str.erase(0, pos + delimiter1.length());
				}
			}
		}

	}
	return TRUE;  // return TRUE  unless you set the focus to a control
}


void LogInDlg::OnBnClickedLogIn()
{
	// TODO: Add your control notification handler code here
	
	UpdateData(TRUE);

	CT2A ascii(mServerURL);
	char * serverURL = ascii.m_psz;
	std::string server_url = std::string(serverURL);

	CT2A user_id(mUserID);
	CT2A password(mPassword);
	std::string userID(user_id.m_psz);
	std::string passWord(password.m_psz);
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url + "/AgentLogin?mobile=0");																		  // pass parameters as a map
		std::map<std::string, std::string> parameters = { { "user_id", userID},{ "password", passWord } };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		std::string resultString1 = jsonObject["searchlists"];
		if (resultString1 == "none")
		{
			MessageBox("LogIn failed. You are unregistered user!", "Warning!", MB_OK | MB_ICONQUESTION);
			return;
		}
		std::vector<std::string> lists = json::parsing::parse_array(resultString1);
		std::vector<json::jobject> results;
		for (size_t i = 0; i < lists.size(); i++) results.push_back(json::jobject::parse(lists[i]));
		
		if (results.size() > 0)
		{
			std::string user_id = "";
			std::string admin_id = "";
			bool isAdmin = false;
			for (int i = 0; i < results.size(); i++)
			{
				std::string userid = results[i]["user_id"];
				std::string member = results[i]["member"];
				std::string adminid = results[i]["adminid"];
				if (member == "admin")
				{
					user_id = userid;
					admin_id = adminid;
					isAdmin = true;
					break;
				}
			}
			if (isAdmin)
			{
				std::string outfile(admin_config);
				ofstream fs1;
				fs1.open(outfile);
				fs1 << "adminid=" << admin_id << endl;
				if (mAdminID != admin_id)
				{
					MessageBox("You are different admin. Please set group to push notification!", "Warning!", MB_OK | MB_ICONQUESTION);
					mGroupToPush.clear();
				}
				else
				{
					if (mGroupToPush.size() > 0)
					{
						std::string group_str = "";
						for (int i = 0; i < mGroupToPush.size(); i++)
						{
							group_str += mGroupToPush[i];
						}
						fs1 << "groupToPush=" << group_str;
					}
				}
				
				fs1.close();
				login = true;

				m_ServerURL = server_url;
				m_MemberType = "admin";
				mAdminID = admin_id;
				m_UserID = user_id;
				MessageBox("Successfully logged in! Pleae wait....", "Success!", MB_OK | MB_ICONQUESTION);
				EndDialog(-1);
				
			}
			else
			{
				if (mAdminID == "")
				{
					MessageBox("LogIn failed. Please set admin id by login with admin!", "Error!", MB_OK | MB_ICONQUESTION);
					return;
				}
				bool autherized = false;
				for (int i = 0; i < results.size(); i++)
				{
					std::string userid = results[i]["user_id"];
					std::string member = results[i]["member"];
					std::string adminid = results[i]["adminid"];
					if (adminid == mAdminID)
					{
						user_id = userid;
						admin_id = mAdminID;
						autherized = true;
						break;
					}
				}
				if (autherized == true)
				{
					m_ServerURL = server_url;
					m_MemberType = "user";
					mAdminID = admin_id;
					m_UserID = user_id;

					login = true;
					MessageBox("Successfully logged in! Pleae wait....", "Success!", MB_OK | MB_ICONQUESTION);
					EndDialog(-1);
				}
				else
				{
					MessageBox("LogIn failed. There is no this user id for current admin id!", "Warning!", MB_OK | MB_ICONQUESTION);
				}
			}
		}
		else
		{
			MessageBox("LogIn failed. You are unregistered user!", "Warning!", MB_OK | MB_ICONQUESTION);
		}
	}
	catch (const std::exception& e)
	{
		std::cerr << "Request failed, error: " << e.what() << '\n';
	}
}


void LogInDlg::OnBnClickedCloseBtn()
{
	// TODO: Add your control notification handler code here
	login = false;
	EndDialog(-1);
}
