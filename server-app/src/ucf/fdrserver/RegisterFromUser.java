package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.PreparedStatement;
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

import com.mysql.jdbc.MySQLConnection;

import sun.misc.BASE64Decoder;
import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * Servlet implementation class Search
 */
@WebServlet("/RegisterFromUser")
public class RegisterFromUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MySQLConnection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	private String root_path = "";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterFromUser() {
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
		PrintWriter out = response.getWriter();
		con=MySQLConfig.getConnection();
		ResultSet rs;
		
		String name= (String) request.getParameter("name");
		String email= (String) request.getParameter("email");
		String phone_str=(String) request.getParameter("phone");
		String group_name = (String) request.getParameter("group_name");
		String query = (String) request.getParameter("query");
		String image_str = (String)(request.getParameter("image"));
		String feature_str = (String)(request.getParameter("feature"));
		String adminid = (String)(request.getParameter("adminid"));
		PreparedStatement pstmt;
		String json_str="{registerlists : ok}";

		BASE64Decoder decoder = new BASE64Decoder();
		byte[] imageByte = decoder.decodeBuffer(image_str);
		
		try {
				pstmt = con.prepareStatement(query);
				pstmt.setString(1,name);
				pstmt.setString(2, email);
				pstmt.setString(3, phone_str);
				pstmt.setBytes(4, imageByte);
				pstmt.setString(5,feature_str);
				pstmt.setString(6, adminid);
				pstmt.setString(7, group_name);
				pstmt.setFloat(8,0);
				pstmt.executeUpdate();
	
				json_str+="{registerlists : ok}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("RegisterFromUser.java" + json_str);
				
			} catch (SQLException e) {
				json_str="{registerlists : fail}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				e.printStackTrace();
			} catch (org.json.JSONException e){
				LogIn.m_logger.debug(json_str);
				e.printStackTrace();	
			}
		
	}
}
