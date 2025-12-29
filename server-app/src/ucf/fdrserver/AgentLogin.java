package ucf.fdrserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

import org.apache.log4j.*;
import org.json.*;
/**
 * Servlet implementation class AgentLogin
 */
@WebServlet("/AgentLogin")
public class AgentLogin extends HttpServlet {
	public static Logger m_logger = Logger.getLogger(MySQLConfig.class);
	private MySQLConnection con;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AgentLogin() {
        super();
    }
    public void init()
    {
    	String  root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	MySQLConfig.readUserInfo(root_path+"user_config.txt");
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
		String userid = request.getParameter("user_id");
		String password = request.getParameter("password");
    	String  root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path = root_path + "user_config.txt";
    	con = MySQLConfig.getConnection();  
    	if (con == null){
    		m_logger.debug("Failed to connect to MySQL database!");
    	}
		
		PrintWriter out = response.getWriter();
		String json_str="{searchlists : [";
		try {
			Statement stmt = con.createStatement();
			String sql = "SELECT * FROM users_info where userid='"+userid+"' and password='"+password+"'";
			ResultSet rs = stmt.executeQuery(sql);
			String adminid = "";
			String member = "";
			int i = 0;
			while (rs.next()==true){
				member = rs.getString("member");
				adminid = rs.getString("adminid");
				if (i>0) json_str+=",";
				json_str+="{";
				json_str+="user_id : '"+userid+"',";
				json_str+="password : '"+password+"',";
				json_str+="member : '"+member+"',";
				json_str+="adminid : '"+adminid+"',";
				json_str+="}";
				i++;
			}
			json_str+="]}";	
			if (i==0){
				json_str="{searchlists : none}";
			}
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			LogIn.m_logger.debug("Failed to connect to MySQL database!");
			e.printStackTrace();
		}
		
	}

}
