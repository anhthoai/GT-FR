package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.json.JSONObject;

import java.sql.Connection;
import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

@WebServlet("/PersonEdit")
public class PersonEdit extends HttpServlet {
	String feature_fpath="";
	String cropimg_fpath="";
	String exe_path="";
	String root_path="";

	String feature_str="";
	byte[] rsrc_imgbytes;
	String feature_str1="";
	byte[] rsrc_imgbytes1;
	String feature_str2="";
	byte[] rsrc_imgbytes2;
	String feature_str3="";
	byte[] rsrc_imgbytes3;
	private Connection con;
	public PersonEdit() {
        super();       
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	feature_fpath=root_path+"\\externalexec\\register_feature.txt";
    	exe_path=root_path+"\\externalexec\\OpenVinoFaceEngine.exe";
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String userid = (String) session.getAttribute("userid");

		if (userid==null) {
			userid = (String) request.getParameter("userid");// due to mobile
		}
		if (userid==null) {
			request.setAttribute("Error!", "Session losted!!!");
			RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
			mDispatcher.forward(request, response);
			return;
		}
		String name = (String) request.getParameter("name");
		String new_email = (String) request.getParameter("email");
		String sex = (String) request.getParameter("sex");
		String birthday = (String) request.getParameter("birthday");
		String new_home = (String) request.getParameter("home");
		String new_country = (String) request.getParameter("country");
		String new_phone = request.getParameter("phone");
		String new_city = request.getParameter("city");
		String fileID = request.getParameter("file_id");
		String fileID1 = request.getParameter("file_id1");
		String fileID2 = request.getParameter("file_id2");
		String fileID3 = request.getParameter("file_id3");
		String group_name = (String) request.getParameter("group_name");
		int id = Integer.parseInt(request.getParameter("id"));	
		
		con=MySQLConfig.getConnection();
		PreparedStatement pstmt;

		PrintWriter out = response.getWriter();

		
		if(fileID != null && fileID != "0"){
			String register_imgpath=root_path+"\\data\\"+userid+"\\request\\"+fileID+"_cr.jpg";
	       	File f;
	       	deletetmpfiles();
			String[] args = new String [3];
			args[0] = exe_path;					//input exe path
			args[1] = register_imgpath;//input src imgpath
			args[2] = feature_fpath;	//feature
			//args[3] = register_imgpath;	//input output imgpath
			//args[4] = "0*0*0*0*-0.0";			//crop size, angle
			//args[5] = root_path+"\\externalexec\\landmark.txt";	//landmark
			//args[6] = feature_fpath;	//feature
			Process proc;
			try {
				proc = Runtime.getRuntime().exec(args);
				proc.waitFor();
				
				f=new File(feature_fpath);
				
				if (f.exists()==true){
					DataInputStream dis = globalUtil.getInputStream(feature_fpath);
					feature_str= dis.readLine().trim();
					dis.close();
					DataInputStream distream;
					File in_f= new File(register_imgpath);
					try {
						distream = globalUtil.getInputStream(register_imgpath);
						rsrc_imgbytes=new byte[(int)in_f.length()];
						distream.read(rsrc_imgbytes);
						distream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							String json_str="{personeditlists : fail}";
							JSONObject obj = new JSONObject(json_str);
							out.print(obj);
							return;
						}
				}
				else{
					String json_str="{personeditlists : nofeature}";
					JSONObject obj = new JSONObject(json_str);
					out.print(obj);
					LogIn.m_logger.info("PersonEdit.java" + json_str);
					return;
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : fail}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : none}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			}

		}
		
