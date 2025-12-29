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
@WebServlet("/Search")
public class Search extends HttpServlet {
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
    public Search() {
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
			else sql="phone='"+phone_str.trim()+"'";
		}
		
		String country= (String) request.getParameter("country");
		if(country!=null){
			if(sql!="") sql+=" AND country='"+country+"'";
			else sql="country='"+country+"'";
			
		}
		
		String city=(String) request.getParameter("city");
		if(city!=null){
			if(sql!="") sql+=" AND city like '%"+city.trim()+"%'";
			else sql="city="+city.trim();
		}
		
		
		String adminid= (String) request.getParameter("adminid");
		if(adminid!=null){
			if(sql!="") sql+=" AND adminid='"+adminid+"'";
			else sql="adminid='"+adminid+"'";
			
		}
		
		String group_name = request.getParameter("group_name");
		if(group_name!=null){
			if(sql!="") sql+=" AND group_name='"+group_name+"'";
			else sql="group_name='"+group_name+"'";
			
		}
		if(sql!=""){
			sql = "select * from person_info where "+sql;
			
		}
		else
			sql = "select * from person_info where 1";
		String file_id=(String) request.getParameter("file_id");
		if(file_id!=null) is_sel_photo=true;
		else is_sel_photo=false;
		
		PrintWriter out = response.getWriter();
//		if(is_sel_photo){
//			String search_imgpath=root_path+"\\data\\"+userid+"\\request\\"+file_id+"_cr.jpg";
//	       	File f;
//	       	deletetmpfiles();
//
//			String[] args = new String [3];
//			args[0] = exe_path;					//input exe path
//			args[1] = search_imgpath;//input src imgpath
//			args[2] = feature_fpath;	//feature
//			Process proc;
//			try {
//				proc = Runtime.getRuntime().exec(args);
//				proc.waitFor();
//				
//				f=new File(feature_fpath);
//				
//				if (f.exists()==true){
//					DataInputStream dis = globalUtil.getInputStream(feature_fpath);
//					feature_str= dis.readLine().trim();
//					dis.close();
//				}
//				else{
//					String json_str="{searchlists : none}";
//					JSONObject obj = new JSONObject(json_str);
//					out.print(obj);
//					LogIn.m_logger.info("Search.java" + json_str);
//					return;
//					
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				String json_str="{searchlists : none}";
//				JSONObject obj = new JSONObject(json_str);
//				out.print(obj);
//				LogIn.m_logger.info("Search.java" + json_str);
//				return;
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				String json_str="{searchlists : none}";
//				JSONObject obj = new JSONObject(json_str);
//				out.print(obj);
//				LogIn.m_logger.info("Search.java" + json_str);
//				return;
//			}
//
//		}
		int id;
		String  phone = "";
    	con = MySQLConfig.getConnection();

    	globalUtil.delFolderContent(personimgdir);
    	
		String json_str="{searchlists : [";
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				int i=0;
				double temp_simscore=0;
				id = 0;
				while (rs.next()==true){
//					if(is_sel_photo){
//						String res_feature=rs.getString("feature");
//						temp_simscore = globalUtil.GetSimilarity(feature_str, res_feature);
//						if(temp_simscore<90){
//							continue;
//						}
//						
//					}
					
					double rand = Math.random();
			    	long fileid = (long) (rand*999999999);
			    	double rand1 = Math.random();
			    	long fileid1 = (long) (rand1*999999999);
			    	double rand2 = Math.random();
			    	long fileid2 = (long) (rand2*999999999);
			    	double rand3 = Math.random();
			    	long fileid3 = (long) (rand3*999999999);
			    	
			    	id = rs.getInt("id");
					name = rs.getString("name");
					sex = rs.getString("sex");
					if (sex==null) sex="";
					Date birth_dt=rs.getDate("birthday");
					if (birth_dt!= null) 
						birth=formatter.format(birth_dt);
					else
						birth="1900-0101";
					home=rs.getString("home");
					if (home==null) home="";
					city=rs.getString("city");
					if (city==null) city="";
					country=rs.getString("country");
					if (country==null) country="";
					email=rs.getString("email");
					phone=rs.getString("phone");
					Blob datablob = rs.getBlob("person_img");
					group_name = rs.getString("group_name");
					if (group_name==null) group_name="Nogroup";
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
								json_str="{searchlists : fail}";
								JSONObject obj = new JSONObject(json_str);
								out.print(obj);
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
					image_path=personimgdir+fileid+".jpg";
					File img_f = new File(image_path);
					if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path);
						dos.write(imgbytes);
						dos.close();
					}
					
					datablob = rs.getBlob("person_img1");
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
								json_str="{searchlists : fail}";
								JSONObject obj = new JSONObject(json_str);
								out.print(obj);
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
					
					image_path=personimgdir+fileid1+".jpg";
					img_f = new File(image_path);
					if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path);
						dos.write(imgbytes);
						dos.close();
					}
					
					datablob = rs.getBlob("person_img2");
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
								json_str="{searchlists : fail}";
								JSONObject obj = new JSONObject(json_str);
								out.print(obj);
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
					image_path=personimgdir+fileid2+".jpg";
					img_f = new File(image_path);
					if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path);
						dos.write(imgbytes);
						dos.close();
					}
					
					datablob = rs.getBlob("person_full_img");
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
								json_str="{searchlists : fail}";
								JSONObject obj = new JSONObject(json_str);
								out.print(obj);
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
					image_path=personimgdir+fileid3 + ".jpg";
					img_f = new File(image_path);
					if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path);
						dos.write(imgbytes);
						dos.close();
					}
					
					//fileid = rs.getInt("fileid");
	
					if (i>0) json_str+=",";
					json_str+="{";
					json_str+="name : '"+name+"',";
					json_str+="sex : '"+sex+"',";
					json_str+="birth : '"+birth+"',";
					json_str+="home : '"+home+"',";
					json_str+="city : '"+city+"',";
					json_str+="country : '"+country+"',";
					json_str+="email : '"+email+"',";
					json_str+="id : "+id+",";
					json_str+="fileid : "+fileid+",";
					json_str+="fileid1 : "+fileid1+",";
					json_str+="fileid2 : "+fileid2+",";
					json_str+="fileid3 : "+fileid3+",";
					json_str+="phone : "+phone+",";
					json_str+="score : "+temp_simscore+",";
					json_str+="group_name : "+group_name+",";
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
				LogIn.m_logger.info("Search.java" + json_str);
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
	public void deletetmpfiles(){
    	File f=new File(feature_fpath);
		if (f.isFile()==true) f.delete();
		f=new File(cropimg_fpath);
    }
}
