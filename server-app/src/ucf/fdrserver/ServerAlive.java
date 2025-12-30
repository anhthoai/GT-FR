package ucf.fdrserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.*;

import java.sql.Connection;
import ucf.fdrssutil.MySQLConfig;
@WebServlet("/ServerAlive")
public class ServerAlive extends HttpServlet {
	Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Connection con;
	public ServerAlive() {
		super();        
	}
	public void init()
	{
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String adminid = request.getParameter("adminid");
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String root_path = getServletContext().getRealPath("/");
		MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	con = MySQLConfig.getConnection();   
//		String query = "UPDATE agents_info SET state='"+timeStamp+"' WHERE id="+agentid;
//		try {
//			Statement stmt = con.createStatement();
//			stmt.executeUpdate(query);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} 
		try{
			String json_str="{result:ok}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
		} catch (org.json.JSONException e){
			e.printStackTrace();
		}
	}
}
