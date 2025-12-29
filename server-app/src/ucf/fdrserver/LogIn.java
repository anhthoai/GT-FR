package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

import org.apache.log4j.*;
import org.json.*;
/**
 * Servlet implementation class LogIn
 * Supports both /LogIn and /login for case-insensitive access
 */
@WebServlet({"/LogIn", "/login"})
public class LogIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger m_logger = Logger.getLogger(MySQLConfig.class);
	private MySQLConnection con;
	public static String  root_path="";
	public static String personimgdir="";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() {
        super();
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
		String logFile = root_path + "/WEB-INF/FDRS.log" ; 
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	MySQLConfig.readUserInfo(root_path+"user_config.txt");
    	personimgdir = root_path+"personimgs/";
		Layout layout = new PatternLayout("%p : [%d{dd MMM yyyy HH:mm:ss,SSS}] - %m%n");
		try {
			if(MySQLConfig.enablelog == 1 ){
				m_logger.addAppender(new FileAppender(layout, logFile));
			}else{
				//PropertyConfigurator.configure(root_path + "/WEB-INF/FDS.log");
				BasicConfigurator.configure();
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		String userid = request.getParameter("userid");
		String password = request.getParameter("password");
		String strMobile = request.getParameter("mobile");
		
		File img_f = new File(personimgdir);
		if (!img_f.exists())  img_f.mkdirs();
		int deviceID = 0;
		if( strMobile != null )
			deviceID = Integer.parseInt(strMobile);
		
    	
    	MySQLConfig.userinfo_path = root_path + "user_config.txt";
    	con = MySQLConfig.getConnection();  
    	if (con == null){
    		m_logger.debug("Failed to connect to MySQL database!");
    	}
		
		PrintWriter out = response.getWriter();
		try {
			Statement stmt = con.createStatement();
			String sql = "select * from users_info where userid='"+userid+"'";//+"' and password="+password;
			ResultSet rs = stmt.executeQuery(sql);
			
			if (rs.next()){
				String db_password = rs.getString("password");
				
				String member = rs.getString("member");
				if (password.compareTo(db_password)==0){
					String fullname = rs.getString("fullname");
					String email = rs.getString("email");
					String address = rs.getString("address");
					String city = rs.getString("city");
					String country = rs.getString("country");
					String adminid = rs.getString("adminid");
					
					byte[] userimage = rs.getBytes("user_img");
					if (userimage == null)
					{
						String user_image_path = root_path + "/assets/img/face.png";
						File in_f= new File(user_image_path);
						try {
							DataInputStream distream;
							distream = globalUtil.getInputStream(user_image_path);
							userimage=new byte[(int)in_f.length()];
							distream.read(userimage);
							distream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								out.print("fail");
								return;
							}
					}
					String fileDir = root_path+"data/"+userid;
					File f = new File(fileDir);
					if (!f.exists())  f.mkdirs();
					if (f.exists()==true){				
						globalUtil.delFolderContent(fileDir);
						//f.delete();
					}
					String user_image_path = root_path+"/assets/img/user.jpg";
					DataOutputStream dos = globalUtil.getOutputStream(user_image_path);
					dos.write(userimage);
					dos.close();
					
			        HttpSession session = request.getSession();
			        session.setAttribute("MyAttribute", "test value");
			        session.setAttribute("fullname", fullname);
			        session.setAttribute("email", email);
			        session.setAttribute("address", address);
			        session.setAttribute("city", city);
			        session.setAttribute("country", country);
			        session.setAttribute("userid", userid);
			        session.setAttribute("password", password);
			        session.setAttribute("member", member);
			        session.setAttribute("adminid", adminid);
			        //session.setAttribute("userimage", userimage);
					
					request.setAttribute("fullname", fullname);
					request.setAttribute("email", email);
					request.setAttribute("address", address);
					request.setAttribute("city", city);
					request.setAttribute("country", country);
					request.setAttribute("userid", userid);
					request.setAttribute("password", password);
					request.setAttribute("member", member);
					request.setAttribute("adminid", adminid);
					//request.setAttribute("userimage", userimage);
					
					if( deviceID == 0 )
					{
				        RequestDispatcher mDispatcher = request.getRequestDispatcher("/search.jsp");
						mDispatcher.forward(request, response);
				
					}else {
						String strJson = "{";
						strJson += "fullname:'" + fullname+"',";
						strJson += "email:'" + email + "',";
						strJson += "address:'" + address + "',";
						strJson += "city:'" + city + "',";
						strJson += "country:'" + country + "',";
						strJson += "userid:'" + userid + "',";
						strJson += "password:'" + password+"',";
						strJson += "member:'" + member+"',";
						strJson += "adminid:'" + adminid+"'";
						strJson += "}";
						
						JSONObject obj = new JSONObject(strJson);
						out.print(obj);
					}
					return;
				}else{ // Invalid Password
					if( deviceID == 0 )
					{
						request.setAttribute("error", "Wrong password! Fogot your password? Please type password again!");
						RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
						mDispatcher.forward(request, response);
					}
					else {
						String strJson = "{error:Wrong password! Fogot your password?\n Please type password again!}";
						JSONObject obj = new JSONObject(strJson);
						out.print(obj);
					}
					return;
				}
			}else{ // User Does Not Exist
				if( deviceID == 0 )
				{
					request.setAttribute("error", "Wrong user id! Please type user id again!");
					RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
					mDispatcher.forward(request, response);
				}
				else 
				{
					String strJson = "{error:Wrong password! Wrong user id! Please type user id again!}";
					JSONObject obj = new JSONObject(strJson);
					out.print(obj);
	
				}
			}
			
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
