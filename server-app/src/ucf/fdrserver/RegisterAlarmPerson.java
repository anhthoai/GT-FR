package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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

/**
 * Servlet implementation class Search
 */
@WebServlet("/RegisterAlarmPerson")
public class RegisterAlarmPerson extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MySQLConnection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	String image_path="";
	
	String feature_fpath="";
	String cropimg_fpath="";
	String exe_path="";
	String root_path="";
	String feature_str="";
	byte[] rsrc_imgbytes;
	String feature_str1="";
	byte[] rsrc_imgbytes1;
	String feature_str2="";
	byte[] rsrc_imgbytes2;
	String feature_str3="";
	byte[] rsrc_imgbytes3;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterAlarmPerson() {
        super();       
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	feature_fpath=root_path+"\\externalexec\\register_feature.txt";
    	exe_path=root_path+"\\externalexec\\OpenVinoFaceEngine.exe";
    	cropimg_fpath=root_path+"\\externalexec\\register_cropimg.jpg";
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
		boolean is_sel_photo=false;
		boolean is_sel_photo1=false;
		boolean is_sel_photo2=false;
		boolean is_sel_photo3=false;
		HttpSession session = request.getSession();
		String userid = (String) session.getAttribute("userid");
		String is_mobile_str = (String) request.getParameter("mobile");
		int is_mobile = 0;
		if (is_mobile_str!=null){
			is_mobile = Integer.parseInt(is_mobile_str);	
		}
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
		String sex= (String) request.getParameter("sex");
		String birth= (String) request.getParameter("birthday");
		String home= (String) request.getParameter("home");
		String email= (String) request.getParameter("email");
		String phone_str=(String) request.getParameter("phone");
		String city_str=(String) request.getParameter("city");
		String country_str=(String) request.getParameter("country");
		String file_id=(String) request.getParameter("file_id");
		String adminid = (String) request.getParameter("adminid");
		String group_name = (String) request.getParameter("group_name");
		
		String register_imgpath=root_path+file_id;
       	File f;
       	deletetmpfiles();
		String[] args = new String [3];
		args[0] = exe_path;					//input exe path
		args[1] = register_imgpath;//input src imgpath
		args[2] = feature_fpath;	//feature
		Process proc;
		try {
			proc = Runtime.getRuntime().exec(args);
			proc.waitFor();
			
			f=new File(feature_fpath);
			
			if (f.exists()==true){
				DataInputStream dis = globalUtil.getInputStream(feature_fpath);
				feature_str= dis.readLine().trim();
				dis.close();
				DataInputStream distream;
				File in_f= new File(register_imgpath);
				try {
					distream = globalUtil.getInputStream(register_imgpath);
					rsrc_imgbytes=new byte[(int)in_f.length()];
					distream.read(rsrc_imgbytes);
					distream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						String json_str="{registerlists : fail}";
						JSONObject obj = new JSONObject(json_str);
						out.print(obj);
						return;
					}
			}
			else{
				String json_str="{registerlists : nofeature}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("RegisterAlarmPerson.java" + json_str);
				return;
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			String json_str="{registerlists : fail}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("RegisterAlarmPerson.java" + json_str);
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			String json_str="{registerlists : none}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("RegisterAlarmPerson.java" + json_str);
			return;
		}
		
		PreparedStatement pstmt;
		String json_str="{registerlists : ok}";
    	
		
		try {
				String query = "INSERT INTO person_info (name,sex,birthday,home,city,country,email,phone,person_img, feature,person_img1, feature1,person_img2, feature2,person_full_img, adminid, group_name, average) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1,name);
				pstmt.setString(2,sex);
				pstmt.setString(3,birth);
				pstmt.setString(4, home);
				pstmt.setString(5, city_str);
				pstmt.setString(6, country_str);
				pstmt.setString(7, email);
				pstmt.setString(8, phone_str);
				pstmt.setBytes(9, rsrc_imgbytes);
				pstmt.setString(10,feature_str);
				pstmt.setBytes(11, rsrc_imgbytes1);
				pstmt.setString(12,feature_str1);
				pstmt.setBytes(13, rsrc_imgbytes2);
				pstmt.setString(14,feature_str2);
				pstmt.setBytes(15, rsrc_imgbytes3);
				pstmt.setString(16,adminid);
				pstmt.setString(17,group_name);
				pstmt.setFloat(18,0);
				pstmt.executeUpdate();
	
				json_str+="{registerlists : ok}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("RegisterAlarmPerson.java" + json_str);
				
				globalUtil.delFolderContent(root_path+"\\data\\"+userid+"\\request\\");
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
	public void deletetmpfiles(){
    	File f=new File(feature_fpath);
		if (f.isFile()==true) f.delete();
		f=new File(cropimg_fpath);
    }
}
