// SettingDlg.cpp : implementation file
//

#include "stdafx.h"
#include "FaceRecognitionMFC.h"
#include "SettingDlg.h"
#include "afxdialogex.h"
#include "HTTPRequest.hpp"
#include "json.hpp"
#include <iostream>

// CSettingDlg dialog

IMPLEMENT_DYNAMIC(CSettingDlg, CDialogEx)

CSettingDlg::CSettingDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(IDD_SETTING, pParent)
	, mGroupNameToPush(_T(""))
{

}

CSettingDlg::~CSettingDlg()
{
}

void CSettingDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_GROUP_COMBO, mGroupList);
	DDX_CBString(pDX, IDC_GROUP_COMBO, mGroupNameToPush);
	DDX_Control(pDX, IDC_GROUP_LIST, mSelectedGroupList);
}


BEGIN_MESSAGE_MAP(CSettingDlg, CDialogEx)
	ON_CBN_SELCHANGE(IDC_GROUP_COMBO, &CSettingDlg::OnCbnSelchangeGroupCombo)
END_MESSAGE_MAP()


// CSettingDlg message handlers


BOOL CSettingDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// TODO:  Add extra initialization here
	UpdateData(TRUE);

	mGroupList.Clear();
	mGroupList.AddString(_T("Unregistered"));
	mGroupList.AddString(_T("Nogroup"));

	std::string server_url = mServerURL + "/GroupSearch";
	try
	{
		// you can pass http::InternetProtocol::V6 to Request to make an IPv6 request
		http::Request request(server_url);																		  // pass parameters as a map
		std::map<std::string, std::string> parameters = { { "userid", "" },{ "adminid", mAdminID } };
		const http::Response response = request.send("POST", parameters, {
			"Content-Type: application/x-www-form-urlencoded"
		});
		std::string resultString = std::string(response.body.begin(), response.body.end());
		json::jobject jsonObject = json::jobject::parse(resultString);
		std::string result_str = jsonObject["searchlists"];

		if (result_str == "none")
		{
			MessageBox("No group to push notification for current admin!", "Warning!", MB_OK | MB_ICONQUESTION);
		}
		else
		{
			std::vector<std::string> lists = json::parsing::parse_array(result_str);
			std::vector<json::jobject> results;
			for (size_t i = 0; i < lists.size(); i++) results.push_back(json::jobject::parse(lists[i]));
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
	//mGroupList.SetCurSel(0);
	//mGroupList.GetLBText(0, mGroupNameToPush);
	group_lists.clear();
	return TRUE;  // return TRUE unless you set the focus to a control
				  // EXCEPTION: OCX Property Pages should return FALSE
}

std::vector<std::string> CSettingDlg::getGroupName()
{
	return group_lists;
}

void CSettingDlg::OnCbnSelchangeGroupCombo()
{
	// TODO: Add your control notification handler code here
	int nSel = mGroupList.GetCurSel();
	if (nSel != LB_ERR)
	{
		mGroupList.GetLBText(nSel, mGroupNameToPush);
		int index = mSelectedGroupList.GetCount();
		int item = mSelectedGroupList.InsertString(index, mGroupNameToPush);
		CT2A ascii(mGroupNameToPush);
		char * group_str = ascii.m_psz;
		std::string group_string = std::string(group_str);
		group_lists.push_back(group_string);
	}
	
}
