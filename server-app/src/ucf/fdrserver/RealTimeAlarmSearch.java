package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Base64;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.Timestamp;


import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;


/**
 * Servlet implementation class Search
 */
@WebServlet("/RealTimeAlarmSearch")
public class RealTimeAlarmSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection  con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	Format formatter1 = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
	String image_path="";
	public static String historyimgdir="";
	public static String personimgdir="";
	
	String root_path="";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RealTimeAlarmSearch() {
        super();       
    }
    public void init()
    {
    	root_path = getServletContext().getRealPath("/");
    	historyimgdir = root_path+"historyimgs/";
    	personimgdir = root_path+"personimgs/";
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
		boolean is_sel_photo=false;
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
		String name= (String) request.getParameter("name");
		if(name!=null)
			sql+="name like '%"+name+"%'";
		String sex= (String) request.getParameter("sex");
		if(sex!=null){
			if(sql!="") sql+=" AND sex='"+sex+"'";
			else sql="sex='"+sex+"'";
			
		}
		String birth= (String) request.getParameter("birthday");
		if(birth!=null){
			String[] parts = birth.split("-");
			int length=parts.length;
			if(length==3){
				if(sql!="") sql+=" AND birthday='"+birth.trim()+"'";
				else sql="birthday='"+birth.trim()+"'";
				//yyyy-MM-dd HH:mm:ss
			}else if(length==2){
				if(sql!="") sql+=" AND birthday>='"+birth.trim()+"-01' AND birthday <='" +birth.trim()+"-31'";
				else sql="birthday>='"+birth.trim()+"-01' AND birthday <='" +birth.trim()+"-31'";
				
			}else if(length==1){
				if(sql!="") sql+=" AND birthday>='"+birth.trim()+"-01-01' AND birthday <='" +birth.trim()+"-12-31'";
				else sql="birthday>='"+birth.trim()+"-01-01' AND birthday <='" +birth.trim()+"-12-31'";
				
			}
		}
		String home= (String) request.getParameter("home");
		if(home!=null){
			if(sql!="") sql+=" AND home like '%"+home+"%'";
			else sql="home like '%"+home+"%'";
		}
		String email= (String) request.getParameter("email");
		if(email!=null){
			if(sql!="") sql+=" AND email like '%"+email.trim()+"%'";
			else sql="email like '%"+email.trim()+"%'";
		}
		String phone_str=(String) request.getParameter("phone");
		if(phone_str!=null){
			if(sql!="") sql+=" AND phone like '%"+phone_str.trim()+"%'";
			else sql="phone='"+phone_str.trim() +"'";
		}
		String group_name=(String) request.getParameter("group_name");
		if(group_name!=null){
			if(sql!="") sql+=" AND group_name='"+group_name.trim()+"'";
			else sql="group_name='"+group_name.trim() +"'";
		}
		
		String city_str=(String) request.getParameter("city");
		if(city_str!=null){
			if(sql!="") sql+=" AND city like '%"+city_str.trim()+"%'";
			else sql="city='"+city_str.trim() +"'";
		}
		
		String country_str=(String) request.getParameter("country");
		if(country_str!=null){
			if(sql!="") sql+=" AND country like '%"+country_str.trim()+"%'";
			else sql="country='"+country_str.trim() +"'";
		}
		
		
		int verify_status=0;
		String verifyStateParam = request.getParameter("verify_state");
		if (verifyStateParam != null && !verifyStateParam.isEmpty()) {
			try { verify_status = Integer.parseInt(verifyStateParam); } catch (NumberFormatException ignored) {}
		}
		if (verify_status == 1)
		{
			if(sql!="") sql+=" AND verify_state='"+verify_status+"'";
			else sql="verify_state='"+verify_status + "'";
		}
		
		String from=(String) request.getParameter("from");
		if(from!=null){
			if(sql!="") sql+=" AND access>='"+from.trim()+"'";
			else sql="access>='"+from.trim() + "'";
		}
		
		String to=(String) request.getParameter("to");
		if(to!=null){
			if(sql!="") sql+=" AND access<'"+to.trim()+"'";
			else sql="access<'"+to.trim() + "'";
		}
		
		String file_id=(String) request.getParameter("file_id");
		if(file_id!=null) is_sel_photo=true;
		else is_sel_photo=false;
		
		PrintWriter out = response.getWriter();
		
		int id;
		String  phone = "";
    	con = MySQLConfig.getConnection();
    	
    	String member=(String) request.getParameter("member");
    	if (member == null) member = "";
    	String adminid = "";
		if (!"agent".equals(member))
		{
			try {
				String query = "select * from users_info where userid='"+userid+"'";
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				// Connector/J 8 default ResultSet is TYPE_FORWARD_ONLY -> use next() not first()
				if (!rs.next())
				{
					out.print("fail");
					return;
				}
				adminid = rs.getString("adminid");
			
			} catch (SQLException e) {
				out.print("fail");
				e.printStackTrace();
			} catch (org.json.JSONException e){
				out.print("fail");
				e.printStackTrace();
			}
		}
		
		String count_sql = "select * from alarm_info where ";
		
		if(adminid != null && !adminid.isEmpty()){
			if(sql!="") sql+=" AND adminid='"+adminid.trim()+"'";
			else sql="adminid='"+adminid.trim() + "'";
			
			count_sql += "adminid='"+adminid.trim() + "'";
		}
		else
		{
			count_sql += "1";
		}
		String json_str="{searchlists : [";
		
		int total_count = 0;
		
		if(sql!=""){
			sql = "select * from alarm_info where "+sql;
			
		}
		else
			sql = "select * from alarm_info where 1";
		
		
		File f = new File(personimgdir);
		if (f.exists()==false) f.mkdirs();
//		else{
//			globalUtil.delFolderContent(personimgdir);
//		}
		
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				
				while (rs.next()==true)
				{
					total_count++;
				}
				int i=0;
				int ii=0;
				rs = stmt.executeQuery(sql);
				while (rs.next()==true)
				{
					if (total_count > 10 && ii < (total_count - 10))
					{
						ii++;
						continue;
					}

					id=rs.getInt("id");
					int score = rs.getInt("score");
					name = rs.getString("name");						
					sex = rs.getString("sex");
					if (sex==null) sex="";
					Date birth_dt=rs.getDate("birthday");
					if (birth_dt==null) birth="";
					else
						birth=formatter.format(birth_dt);
					home=rs.getString("home_address");
					if (home==null) home="";
					email=rs.getString("email");
					phone=rs.getString("phone");
					city_str=rs.getString("city");
					if (city_str==null) city_str="";
					country_str=rs.getString("country");
					if (country_str==null) country_str="";
					String alarm_id=rs.getString("alarm_id");
					adminid=rs.getString("adminid");
					group_name=rs.getString("group_name");
					if (group_name == null)
						group_name = "Nogroup";
					Date d1 = null;
					//String time = new   SimpleDateFormat("HH-mm-ss.SSS").format(rs.getTime("access").getTime());

					Timestamp timestamp = rs.getTimestamp("access");
					Time tt = new Time(timestamp.getTime());
					String access_time1 = new SimpleDateFormat("HH-mm-ss.SSS").format(tt);
					if (timestamp != null)
						d1 = new java.util.Date(timestamp.getTime());
					   
					String access_date="";
					try
					{
						access_date = formatter.format(d1);
					}
					catch (Exception e)
					{
						continue;
					}
					

					double rand = Math.random();
					
					String part2 = access_time1.substring(0, access_time1.indexOf("."));
					String part3 = access_time1.substring(access_time1.indexOf(".") + 1, access_time1.length());
					String access_time = part2 + "-" + part3;

			    	File ff = new File(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_detected" + ".encode");
			    	if (ff.exists())
			    	{
			    		BufferedReader br = new BufferedReader(new FileReader(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_detected" + ".encode"));
						try {
						    StringBuilder sb = new StringBuilder();
						    String line = br.readLine();

						    while (line != null) {
						        sb.append(line);
						        sb.append(System.lineSeparator());
						        line = br.readLine();
						    }
						    String everything = sb.toString();
							String sanitized = everything.replaceAll("\\s+", "");
							byte[] imageByte = Base64.getDecoder().decode(sanitized);
							image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_detected.jpg";
							//if (!img_f.exists()){
								DataOutputStream dos = globalUtil.getOutputStream(image_path);
								dos.write(imageByte);
								dos.close();
							//}
						} finally {
						    br.close();
						}
			    	}
			    	else
			    	{
			    		Path path = Paths.get(root_path + "/assets/img/no_image.jpg");
			    		byte[] data = Files.readAllBytes(path);
			    		image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_detected.jpg";
						//if (!img_f.exists()){
							DataOutputStream dos = globalUtil.getOutputStream(image_path);
							dos.write(data);
							dos.close();
						//}
			    		
			    	}
			    	ff = new File(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_recognized" + ".encode");
			    	if (ff.exists())
			    	{
			    		BufferedReader br = new BufferedReader(new FileReader(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_recognized" + ".encode"));
						try {
						    StringBuilder sb = new StringBuilder();
						    String line = br.readLine();
	
						    while (line != null) {
						        sb.append(line);
						        sb.append(System.lineSeparator());
						        line = br.readLine();
						    }
						    String everything = sb.toString();
							String sanitized = everything.replaceAll("\\s+", "");
							byte[] imageByte = Base64.getDecoder().decode(sanitized);
							image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_recognized.jpg";
							//if (!img_f.exists()){
								DataOutputStream dos = globalUtil.getOutputStream(image_path);
								dos.write(imageByte);
								dos.close();
							//}
						} finally {
						    br.close();
						}
			    	}
			    	else
			    	{
			    		Path path = Paths.get(root_path + "/assets/img/no_image.jpg");
			    		byte[] data = Files.readAllBytes(path);
			    		image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_recognized.jpg";
						//if (!img_f.exists()){
							DataOutputStream dos = globalUtil.getOutputStream(image_path);
							dos.write(data);
							dos.close();
						//}
			    	}
			    	ff = new File(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_full" + ".encode");
			    	if (ff.exists())
			    	{
			    		BufferedReader br = new BufferedReader(new FileReader(historyimgdir + "/" + access_date + "/" + alarm_id + "_" + access_time1 + "_" + adminid + "_full" + ".encode"));
						try {
						    StringBuilder sb = new StringBuilder();
						    String line = br.readLine();
	
						    while (line != null) {
						        sb.append(line);
						        sb.append(System.lineSeparator());
						        line = br.readLine();
						    }
						    String everything = sb.toString();
							String sanitized = everything.replaceAll("\\s+", "");
							byte[] imageByte = Base64.getDecoder().decode(sanitized);
							image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_full.jpg";
							//if (!img_f.exists()){
								DataOutputStream dos = globalUtil.getOutputStream(image_path);
								dos.write(imageByte);
								dos.close();
							//}
						} finally {
						    br.close();
						}
			    	}
			    	else
			    	{
			    		Path path = Paths.get(root_path + "/assets/img/no_image.jpg");
			    		byte[] data = Files.readAllBytes(path);
			    		image_path=personimgdir+"/" + access_date + "_" + alarm_id + "_" + access_time + "_" + adminid + "_full.jpg";
						//if (!img_f.exists()){
							DataOutputStream dos = globalUtil.getOutputStream(image_path);
							dos.write(data);
							dos.close();
			    	}
					
					
					if (i>0) json_str+=",";
					json_str+="{";
					json_str+="name : '"+name+"',";
					json_str+="sex : '"+sex+"',";
					json_str+="birth : '"+birth+"',";
					json_str+="home : '"+home+"',";
					json_str+="city : '"+city_str+"',";
					json_str+="country :'"+country_str+"',";
					json_str+="email : '"+email+"',";
					json_str+="id : "+id+",";
					json_str+="phone : "+phone+",";
					json_str+="score : "+score+",";
					json_str+="access : "+access_date + " " + access_time1 +",";
					json_str+="fileid : "+access_date + "_" + alarm_id + "_" + access_time + "_" + adminid +",";
					json_str+="alarm_id : "+alarm_id+",";
					json_str+="adminid : "+adminid+",";
					json_str+="group_name : "+group_name+",";
					json_str+="}";
					i++;
				}
				json_str+="]}";	
				if (i==0){
					json_str="{searchlists : none}";
				}
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("AlarmSearch.java" + json_str);
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
}
