package ucf.fdrserver;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Base64;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * General Photo Upload
 */
@WebServlet("/PersonSearch")
public class PersonSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir, m_fileDir;
	private MySQLConnection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	byte[] search_imgbytes;
	public PersonSearch() {
		super();

	}
	public void init( ){
		m_rootDir = getServletContext().getRealPath("/");
		m_fileDir = m_rootDir+"data/";
    	MySQLConfig.userinfo_path=m_rootDir+"user_config.txt";
    	con = MySQLConfig.getConnection();  
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setCharacterEncoding("UTF-8");
		String adminid = request.getParameter("adminid");
		String query = request.getParameter("query");
		String feature = request.getParameter("feature");
		String image_path ="";
		String fileDir = m_fileDir+adminid;
		File f = new File(fileDir);
		if (!f.exists())  f.mkdirs();
		image_path = fileDir+"/"+adminid+"_search.jpg";
		//mysql connect part
		String res_name="", res_sex="", res_birth="",res_home="", res_email="", res_phone_str="", res_city = "", res_country = "";;
		String res_feature="",res_average_str="", res_group="";
		String encodstring="", fullencodstring="";
		String res_phone="";
		float res_average=0;
		double temp_simscore=0, sim_score=0;
		int counter=0;
		PrintWriter out = response.getWriter();
		String json_str="";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()==true){
				String admin_id_person = rs.getString("adminid");
				if (!admin_id_person.equals("") && admin_id_person.compareTo(adminid) != 0) // person was not created by this agent
					continue;
				res_feature=rs.getString("feature");
				temp_simscore = globalUtil.GetSimilarity(feature, res_feature);
				if(temp_simscore>95){
					if(temp_simscore>sim_score){
						sim_score=temp_simscore;
					}
				}
				res_feature=rs.getString("feature1");
				temp_simscore = globalUtil.GetSimilarity(feature, res_feature);
				if(temp_simscore>95){
					if(temp_simscore>sim_score){
						sim_score=temp_simscore;
					}
				}
				res_feature=rs.getString("feature2");
				temp_simscore = globalUtil.GetSimilarity(feature, res_feature);
				if(temp_simscore>95){
					if(temp_simscore>sim_score){
						sim_score=temp_simscore;
					}
				}

				if (sim_score < 95) continue;
				res_name = rs.getString("name");
				res_sex = rs.getString("sex");
				if (res_sex == null) res_sex = "";
				Date birth=rs.getDate("birthday");
				if (birth != null) 
					res_birth = formatter.format(birth);
				else
					res_birth = "1900-01-01";
				res_home = rs.getString("home");
				if (res_home==null) res_home="";
				res_email = rs.getString("email");
				if (res_email==null) res_email="";
				res_phone = rs.getString("phone");
				res_city = rs.getString("city");
				if (res_city==null) res_city="";
				res_country = rs.getString("country");
				if (res_country==null) res_country="";
				res_group = rs.getString("group_name");
				if (res_group==null)
					res_group = "Nogroup";
				byte [] imgbytes;
				Blob datablob = rs.getBlob("person_img");
				if (datablob != null)
				{
					InputStream is = datablob.getBinaryStream();
					imgbytes = new byte[(int)datablob.length()];
					is.read(imgbytes);
					is.close();
					DataOutputStream dos = globalUtil.getOutputStream(image_path);
					dos.write(imgbytes);
					dos.close();
					
					File img_f =  new File(image_path);
			        encodstring = encodeFileToBase64Binary(img_f);
				}
				datablob = rs.getBlob("person_img1");
				if (datablob != null)
				{
					InputStream is = datablob.getBinaryStream();
					imgbytes = new byte[(int)datablob.length()];
					is.read(imgbytes);
					is.close();
					DataOutputStream dos = globalUtil.getOutputStream(image_path);
					dos.write(imgbytes);
					dos.close();
					
					File img_f =  new File(image_path);
			        encodstring = encodeFileToBase64Binary(img_f);
				}
				datablob = rs.getBlob("person_img2");
				if (datablob != null)
				{
					InputStream is = datablob.getBinaryStream();
					imgbytes = new byte[(int)datablob.length()];
					is.read(imgbytes);
					is.close();
					DataOutputStream dos = globalUtil.getOutputStream(image_path);
					dos.write(imgbytes);
					dos.close();
					
					File img_f =  new File(image_path);
			        encodstring = encodeFileToBase64Binary(img_f);
				}
				
				
		        
		        datablob = rs.getBlob("person_full_img");
		        if (datablob == null)
				{
					String user_image_path = m_rootDir + "/assets/img/face.png";
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
					DataOutputStream dos = globalUtil.getOutputStream(image_path);
					dos.write(imgbytes);
					dos.close();
					File img_f =  new File(image_path);
					fullencodstring = encodeFileToBase64Binary(img_f);
				}
				else
				{
					InputStream is = datablob.getBinaryStream();
					imgbytes = new byte[(int)datablob.length()];
					is.read(imgbytes);
					is.close();
					DataOutputStream dos = globalUtil.getOutputStream(image_path);
					dos.write(imgbytes);
					dos.close();
					File img_f =  new File(image_path);
					fullencodstring = encodeFileToBase64Binary(img_f);
				}
				
		        
				if (counter>0) json_str+=",";
				json_str+="'name"+counter+"' : '"+res_name+"',";
				json_str+="'sex"+counter+"' : '"+res_sex+"',";
				json_str+="'birth"+counter+"' : '"+res_birth+"',";
				json_str+="'home"+counter+"' : '"+res_home+"',";
				json_str+="'email"+counter+"' : '"+res_email+"',";
				json_str+="'phone"+counter+"' : '"+res_phone+"',";
				json_str+="'simscore"+counter+"' : '"+sim_score+"',";
				json_str+="'imgstr"+counter+"' : '"+encodstring+"',";
				json_str+="'fullimgstr"+counter+"' : '"+fullencodstring+"',";
				json_str+="'city"+counter+"' : '"+res_city+"',";
				json_str+="'country"+counter+"' : '"+res_country+"',";
				json_str+="'group_name"+counter+"' : '"+res_group+"'";
				counter++;
			}
			json_str+=",num:'"+counter+"'}";	
			json_str="{'result' : 'ok',"+json_str;
			if (counter==0){
				json_str="{'result' : 'none'}";
			}
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("PersonSearch.java" + json_str);
		} catch (SQLException e) {
			json_str="{'result' : 'fail'}";
			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			e.printStackTrace();
		} catch (org.json.JSONException e){
			LogIn.m_logger.debug(json_str);
			e.printStackTrace();
		}
		
	}
	 private static String encodeFileToBase64Binary(File file){
         String encodedfile = null;
         try {
             FileInputStream fileInputStreamReader = new FileInputStream(file);
             byte[] bytes = new byte[(int)file.length()];
             fileInputStreamReader.read(bytes);
             encodedfile = Base64.getEncoder().encodeToString(bytes);
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }

         return encodedfile;
     }

}
