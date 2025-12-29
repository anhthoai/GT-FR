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
import java.io.FileWriter;
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

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.MySQLConnection;

import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * General Photo Upload
 */
@WebServlet("/UpdateDatabase")
public class UpdateDatabase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir, m_fileDir;
	private MySQLConnection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");

	private int m_maxFileSize = 10*1024 * 1024;
	private int m_maxMemSize = 1*1024 * 1024;
	private File m_file;

	public static String historyimgdir="";
	
	public UpdateDatabase() {
		super();

	}
	public void init( ){
		m_rootDir = getServletContext().getRealPath("/");
		m_fileDir = m_rootDir+"data/";
		historyimgdir = m_rootDir+"historyimgs/";
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
		String alarm_id = request.getParameter("alarm_id");
		String access = request.getParameter("access");
		int verify_state = Integer.parseInt(request.getParameter("verify_state"));
		int score = Integer.parseInt(request.getParameter("score"));
		String name = request.getParameter("name");
		String sex = request.getParameter("sex");
		String birthday = request.getParameter("birthday");
		String home_address = request.getParameter("home_address");
		String email = request.getParameter("email");
		String city = request.getParameter("city");
		String country = request.getParameter("country");
		
		String[] parts = access.split(" ");
		String access_date = parts[0]; // 004
		
		String[] parts1 = parts[1].split(":");
		String access_time = parts1[0] + "-" + parts1[1] + "-" + parts1[2];
		
		String phone = (request.getParameter("phone"));
		
		String image_path=historyimgdir + "/" + access_date + "/";
		
		PrintWriter out = response.getWriter();
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
	    // maximum size that will be stored in memory
		factory.setSizeThreshold(m_maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(image_path));
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax( m_maxFileSize );
	      try{ 
		      // Parse the request to get file items.
		      List<FileItem> fileItems = upload.parseRequest(request);
		      // Process the uploaded file items
		      Iterator<FileItem> i = fileItems.iterator();
		      int n = 0;
		      while ( i.hasNext () ) 
		      {
		         FileItem fi = (FileItem)i.next();
		         if ( !fi.isFormField ())
		         {
		        	 File f = new File( image_path + "temp.jpg");
		        	 fi.write( f );
		        	 
		        	 if (n == 0)
		        		 m_file = new File( image_path + alarm_id + "_" + access_time + "_" + adminid + "_recognized.encode");
		        	 else if (n == 1)
		        		 m_file = new File( image_path + alarm_id + "_" + access_time + "_" + adminid + "_full.encode");
		        	 
		        	 String encoded_string = encodeFileToBase64Binary(f);
		    		try {
		                FileWriter fw = new FileWriter(m_file);
		                fw.write(encoded_string);
		                fw.close();

		            } catch (IOException iox) {
		                //do stuff with exception
		                iox.printStackTrace();
		            }
			    		
		        	 n++;
		         }
		      }
		   }catch(Exception ex) {
			   String strJson = "{result : fail}";
			   JSONObject obj = new JSONObject(strJson);
			   out.print(obj);
		   }

		String json_str="";
		PreparedStatement pstmt;
		
//		DataInputStream distream;
//		File in_f= new File(image_path + "/d1.jpg");
//		try {
//			distream = globalUtil.getInputStream(image_path + "/d1.jpg");
//			rsrc_imgbytes=new byte[(int)in_f.length()];
//			distream.read(rsrc_imgbytes);
//			distream.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				json_str="{registerlists : fail}";
//				JSONObject obj = new JSONObject(json_str);
//				out.print(obj);
//				return;
//			}
//		
//		in_f= new File(image_path + "/d2.jpg");
//		try {
//			distream = globalUtil.getInputStream(image_path + "/d2.jpg");
//			rsrc_imgbytes1=new byte[(int)in_f.length()];
//			distream.read(rsrc_imgbytes1);
//			distream.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				json_str="{result : fail}";
//				JSONObject obj = new JSONObject(json_str);
//				out.print(obj);
//				return;
//			}

		
		try {
			pstmt = con.prepareStatement(query);
			
			pstmt.setInt(1,verify_state);
			pstmt.setInt(2,score);
			pstmt.setString(3, name);
			pstmt.setString(4, sex);
			pstmt.setString(5, birthday);
			pstmt.setString(6, home_address);
			pstmt.setString(7, city);
			pstmt.setString(8, country);
			pstmt.setString(9, email);
			pstmt.setString(10, phone);
//			pstmt.setBytes(11, rsrc_imgbytes);
//			pstmt.setBytes(12, rsrc_imgbytes1);
			pstmt.setString(11, alarm_id);
			pstmt.setString(12, access);
			pstmt.setString(13, adminid);
			pstmt.executeUpdate();
			
			
			json_str="{'result' : 'ok'}";

			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("UpdateDatabase.java" + json_str);
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
             encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
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
