
// FaceRecognitionMFCDlg.h : header file
//

#pragma once
#include "afxwin.h"
#include "CameraURLSetting.h"
#include <xstring>
#include <vector>
#include "afxcmn.h"

using namespace std;


// CFaceRecognitionMFCDlg dialog
class CFaceRecognitionMFCDlg : public CDialogEx
{
// Construction
public:
	CFaceRecognitionMFCDlg(CWnd* pParent = NULL);	// standard constructor

	static UINT thread_func1(LPVOID param);
	static UINT thread_func2(LPVOID param);
	static UINT thread_func3(LPVOID param);
	static UINT thread_func4(LPVOID param);
	static UINT thread_recognition(LPVOID param);
// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_FACERECOGNITIONMFC_DIALOG };
#endif

	typedef struct THREADSTRUCT
	{
		CFaceRecognitionMFCDlg*    _this;
		//you can add here other parameters you might be interested on
	} THREADSTRUCT;

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedLive1RunBtn();
	CButton mLive1RunBtn;
	afx_msg void OnBnClickedLive2RunBtn();
	CButton mLive2RunBtn;
	CButton mLive3RunBtn;
	CButton mLive4RunBtn;
	afx_msg void OnBnClickedLive3RunBtn();
	afx_msg void OnBnClickedLive4RunBtn();
	afx_msg void OnClose();
	afx_msg BOOL OnEraseBkgnd(CDC* pDC);
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);

	CameraURLSetting mCameraURLSetting;
	std::string mServerURL;
	std::string mMemberType;
	std::string mAdminID;
	std::string mUserID;
	std::vector<std::string> mGroupToPush;
	CListCtrl mUnRecognizedFace;
	CListCtrl mRecognizedFace;

	CImageList m_RecognizedFaceList;
	CImageList m_UnRecognizedFaceList;
	afx_msg void OnBnClickedClose();
	CButton mCloseBtn;
	CButton mSettingBtn;
	afx_msg void OnBnClickedSetting();
};