		if(fileID1 != null && fileID1 != "0"){
			String register_imgpath=root_path+"\\data\\"+userid+"\\request\\"+fileID1+"_cr.jpg";
	       	File f;
	       	deletetmpfiles();
			String[] args = new String [3];
			args[0] = exe_path;					//input exe path
			args[1] = register_imgpath;//input src imgpath
			args[2] = feature_fpath;	//feature
			//args[3] = register_imgpath;	//input output imgpath
			//args[4] = "0*0*0*0*-0.0";			//crop size, angle
			//args[5] = root_path+"\\externalexec\\landmark.txt";	//landmark
			//args[6] = feature_fpath;	//feature
			Process proc;
			try {
				proc = Runtime.getRuntime().exec(args);
				proc.waitFor();
				
				f=new File(feature_fpath);
				
				if (f.exists()==true){
					DataInputStream dis = globalUtil.getInputStream(feature_fpath);
					feature_str1= dis.readLine().trim();
					dis.close();
					DataInputStream distream;
					File in_f= new File(register_imgpath);
					try {
						distream = globalUtil.getInputStream(register_imgpath);
						rsrc_imgbytes1=new byte[(int)in_f.length()];
						distream.read(rsrc_imgbytes1);
						distream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							String json_str="{personeditlists : fail}";
							JSONObject obj = new JSONObject(json_str);
							out.print(obj);
							return;
						}
				}
				else{
					String json_str="{personeditlists : nofeature}";
					JSONObject obj = new JSONObject(json_str);
					out.print(obj);
					LogIn.m_logger.info("PersonEdit.java" + json_str);
					return;
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : fail}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : none}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			}

		}
		
		
		if(fileID2 != null && fileID2 != "0"){
			String register_imgpath=root_path+"\\data\\"+userid+"\\request\\"+fileID2+"_cr.jpg";
	       	File f;
	       	deletetmpfiles();
			String[] args = new String [3];
			args[0] = exe_path;					//input exe path
			args[1] = register_imgpath;//input src imgpath
			args[2] = feature_fpath;	//feature
			//args[3] = register_imgpath;	//input output imgpath
			//args[4] = "0*0*0*0*-0.0";			//crop size, angle
			//args[5] = root_path+"\\externalexec\\landmark.txt";	//landmark
			//args[6] = feature_fpath;	//feature
			Process proc;
			try {
				proc = Runtime.getRuntime().exec(args);
				proc.waitFor();
				
				f=new File(feature_fpath);
				
				if (f.exists()==true){
					DataInputStream dis = globalUtil.getInputStream(feature_fpath);
					feature_str2= dis.readLine().trim();
					dis.close();
					DataInputStream distream;
					File in_f= new File(register_imgpath);
					try {
						distream = globalUtil.getInputStream(register_imgpath);
						rsrc_imgbytes2=new byte[(int)in_f.length()];
						distream.read(rsrc_imgbytes2);
						distream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							String json_str="{personeditlists : fail}";
							JSONObject obj = new JSONObject(json_str);
							out.print(obj);
							return;
						}
				}
				else{
					String json_str="{personeditlists : nofeature}";
					JSONObject obj = new JSONObject(json_str);
					out.print(obj);
					LogIn.m_logger.info("PersonEdit.java" + json_str);
					return;
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : fail}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				String json_str="{personeditlists : none}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("PersonEdit.java" + json_str);
				return;
			}

		}
		
		
		if(fileID3 != null && fileID3 != "0"){
			String register_imgpath=root_path+"\\data\\"+userid+"\\request\\"+fileID3+"_cr.jpg";
			DataInputStream distream;
			File in_f= new File(register_imgpath);
			try {
				distream = globalUtil.getInputStream(register_imgpath);
				rsrc_imgbytes3=new byte[(int)in_f.length()];
				distream.read(rsrc_imgbytes3);
				distream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					String json_str="{personeditlists : fail}";
					JSONObject obj = new JSONObject(json_str);
					out.print(obj);
					return;
				}
		}
		
		ResultSet rs;
		String member = "";
		String adminid = "";
		try {
			String query ="UPDATE person_info SET home = ?, city = ?, country = ?, email = ?, phone = ?, name = ?, sex = ? , birthday = ?, group_name=? WHERE id = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1,new_home);
			pstmt.setString(2,new_city);
			pstmt.setString(3,new_country);
			pstmt.setString(4, new_email);
			pstmt.setString(5, new_phone);
			pstmt.setString(6, name);
			pstmt.setString(7, sex);
			pstmt.setString(8, birthday);
			pstmt.setString(9, group_name);
			pstmt.setInt(10, id);
			pstmt.executeUpdate();
				
			if (fileID != null)
			{
				query = "UPDATE person_info SET person_img = ?, feature = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setBytes(1, rsrc_imgbytes);
				pstmt.setString(2,feature_str);
				pstmt.setInt(3, id);
				pstmt.executeUpdate();
			}
			
			if (fileID1 != null)
			{
				query = "UPDATE person_info SET person_img1 = ?, feature1 = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setBytes(1, rsrc_imgbytes1);
				pstmt.setString(2,feature_str1);
				pstmt.setInt(3, id);
				pstmt.executeUpdate();
			}
			
			if (fileID2 != null)
			{
				query = "UPDATE person_info SET person_img2 = ?, feature2 = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setBytes(1, rsrc_imgbytes2);
				pstmt.setString(2,feature_str2);
				pstmt.setInt(3, id);
				pstmt.executeUpdate();
			}
			
			if (fileID3 != null)
			{
				query = "UPDATE person_info SET person_full_img = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setBytes(1, rsrc_imgbytes3);
				pstmt.setInt(2, id);
				pstmt.executeUpdate();
			}
			
			String json_str="{personeditlists : ok}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("PersonEdit.java" + json_str);
			
		} catch (SQLException e) {
			String json_str="{personeditlists : fail}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
		} catch (org.json.JSONException e){
			String json_str="{personeditlists : fail}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
		}
	}
	
	public void deletetmpfiles(){
    	File f=new File(feature_fpath);
		if (f.isFile()==true) f.delete();
		f=new File(cropimg_fpath);
    }
}
