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

@WebServlet("/GroupUpdate")
public class GroupUpdate extends HttpServlet {
	String root_path="";

	private MySQLConnection con;
	public GroupUpdate() {
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
		String group_name = (String) request.getParameter("group_name");
		int id = Integer.parseInt(request.getParameter("id"));	
		
		con=MySQLConfig.getConnection();
		PreparedStatement pstmt;

		PrintWriter out = response.getWriter();

		try {
			String query = "";
			if (group_name != null)
			{
				query = "UPDATE group_info SET name = ? WHERE id = ? AND adminid = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, group_name);
				pstmt.setInt(2, id);
				pstmt.setString(3, userid);
				pstmt.executeUpdate();
			}
			out.print("success");
			LogIn.m_logger.info("GroupUpdate.java" + ": success");
			
		} catch (SQLException e) {
			out.print("fail");
			e.printStackTrace();
		} catch (org.json.JSONException e){
			out.print("fail");
			e.printStackTrace();
		}
	}
}
