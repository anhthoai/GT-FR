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

@WebServlet("/UserEdit")
public class UserEdit extends HttpServlet {
	String root_path="";
	private MySQLConnection con;
	public UserEdit() {
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
		String cur_userid = (String) session.getAttribute("cur_userid");

		if (cur_userid==null) {
			cur_userid = (String) request.getParameter("cur_userid");// due to mobile
		}
		if (cur_userid==null) {
			request.setAttribute("Error!", "Session losted!!!");
			RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
			mDispatcher.forward(request, response);
			return;
		}
		String new_fullname = (String) request.getParameter("fullname");
		String new_email = (String) request.getParameter("email");
		String new_address = (String) request.getParameter("address");
		String new_city = (String) request.getParameter("city");
		String new_userid = (String) request.getParameter("userid");
		String new_country = (String) request.getParameter("country");
		String cur_password = request.getParameter("cur_password");
		String new_password = request.getParameter("new_password");
		String fileID = request.getParameter("user-fileid");
		String new_user_image_path = root_path + "data/" +cur_userid+"/request/"+fileID+"_cr.jpg";
		con=MySQLConfig.getConnection();
		PreparedStatement pstmt;

		PrintWriter out = response.getWriter();
		
		byte[] new_user_image;
		
		
		ResultSet rs;
		String member = "";
		String adminid = "";
		try {
				String query = "select * from users_info where userid like '%"+cur_userid+"%' AND password='"+cur_password+"'";
				Statement stmt = con.createStatement();
				rs = stmt.executeQuery(query);
				if (!rs.first())
				{
					out.print("incorrect");
					return;
				}
				member = rs.getString("member");
				adminid = rs.getString("adminid");
				if (!fileID.equals("0")) {
					try {
						DataInputStream distream;
						File in_f= new File(new_user_image_path);
						distream = globalUtil.getInputStream(new_user_image_path);
						new_user_image=new byte[(int)in_f.length()];
						distream.read(new_user_image);
						distream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							out.print("fail");
							return;
						}
					try {
						query = "UPDATE users_info SET fullname = ?, email = ?, address = ?, city = ?, country = ?, userid = ?," 
							   +" password = ?, user_img = ?, adminid = ?, member = ?, status = ? WHERE userid = ? AND password = ?";
						
						pstmt = con.prepareStatement(query);
						pstmt.setString(1,new_fullname);
						pstmt.setString(2,new_email);
						pstmt.setString(3,new_address);
						pstmt.setString(4, new_city);
						pstmt.setString(5, new_country);
						pstmt.setString(6, new_userid);
						pstmt.setString(7,new_password);
						pstmt.setBytes(8, new_user_image);
						pstmt.setString(9, adminid);
						pstmt.setString(10, member);
						pstmt.setInt(11, 1);
						pstmt.setString(12, cur_userid);
						pstmt.setString(13, cur_password);
						pstmt.executeUpdate();
						
						out.print("success");
					} catch (SQLException e) {
						out.print("fail");
						e.printStackTrace();
					} catch (org.json.JSONException e){
						out.print("fail");
						e.printStackTrace();
					}
				}
				else
				{
					byte[] image = rs.getBytes("user_img");
					if (image != null)
					{
						try {
							query = "UPDATE users_info SET fullname = ?, email = ?, address = ?, city = ?, country = ?, userid = ?," 
								   +" password = ?, user_img = ?, adminid = ?, member = ?, status = ? WHERE userid = ? AND password = ?";
							
							pstmt = con.prepareStatement(query);
							pstmt.setString(1,new_fullname);
							pstmt.setString(2,new_email);
							pstmt.setString(3,new_address);
							pstmt.setString(4, new_city);
							pstmt.setString(5, new_country);
							pstmt.setString(6, new_userid);
							pstmt.setString(7,new_password);
							pstmt.setBytes(8, rs.getBytes("user_img"));
							pstmt.setString(9, adminid);
							pstmt.setString(10, member);
							pstmt.setInt(11, 1);
							pstmt.setString(12, cur_userid);
							pstmt.setString(13, cur_password);
							pstmt.executeUpdate();
							
							out.print("success");
						} catch (SQLException e) {
							out.print("fail");
							e.printStackTrace();
						} catch (org.json.JSONException e){
							out.print("fail");
							e.printStackTrace();
						}
					}
					else
					{
						try {
							query = "UPDATE users_info SET fullname = ?, email = ?, address = ?, city = ?, country = ?, userid = ?," 
								   +" password = ?, adminid = ?, member = ?, status = ? WHERE userid = ? AND password = ?";
							
							pstmt = con.prepareStatement(query);
							pstmt.setString(1,new_fullname);
							pstmt.setString(2,new_email);
							pstmt.setString(3,new_address);
							pstmt.setString(4, new_city);
							pstmt.setString(5, new_country);
							pstmt.setString(6, new_userid);
							pstmt.setString(7,new_password);
							pstmt.setString(8, adminid);
							pstmt.setString(9, member);
							pstmt.setInt(10, 1);
							pstmt.setString(11, cur_userid);
							pstmt.setString(12, cur_password);
							pstmt.executeUpdate();
							
							out.print("success");
						} catch (SQLException e) {
							out.print("fail");
							e.printStackTrace();
						} catch (org.json.JSONException e){
							out.print("fail");
							e.printStackTrace();
						}
					}
				}
			
			} catch (SQLException e) {
				out.print("fail");
				e.printStackTrace();
			} catch (org.json.JSONException e){
				out.print("fail");
				e.printStackTrace();
			}
		
		
	}
}
