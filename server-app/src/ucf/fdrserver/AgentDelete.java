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

@WebServlet("/AgentDelete")
public class AgentDelete extends HttpServlet {
	String root_path="";

	private Connection con;
	public AgentDelete() {
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
		int id = Integer.parseInt(request.getParameter("index"));
		
		con=MySQLConfig.getConnection();
		PreparedStatement pstmt;

		PrintWriter out = response.getWriter();

		try {
			String query ="DELETE FROM users_info WHERE id = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			
			String json_str="{admindelete : ok}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("AgentDelete.java" + json_str);
			
		} catch (SQLException e) {
			String json_str="{admindelete : fail}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
		} catch (org.json.JSONException e){
			String json_str="{admindelete : fail}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
		}
	}
}
