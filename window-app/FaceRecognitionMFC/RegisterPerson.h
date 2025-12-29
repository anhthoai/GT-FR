#pragma once

#include <xstring>
#include "afxwin.h"

#include <opencv2/opencv.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui/highgui.hpp"
// CRegisterPerson dialog

class CRegisterPerson : public CDialogEx
{
	DECLARE_DYNAMIC(CRegisterPerson)

public:
	CRegisterPerson(CWnd* pParent = NULL);   // standard constructor
	virtual ~CRegisterPerson();

	void setAdminID(std::string adminid)
	{
		mAdminID = adminid;
	}
	void setServerURL(std::string url)
	{
		mServerURL = url;
	}
	void setUserID(std::string userid)
	{
		mUserID = userid;
	}
	void setImage(cv::Mat image)
	{
		image.copyTo(person_image);
	}
	void setRegion(cv::Rect r)
	{
		face_rect = r;
	}
	void setFeature(std::string f)
	{
		feature = f;
	}

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_REGISTER_PERSON };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();

private:
	std::string mAdminID;
	std::string mServerURL;
	std::string mUserID;
	cv::Mat person_image;
	cv::Rect face_rect;
	std::string feature;
public:
	afx_msg void OnBnClickedRegisterClose();
	CComboBox mGroupList;
	CString mGroupName;
	afx_msg void OnBnClickedRegister();
	afx_msg void OnBnClickedShowImage();
	CString mPersonName;
	CString mPersonEmail;
	CString mPersonPhone;
};
