package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

/**
 * Servlet implementation class Search
 */
@WebServlet("/ReadCameraInfo")
public class ReadCameraInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection  con;
	String root_path="";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadCameraInfo() {
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
		String userid = (String) request.getParameter("user_id");
		String admin_id= (String) request.getParameter("admin_id");
		String camera_id= (String) request.getParameter("camera_id");
		
		PrintWriter out = response.getWriter();
		String query = "select * from camera_info where user_id='" + userid + "' AND adminid='" + admin_id + "'";
    	con = MySQLConfig.getConnection();
    	String json_str="{searchlists : [";
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				int i=0;
				while (rs.next()==true){
					String camera_name=rs.getString("camera" + camera_id + "_name");
					String camera_url=rs.getString("camera" + camera_id + "_url");
					json_str+="{";
					json_str+="camera_name : '"+camera_name+"',";
					json_str+="camera_url : '"+camera_url+"'";
					json_str+="}";
					i++;
				}
				json_str+="]}";	
				if (i==0){
					json_str="{searchlists : none}";
				}
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("ReadCameraInfo.java" + json_str);
			} catch (SQLException e) {
				json_str="{searchlists : fail}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				e.printStackTrace();
			} catch (org.json.JSONException e){
				LogIn.m_logger.debug(json_str);
				e.printStackTrace();	
			}
		
	}
}
