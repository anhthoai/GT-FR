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

@WebServlet("/SaveCameraUrl")
public class SaveCameraUrl extends HttpServlet {
	String root_path="";
	private Connection con;
	public SaveCameraUrl() {
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
		String user_id = (String) request.getParameter("user_id");
		String admin_id = (String) request.getParameter("admin_id");
		String camera_name = (String) request.getParameter("camera_name");
		String camera_url = (String) request.getParameter("camera_url");
		String camera_id_str = (String) request.getParameter("camera_id");
		int camera_id = Integer.parseInt(camera_id_str);
		
		con=MySQLConfig.getConnection();
		PrintWriter out = response.getWriter();
		PreparedStatement pstmt;
		try {
				String query = "SELECT COUNT(*) as rowcount FROM camera_info WHERE user_id = '" + user_id + "' AND adminid = '" + admin_id + "'";
				Statement stmt = con.createStatement();
				ResultSet r = stmt.executeQuery(query);
				r.next();
				int count = r.getInt("rowcount");
				r.close();
				if (count  > 0)
				{
					query = "UPDATE camera_info SET camera" + camera_id_str +"_name = ?, camera" + camera_id_str +"_url = ? WHERE user_id = ? AND adminid = ?";
					
					pstmt = con.prepareStatement(query);
					pstmt.setString(1,camera_name);
					pstmt.setString(2,camera_url);
					pstmt.setString(3,user_id);
					pstmt.setString(4,admin_id);
					pstmt.executeUpdate();
					String strJson = "{'result':'ok'}";
					JSONObject obj = new JSONObject(strJson);
					out.print(obj);
				}
				else
				{
					query = "INSERT INTO camera_info (user_id, adminid, camera1_name, camera1_url, camera2_name, camera2_url"
							+ ", camera3_name, camera3_url, camera4_name, camera4_url) VALUES (?,?,?,?,?,?,?,?,?,?)";
					pstmt = con.prepareStatement(query);
					pstmt.setString(1,user_id);
					pstmt.setString(2,admin_id);
					if (camera_id == 1)
					{
						pstmt.setString(3,camera_name);
						pstmt.setString(4, camera_url);
					}
					else
					{
						pstmt.setString(3,"");
						pstmt.setString(4, "");
					}
					if (camera_id == 2)
					{
						pstmt.setString(5,camera_name);
						pstmt.setString(6, camera_url);
					}
					else
					{
						pstmt.setString(5,"");
						pstmt.setString(6, "");
					}
					if (camera_id == 3)
					{
						pstmt.setString(7,camera_name);
						pstmt.setString(8, camera_url);
					}
					else
					{
						pstmt.setString(7,"");
						pstmt.setString(8, "");
					}
					if (camera_id == 4)
					{
						pstmt.setString(9,camera_name);
						pstmt.setString(10, camera_url);
					}
					else
					{
						pstmt.setString(9,"");
						pstmt.setString(10, "");
					}
					pstmt.executeUpdate();
					String strJson = "{'result':'ok'}";
					JSONObject obj = new JSONObject(strJson);
					out.print(obj);
				}
				
			} catch (SQLException e) {
				String strJson = "{'result':'fail'}";
				JSONObject obj = new JSONObject(strJson);
				out.print(obj);
				e.printStackTrace();
			} catch (org.json.JSONException e){
				e.printStackTrace();	
			}
		
	}
}
