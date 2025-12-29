package ucf.fdrserver;

import java.io.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.MySQLConnection;

import sun.misc.BASE64Decoder;
import ucf.firebase.FirebaseResponse;
import ucf.firebase.Notification;
import ucf.firebase.PushNotifHelper;
import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.mysql.jdbc.MySQLConnection;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * General Photo Upload
 */
@WebServlet("/SaveToDatabase")
public class SaveToDatabase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir, m_fileDir;
	private MySQLConnection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");

	private int m_maxFileSize = 10*1024 * 1024;
	private int m_maxMemSize = 1*1024 * 1024;
	private File m_file;
	
	byte[] imgbytes;
	private static Storage storage = null;

	public static String historyimgdir="";
	public String SERVICE_ACCOUNT_JSON_PATH;
	ArrayList<String> device_lists = new ArrayList<String>();
	public SaveToDatabase() {
		super();

	}
	public void init( ){
		m_rootDir = getServletContext().getRealPath("/");
		m_fileDir = m_rootDir+"data/";
		historyimgdir = m_rootDir+"historyimgs/";
		
    	MySQLConfig.userinfo_path=m_rootDir+"user_config.txt";
    	con = MySQLConfig.getConnection();
    	SERVICE_ACCOUNT_JSON_PATH = m_rootDir + "/FaceRecognition-966aee651648.json";//"D:\\My2019Work\\VietnamThoaiWork\\FDRServer\\FaceRecognition-966aee651648.json";
    	//storage = StorageOptions.getDefaultInstance().getService();
    	try {
			storage =
				    StorageOptions.newBuilder()
				        .setCredentials(
				            ServiceAccountCredentials.fromStream(
				                new FileInputStream(SERVICE_ACCOUNT_JSON_PATH)))
				        .build()
				        .getService();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
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
		if (birthday.equals(""))
			birthday = "1900-01-01";
		String home_address = request.getParameter("home_address");
		String email = request.getParameter("email");
		String city = request.getParameter("city");
		String country = request.getParameter("country");
		String group_name = request.getParameter("group_name");
		String to_push = request.getParameter("to_push");

		String[] parts = access.split(" ");
		String access_date = parts[0]; // 004
		
		String[] parts1 = parts[1].split(":");
		String access_time = parts1[0] + "-" + parts1[1] + "-" + parts1[2];
		
		String phone = request.getParameter("phone");
		File img_f = new File(historyimgdir);
		if (!img_f.exists())  img_f.mkdirs();
		String image_path=historyimgdir + "/" + access_date + "/";
//		
		File ff = new File(image_path);
		if (ff.exists()==false) ff.mkdirs();
//		else{
//			globalUtil.delFolderContent(image_path);
//		}
//		
		PrintWriter out = response.getWriter();
		String json_str="";
		PreparedStatement pstmt;
		
		m_file = new File( image_path + alarm_id + "_" + access_time + "_" + adminid + "_detected" + ".encode");
		String encoded_string = request.getParameter("detected");
		try {
            FileWriter fw = new FileWriter(m_file);
            fw.write(encoded_string);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
            String strJson = "{result : fail}";
 		    JSONObject obj = new JSONObject(strJson);
 		    out.print(obj);
 		    return;
        }
		if (to_push.equals("1"))
		{
			device_lists.clear();
	    	String sql = "select * from device_lists where adminid='" + adminid + "'";
	    	try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()==true){
					String token = rs.getString("token");
					device_lists.add(token);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (org.json.JSONException e){
				e.printStackTrace();	
			}
	    	if (device_lists.size() > 0)
	    	{
	    		//********** push notification********
				Notification notification = new Notification();
				for (int i = 0 ; i < device_lists.size(); i++)
				{
					notification.addDeviceToSend(device_lists.get(i));
				}
				
				notification.setTitle("FRResult");
				notification.setMessageBody("recognition result");

				BufferedReader br = new BufferedReader(new FileReader(image_path + alarm_id + "_" + access_time + "_" + adminid + "_detected" + ".encode"));
				try {
				    StringBuilder sb = new StringBuilder();
				    String line = br.readLine();

				    while (line != null) {
				        sb.append(line);
				        sb.append(System.lineSeparator());
				        line = br.readLine();
				    }
				    String everything = sb.toString();
				    BASE64Decoder decoder = new BASE64Decoder();
					byte[] imageByte = decoder.decodeBuffer(everything);
					//if (!img_f.exists()){
						DataOutputStream dos = globalUtil.getOutputStream(image_path + "/temp.jpg");
						dos.write(imageByte);
						dos.close();
					//}
				} finally {
				    br.close();
				}
				
				InputStream is = new FileInputStream(image_path + "/temp.jpg");
			    ByteArrayOutputStream os = new ByteArrayOutputStream();
			    byte[] readBuf = new byte[4096];
			    while (is.available() > 0) {
			      int bytesRead = is.read(readBuf);
			      os.write(readBuf, 0, bytesRead);
			    }
				// [START storageHelper]
			    //CloudStorageHelper storageHelper = new CloudStorageHelper();
				DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
			    DateTime dt = DateTime.now(DateTimeZone.UTC);
			    String dtString = dt.toString(dtf);
				BlobInfo blobInfo =
				        storage.create(
				            BlobInfo
				                .newBuilder("gt-face-notify.appspot.com", "image" + dtString + ".jpg")
				                // Modify access list to allow all users with link to read file
				                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
				                .build(),
				                os.toByteArray());
				String image_url = blobInfo.getMediaLink();
				notification.addDataAttribute("image", image_url); // custom data payload
				notification.addDataAttribute("time", access); // custom data payload
				notification.addDataAttribute("name", name); // custom data payload
				notification.addDataAttribute("group", group_name); // custom data payload

				FirebaseResponse fr = new PushNotifHelper().sendNotificationToDevice(notification);
				System.out.println(fr.getErrorMessage());
				System.out.println(fr.getFCMResponseCode());
				System.out.println(fr.getSuccessMessage());
				response.getWriter().append("Notification sent!!!").append(request.getContextPath());
				//***********************************
	    	}
		}
				
		m_file = new File( image_path + alarm_id + "_" + access_time + "_" + adminid + "_recognized.encode");
		encoded_string = request.getParameter("recognized");
		try {
            FileWriter fw = new FileWriter(m_file);
            fw.write(encoded_string);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
            String strJson = "{result : fail}";
 		    JSONObject obj = new JSONObject(strJson);
 		    out.print(obj);
 		    return;
        }
		m_file = new File( image_path + alarm_id + "_" + access_time + "_" + adminid + "_full.encode");
		encoded_string = request.getParameter("full");
		try {
            FileWriter fw = new FileWriter(m_file);
            fw.write(encoded_string);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
            String strJson = "{result : fail}";
 		    JSONObject obj = new JSONObject(strJson);
 		    out.print(obj);
 		    return;
        }
		try {
			pstmt = con.prepareStatement(query);
			
			pstmt.setString(1,alarm_id);
			pstmt.setString(2,access);
			pstmt.setInt(3,verify_state);
			pstmt.setInt(4, score);
			pstmt.setString(5, name);
			pstmt.setString(6, sex);
			pstmt.setString(7, birthday);
			pstmt.setString(8, home_address);
			pstmt.setString(9, city);
			pstmt.setString(10, country);
			pstmt.setString(11, email);
			pstmt.setString(12, phone);
//			pstmt.setBytes(13, rsrc_imgbytes);
//			pstmt.setBytes(14, rsrc_imgbytes1);
//			pstmt.setBytes(15, rsrc_imgbytes2);
			pstmt.setString(13, adminid);
			pstmt.setString(14, group_name);
			pstmt.executeUpdate();
			
			
			json_str="{'result' : 'ok'}";

			JSONObject obj = new JSONObject(json_str);
			out.print(obj);
			LogIn.m_logger.info("SaveToDatabase.java" + json_str);
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
	 
	 private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
		    BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		    Graphics2D g = resizedImage.createGraphics();
		    g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		    g.dispose();

		    return resizedImage;
		}
		
		// Inner class containing image information
		public static class ImageInformation {
		    public final int orientation;
		    public final int width;
		    public final int height;

		    public ImageInformation(int orientation, int width, int height) {
		        this.orientation = orientation;
		        this.width = width;
		        this.height = height;
		    }

		    public String toString() {
		        return String.format("%dx%d,%d", this.width, this.height, this.orientation);
		    }
		}
		public static ImageInformation readImageInformation(File imageFile)  throws IOException, MetadataException, ImageProcessingException {
		   
		    int orientation = 1;
		    try {
		    	Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
		 	    Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		 	    JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

		        orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		        
		        int width = jpegDirectory.getImageWidth();
			    int height = jpegDirectory.getImageHeight();
			    return new ImageInformation(orientation, width, height);
		    } catch (MetadataException me) {
		    	LogIn.m_logger.info("PhotoUpload.java" + " Could not get orientation");
		    	
		    }
		    
		    return null;
		}

}
