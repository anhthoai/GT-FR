#pragma once
#include "afxwin.h"

#include <xstring>
#include <vector>

// CSettingDlg dialog

class CSettingDlg : public CDialogEx
{
	DECLARE_DYNAMIC(CSettingDlg)

public:
	CSettingDlg(CWnd* pParent = NULL);   // standard constructor
	virtual ~CSettingDlg();

	void setServerURL(std::string url) { mServerURL = url; }
	void setAdminID(std::string adminid) { mAdminID = adminid; }

	std::vector<std::string> getGroupName();
// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_SETTING };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox mGroupList;
	CString mGroupNameToPush;
	virtual BOOL OnInitDialog();

public:
	std::string mServerURL;
	std::string mAdminID;
	afx_msg void OnCbnSelchangeGroupCombo();
	CListBox mSelectedGroupList;

	std::vector<std::string> group_lists;
};
