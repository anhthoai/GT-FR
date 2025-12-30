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

@WebServlet("/UserCreate")
public class UserCreate extends HttpServlet {
	String root_path="";
	private Connection con;
	public UserCreate() {
        super();       
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
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
		String admin_id = (String) request.getParameter("admin-id");
		String create_userid = (String) request.getParameter("create-userid");
		String fullname = (String) request.getParameter("create-fullname");
		String email = (String) request.getParameter("create-email");
		String address = (String) request.getParameter("create-address");
		String city = (String) request.getParameter("create-city");
		String country = (String) request.getParameter("create-country");
		String new_password = request.getParameter("create-new_password");
		String fileID = request.getParameter("create-user-fileid");
		String user_image_path = root_path + "data/" +userid+"/request/"+fileID+"_cr.jpg";
		con=MySQLConfig.getConnection();
		ResultSet rs;
		PrintWriter out = response.getWriter();
		try {
			String query = "select * from users_info where userid='"+admin_id+"' AND member='"+"admin"+"'";
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (!rs.first())
			{
				out.print("doesn't exist admin for creating user");
				return;
			}
		
		} catch (SQLException e) {
			out.print("fail");
			e.printStackTrace();
			return;
		} catch (org.json.JSONException e){
			out.print("fail");
			e.printStackTrace();
			return;
		}
		
		try {
			String query = "select * from users_info where userid='"+create_userid+"' AND member='"+"user"+"'" + " AND adminid='" + admin_id + "'";
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			int i = 0 ;
			while (rs.next() == true)
			{
				i++;
			}
			if (i > 0)
			{
				out.print("duplicate");
				return;
			}
		
		} catch (SQLException e) {
			out.print("fail");
			e.printStackTrace();
			return;
		} catch (org.json.JSONException e){
			out.print("fail");
			e.printStackTrace();
			return;
		}
		
		PreparedStatement pstmt;
		String json_str="{create_new_user : ok}";
		File in_f= new File(user_image_path);

		try {
			if (!fileID.equals("0"))
			{
				byte[] new_user_image;
				try {
					DataInputStream distream;
					distream = globalUtil.getInputStream(user_image_path);
					new_user_image=new byte[(int)in_f.length()];
					distream.read(new_user_image);
					distream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						out.print("fail");
						return;
					}
				String query = "INSERT INTO users_info (fullname, email, address, city, country, userid, password, user_img, adminid, member, status)"
						   +" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					
					pstmt = con.prepareStatement(query);
					pstmt.setString(1,fullname);
					pstmt.setString(2,email);
					pstmt.setString(3,address);
					pstmt.setString(4, city);
					pstmt.setString(5, country);
					pstmt.setString(6, create_userid);
					pstmt.setString(7,new_password);
					pstmt.setBytes(8, new_user_image);
					pstmt.setString(9, admin_id);
					pstmt.setString(10,"user");
					pstmt.setInt(11, 1);
					pstmt.executeUpdate();
			}
			else
			{
				String query = "INSERT INTO users_info (fullname, email, address, city, country, userid, password, adminid, member, status)"
						   +" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					
					pstmt = con.prepareStatement(query);
					pstmt.setString(1,fullname);
					pstmt.setString(2,email);
					pstmt.setString(3,address);
					pstmt.setString(4, city);
					pstmt.setString(5, country);
					pstmt.setString(6, create_userid);
					pstmt.setString(7,new_password);
					pstmt.setString(8, admin_id);
					pstmt.setString(9,"user");
					pstmt.setInt(10, 1);
					pstmt.executeUpdate();
			}
				
				
				out.print("success");
			} catch (SQLException e) {
				out.print("fail");
				e.printStackTrace();
			} catch (org.json.JSONException e){
				LogIn.m_logger.debug(json_str);
				e.printStackTrace();	
			}
		
	}
}
