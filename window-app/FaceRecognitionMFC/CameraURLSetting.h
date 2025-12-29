#pragma once
#include <xstring>
#include "afxwin.h"

// CameraURLSetting dialog

class CameraURLSetting : public CDialogEx
{
	DECLARE_DYNAMIC(CameraURLSetting)

public:
	CameraURLSetting(CWnd* pParent = NULL);   // standard constructor
	virtual ~CameraURLSetting();

	void setServerURL(std::string url) { mServerURL = url; }
	void setMemberType(std::string member) { mMemberType = member; }
	void setAdminID(std::string adminid) { mAdminID = adminid; }
	void setUserID(std::string userid) { mUserID = userid; }
	void setCameraIndex(int index) { mCameraIndex = index; }

	std::string getCameraURL();
	std::string getCameraName();
	std::string getUserIDForCameraSetting();
	int getCameraType() { return mCameraType; }
	int getWebCameraIndex()
	{
		return mWebCameraIndex;
	}

	void ReadCameraURL(int index);


// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_CAMERAURL_SETTING };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CString mCameraName;
	int mWebCameraIndex;
	CString mStreamURL;
	afx_msg void OnBnClickedOk();
	afx_msg void OnBnClickedCancel();


	std::string mServerURL;
	std::string mMemberType;
	std::string mAdminID;
	std::string mUserID;
	virtual BOOL OnInitDialog();
	int mCameraType;
	CComboBox mUserList;
	afx_msg void OnBnClickedRadio1();
	afx_msg void OnBnClickedRadio2();

	int mCameraIndex;
	CString mUserIDForCamera;
	afx_msg void OnSelchangeCombo1();
};
