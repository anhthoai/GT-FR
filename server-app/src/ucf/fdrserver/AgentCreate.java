package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
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

@WebServlet("/AgentCreate")
public class AgentCreate extends HttpServlet {
	String root_path="";
	private Connection con;
	public AgentCreate() {
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
		boolean is_sel_photo=false;
		if (userid==null) {
			userid = (String) request.getParameter("userid");// due to mobile
		}
		if (userid==null) {
			request.setAttribute("Error!", "Session losted!!!");
			RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
			mDispatcher.forward(request, response);
			return;
		}
		String create_userid = (String) request.getParameter("create-adminid");
		String fullname = (String) request.getParameter("create-admin-fullname");
		String email = (String) request.getParameter("create-admin-email");
		String address = (String) request.getParameter("create-admin-address");
		String city = (String) request.getParameter("create-admin-city");
		String country = (String) request.getParameter("create-admin-country");
		String new_password = request.getParameter("create-admin-new_password");
		String fileID = request.getParameter("create-admin-fileid");
		String user_image_path = root_path + "data/" +userid+"/request/"+fileID+"_cr.jpg";
		con=MySQLConfig.getConnection();
		PrintWriter out = response.getWriter();
		String json_str="{searchlists : [";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from users_info where member = 'admin'");
			int i=0;
			while (rs.next()==true){
				String user_id=rs.getString("userid");
				if (user_id.equals(create_userid)) {
					i++;
				}
			}
			if (i>0){
				out.print("duplicate");
				return;
			}
		} catch (SQLException e) {
			json_str="fail";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
			return;
		} catch (org.json.JSONException e){
			LogIn.m_logger.debug(json_str);
			e.printStackTrace();	
			return;
		}
		
		PreparedStatement pstmt;
		
		if(fileID!=null) is_sel_photo=true;
		else {
			out.print("fail");
			LogIn.m_logger.info("AgentCreate.java" + json_str);
			is_sel_photo=false;
			return;
		}
		
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
						pstmt.setString(9,create_userid);
						pstmt.setString(10,"admin");
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
						
						pstmt.setString(8,create_userid);
						pstmt.setString(9,"admin");
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
