#pragma once

#include <vector>

// LogInDlg dialog

class LogInDlg : public CDialogEx
{
	DECLARE_DYNAMIC(LogInDlg)

public:
	LogInDlg(CWnd* pParent = NULL);   // standard constructor
	virtual ~LogInDlg();

	bool getLogIn() { return login; }
	std::string getServerURL() { return m_ServerURL; }
	std::string getAdminID() { return mAdminID; }
	std::string getUserID() { return m_UserID; }
	std::string getMemberType() { return m_MemberType; }
	std::vector<std::string> getGroupToPush() { return mGroupToPush; }

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_LOGIN };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	DECLARE_MESSAGE_MAP()
public:
	CString mServerURL;
	std::string m_ServerURL;
	std::string m_UserID;
	std::string m_MemberType;
	afx_msg void OnBnClickedLogIn();
	CString mUserID;
	CString mPassword;
	afx_msg void OnBnClickedCloseBtn();

	std::string mAdminID;
	std::vector<std::string> mGroupToPush;

	bool login;
};
