package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

@WebServlet("/RegisterDevice")
public class RegisterDevice extends HttpServlet {
	String root_path="";
	private MySQLConnection con;
	public RegisterDevice() {
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
		String token = (String) request.getParameter("token");
		String adminid = (String) request.getParameter("adminid");
		
		con=MySQLConfig.getConnection();
		PrintWriter out = response.getWriter();

		PreparedStatement pstmt;
		String json_str="{register_device : ok}";

		try {
			String query = "INSERT INTO device_lists (token, adminid)"
					   +" VALUES(?, ?)";
				
				pstmt = con.prepareStatement(query);
				pstmt.setString(1,token);
				pstmt.setString(2,adminid);
				pstmt.executeUpdate();

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
