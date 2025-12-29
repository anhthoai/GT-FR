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

import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

@WebServlet("/AgentEdit")
public class AgentEdit extends HttpServlet {
	String feature_fpath="";
	String cropimg_fpath="";
	String exe_path="";
	String root_path="";

	byte[] src_imgbytes;

	private MySQLConnection con;
	public AgentEdit() {
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
		String new_name = (String) request.getParameter("fullname");
		String new_email = (String) request.getParameter("email");
		String new_address = (String) request.getParameter("address");
		String new_country = (String) request.getParameter("country");
		String new_city = request.getParameter("city");
		String new_adminid = request.getParameter("adminid");
		String fileID = request.getParameter("fildid");
		String new_password = request.getParameter("password");
		int id = Integer.parseInt(request.getParameter("id"));	
		
		con=MySQLConfig.getConnection();
		PreparedStatement pstmt;

		PrintWriter out = response.getWriter();

		if(fileID != null && fileID != "0"){
			String imgpath=root_path+"\\data\\"+userid+"\\request\\"+fileID+"_cr.jpg";
			DataInputStream distream;
			File in_f= new File(imgpath);
			try {
				distream = globalUtil.getInputStream(imgpath);
				src_imgbytes=new byte[(int)in_f.length()];
				distream.read(src_imgbytes);
				distream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					String json_str="{personeditlists : fail}";
					JSONObject obj = new JSONObject(json_str);
					out.print(obj);
					return;
				}
		}
		
		
		try {
			String query = "UPDATE users_info SET fullname = ?, email = ?, address = ?, city = ?, country = ?, userid = ? WHERE id = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1,new_name);
			pstmt.setString(2,new_email);
			pstmt.setString(3,new_address);
			pstmt.setString(4, new_city);
			pstmt.setString(5, new_country);
			pstmt.setString(6, new_adminid);
			pstmt.setInt(7, id);
			pstmt.executeUpdate();
				
			if (fileID != null)
			{
				query = "UPDATE users_info SET user_img = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setBytes(1, src_imgbytes);
				pstmt.setInt(2, id);
				pstmt.executeUpdate();
			}
			
			if (new_password != null)
			{
				query = "UPDATE users_info SET password = ? WHERE id = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, new_password);
				pstmt.setInt(2, id);
				pstmt.executeUpdate();
			}

			out.print("success");
			LogIn.m_logger.info("AgentEdit.java" + ": success");
			
		} catch (SQLException e) {
			out.print("fail");
			e.printStackTrace();
		} catch (org.json.JSONException e){
			out.print("fail");
			e.printStackTrace();
		}
	}
}
