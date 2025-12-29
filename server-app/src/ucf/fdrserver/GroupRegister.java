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

@WebServlet("/GroupRegister")
public class GroupRegister extends HttpServlet {
	String root_path="";
	private MySQLConnection con;
	public GroupRegister() {
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
		
		String group_name = (String) request.getParameter("register_group_name");
		
		con=MySQLConfig.getConnection();
		ResultSet rs;
		PrintWriter out = response.getWriter();

		PreparedStatement pstmt;
		String json_str="{register_group : ok}";

		try {
			String query = "INSERT INTO group_info (name, adminid)"
					   +" VALUES(?, ?)";
				
				pstmt = con.prepareStatement(query);
				pstmt.setString(1,group_name);
				pstmt.setString(2,userid);
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
