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

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.MySQLConnection;


import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * Servlet implementation class Search
 */
@WebServlet("/UserSearch")
public class UserSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MySQLConnection  con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	String image_path="";
	
	String feature_fpath="";
	String cropimg_fpath="";
	String exe_path="";
	String root_path="";
	String feature_str="";
	String personimgdir = "";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserSearch() {
        super();       
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
    	personimgdir = root_path+"personimgs/";
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    	feature_fpath=root_path+"\\externalexec\\search_feature.txt";
    	exe_path=root_path+"\\externalexec\\OpenVinoFaceEngine.exe";
    	cropimg_fpath=root_path+"\\externalexec\\search_cropimg.jpg";
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
		
		String sql = "";
		String user_id= (String) request.getParameter("user_id");
		if(user_id!=null)
			sql+="userid like '%"+user_id+"%'";
		String country= (String) request.getParameter("country");
		if(country!=null){
			if(sql!="") sql+=" AND country='"+country+"'";
			else sql="country='"+country+"'";
			
		}
		
		String address= (String) request.getParameter("address");
		if(address!=null){
			if(sql!="") sql+=" AND address like '%"+address+"%'";
			else sql="address like '%"+address+"%'";
		}
		String email= (String) request.getParameter("email");
		if(email!=null){
			if(sql!="") sql+=" AND email like '%"+email.trim()+"%'";
			else sql="email like '%"+email.trim()+"%'";
		}
		String city=(String) request.getParameter("city");
		if(city!=null){
			if(sql!="") sql+=" AND city like '%"+city.trim()+"%'";
			else sql="city="+city.trim();
		}
		
		String member_str=(String) request.getParameter("member");
		if (!member_str.equals("agent")) {
			String adminid=(String) request.getParameter("admin_id");
			if(adminid!=null){
				if(sql!="") sql+=" AND adminid='"+adminid.trim()+"'";
				else sql="adminid='"+adminid.trim() + "'";
			}
		}
		
		PrintWriter out = response.getWriter();
		if(sql!="") sql+=" AND member = 'user'";
		else sql="member = 'user'";
		if(sql!=""){
			sql = "select * from users_info where "+sql;
			
		}
		else
			sql = "select * from users_info where 1";
		String file_id=(String) request.getParameter("file_id");
		int id;
    	con = MySQLConfig.getConnection();
    	
    	globalUtil.delFolderContent(personimgdir);
		String json_str="{searchlists : [";
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				int i=0;
				double temp_simscore=0;
				while (rs.next()==true){
					id=rs.getInt("id");
					String name = rs.getString("fullname");
					country = rs.getString("country");
					address=rs.getString("address");
					email=rs.getString("email");
					city=rs.getString("city");
					user_id=rs.getString("userid");
					String adminid = rs.getString("adminid");
					
					Blob datablob = rs.getBlob("user_img");
					byte [] imgbytes;
					if (datablob == null)
					{
						String user_image_path = root_path + "/assets/img/face.png";
						File in_f= new File(user_image_path);
						try {
							DataInputStream distream;
							distream = globalUtil.getInputStream(user_image_path);
							imgbytes=new byte[(int)in_f.length()];
							distream.read(imgbytes);
							distream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								out.print("fail");
								return;
							}
					}
					else
					{
						InputStream is = datablob.getBinaryStream();
						imgbytes = new byte[(int)datablob.length()];
						is.read(imgbytes);
						is.close();
					}
					double rand = Math.random();
			    	long fileid = (long) (rand*999999999);
					image_path=personimgdir+fileid+".jpg";
					File img_f = new File(image_path);
					if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path);
						dos.write(imgbytes);
						dos.close();
					}
					//fileid = rs.getInt("fileid");
	
					if (i>0) json_str+=",";
					json_str+="{";
					json_str+="fullname : '"+name+"',";
					json_str+="country : '"+country+"',";
					json_str+="address : '"+address+"',";
					json_str+="email : '"+email+"',";
					json_str+="user_id : '"+user_id+"',";
					json_str+="adminid : '"+adminid+"',";
					json_str+="id : "+id+",";
					json_str+="fileid : "+fileid+",";
					json_str+="city : '"+city+"',";
					json_str+="score : "+temp_simscore+",";
					json_str+="}";
					i++;
					if(temp_simscore==100) break;
				}
				json_str+="]}";	
				if (i==0){
					json_str="{searchlists : none}";
				}
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("UserSearch.java" + json_str);
			} catch (SQLException e) {
				json_str="{searchlists : none}";
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
