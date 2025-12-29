package ucf.fdrssutil;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.json.JSONObject;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.MySQLConnection;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import ucf.fdrserver.LogIn;


public class MySQLConfig {
	private static final long serialVersionUID = 1L;
    public static MySQLConnection mConnection;
	public static String userinfo_path;
    public static String db_url= "jdbc:mysql://localhost:3306/";
    public final static String unicodeConnString = "?useUnicode=true&characterEncoding=UTF-8";
	public static String db_name ="frdssdata";
	public static String user="frdssdata";
	public static String password="frdssdata";
	public static double threshold=50;
	public static int enablelog = 0;
	public static int install = 1;
	/**
	 * Retrieves the current connection.
	 * @return
	 */
	public static MySQLConnection getConnection()
	{
		File f = new File(userinfo_path);
		if (f.exists()==true)
			readUserInfo(userinfo_path);
		try {
			if (mConnection == null ){
				Class.forName("com.mysql.jdbc.Driver");
				mConnection = (MySQLConnection) DriverManager.getConnection(db_url+db_name+unicodeConnString, user, password);
				LogIn.m_logger.info("Connected to MySQL database successfully!(mConnection == null)");
			}else if(mConnection.isClosed()){
				Class.forName("com.mysql.jdbc.Driver");
				mConnection = (MySQLConnection) DriverManager.getConnection(db_url+db_name+unicodeConnString, user, password);
				LogIn.m_logger.info("Connected to MySQL database successfully!(mConnection.isClosed())");
			}
			
			if (install ==1){
				CreateTables(mConnection);
			}
			
			return mConnection;		
		}		
		catch (MySQLSyntaxErrorException e){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				mConnection = (MySQLConnection) DriverManager.getConnection(db_url, user, password);
				Statement stmt = mConnection.createStatement();
				
			    String sql = "CREATE DATABASE IF NOT EXISTS " + db_name+ " DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci";
			    stmt.executeUpdate(sql);
			    LogIn.m_logger.info("[MySQLConfig.java] SQL = "+sql);
			    
			    mConnection = (MySQLConnection) DriverManager.getConnection(db_url+db_name+unicodeConnString, user, password);
				CreateTables(mConnection);
	
				return mConnection;
			} catch (ClassNotFoundException |SQLException e1 ) {
				e.printStackTrace();
				return null;
			}
		} catch (CommunicationsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogIn.m_logger.debug("Failed to connect to MySQL database!");
			e.printStackTrace();
		}		
		return null;
	}
	
	public static void closeConnection()
	{
		try {
			if (mConnection !=null){
				mConnection.close();
				LogIn.m_logger.info("MySQL database connection closed successfully!");
			}
		} catch (MySQLSyntaxErrorException e){
			e.printStackTrace();
		} catch (CommunicationsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogIn.m_logger.debug("Failed to disconnect from MySQL database!");
			e.printStackTrace();
		}		
	}
	@SuppressWarnings({ "deprecation" })
	public static void readUserInfo(String fn)
	{
		DataInputStream dis;
		try {
			dis = globalUtil.getInputStream(fn);
			String line = dis.readLine().trim();
			String []arr=line.split("=");
			if (arr.length==2) db_url = arr[1];
			
			line = dis.readLine().trim();			
			arr=line.split("=");
			if (arr.length==2) db_name = arr[1];
			
			line = dis.readLine().trim();			
			arr=line.split("=");
			if (arr.length==2) user = arr[1];
			
			line = dis.readLine().trim();
			arr = line.split("=");
			if (arr.length==2) password = arr[1];
			else password="";
			
			line = dis.readLine().trim();
			arr = line.split("=");
			if (arr.length==2) threshold = Double.parseDouble(arr[1]);
			else threshold=0;		
			
			line = dis.readLine().trim();
			arr = line.split("=");
			if (arr.length==2) enablelog = Integer.parseInt(arr[1]);
			else enablelog = 0;	
			
			line = dis.readLine().trim();
			arr = line.split("=");
			if (arr.length==2) install = Integer.parseInt(arr[1]);
			else install = 1;	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	private static void CreateTables(MySQLConnection con){
		try{
			Statement stmt = con.createStatement();
		    
		    String sql="CREATE TABLE IF NOT EXISTS users_info ("
		    		  +"id int NOT NULL AUTO_INCREMENT,"
		    		  +"fullname text,"
		    		  +"email text,"
		    		  +"address text,"
		    		  +"city text,"
		    		  +"country text,"
		    		  +"userid text,"
		    		  +"password text,"
		    		  +"user_img longblob,"
		    		  +"adminid text,"
		    		  +"member varchar(20) DEFAULT 'user',"
		    		  +"status tinyint(4) DEFAULT '0',"
		    		  +" PRIMARY KEY (id)"
		    		  +") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1;";
		    stmt.executeUpdate(sql);
//		    sql="CREATE TABLE IF NOT EXISTS agents_info ("
//		    		  +"id int NOT NULL AUTO_INCREMENT,"
//		    		  +"fullname text,"
//		    		  +"email text,"
//		    		  +"address text,"
//		    		  +"city text,"
//		    		  +"country text,"
//		    		  +"userid text,"
//		    		  +"password text,"
//		    		  +"user_img longblob,"
//		    		  +"member varchar(20) DEFAULT 'agent',"
//		    		  +"status tinyint(4) DEFAULT '0',"
//		    		  +" PRIMARY KEY (id)"
//		    		  +") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1;";
//		    stmt.executeUpdate(sql);
			//LogIn.m_logger.info("[MySQLConfig.java] SQL = "+sql);
			sql = "CREATE TABLE IF NOT EXISTS person_info ("
					+" id int NOT NULL AUTO_INCREMENT,"
					+" name varchar(100) DEFAULT NULL,"
					+" sex varchar(20) DEFAULT NULL,"
					+" birthday datetime DEFAULT NULL,"
					+" home text,"
					+" city text,"
					+" country text,"
					+" email varchar(100) DEFAULT NULL,"
					+" phone varchar(50) DEFAULT NULL,"
					+" person_img longblob,"
					+" feature text,"
					+" person_img1 longblob,"
					+" feature1 text,"
					+" person_img2 longblob,"
					+" feature2 text,"
					+" person_full_img longblob,"
					+" adminid text,"
					+" group_name text,"
					+" average float,"
					+" PRIMARY KEY (id)"
					+") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1;";
			stmt.executeUpdate(sql);
			
//			sql = "CREATE TABLE IF NOT EXISTS alarm_info (id int NOT NULL AUTO_INCREMENT, alarm_id text,"
//                    +"access datetime, verify_state int(2),score int(4), "
//                    +"name text, sex text, birthday datetime, home_address text, city text, country text, email text, "
//                    +"phone int(22), detected_img longblob, verify_img longblob,verify_fullimg longblob, adminid text, PRIMARY KEY (id)) "
//                    +"ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci  AUTO_INCREMENT=1;";
			sql = "CREATE TABLE IF NOT EXISTS alarm_info (id int NOT NULL AUTO_INCREMENT, alarm_id text,"
                    +"access datetime(3), verify_state int(2),score int(4), "
                    +"name text, sex text, birthday datetime, home_address text, city text, country text, email text, "
                    +"phone varchar(50), adminid text, group_name text, PRIMARY KEY (id)) "
                    +"ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci  AUTO_INCREMENT=1;";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS group_info (id int NOT NULL AUTO_INCREMENT, name text, adminid text, PRIMARY KEY (id)) "
                    +"ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci  AUTO_INCREMENT=1;";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS device_lists (id int NOT NULL AUTO_INCREMENT, token text, adminid text, PRIMARY KEY (id)) "
                    +"ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci  AUTO_INCREMENT=1;";
			stmt.executeUpdate(sql);
			
			//LogIn.m_logger.info("[MySQLConfig.java] SQL = "+sql);

			sql = "SELECT * FROM users_info WHERE member = 'agent'";
			ResultSet rs =	stmt.executeQuery(sql);
			if (!rs.first()){
				DataInputStream distream;
				String user_image_path = LogIn.root_path + "/assets/img/thoai.jpg";
				File in_f= new File(user_image_path);
				byte[] user_imgbytes;
				try {
					distream = globalUtil.getInputStream(user_image_path);
					user_imgbytes =new byte[(int)in_f.length()];
					distream.read(user_imgbytes);
					distream.close();
					} catch (IOException e) {
						return;
					}
				sql = "INSERT INTO users_info (fullname, email, address, city, country, userid, password, user_img, adminid, member, status)"
							   +" VALUES(?,?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
				PreparedStatement pstmt = con.prepareStatement(sql);
				pstmt.setString(1,"agent");
				pstmt.setString(2,"Thoai@gmail.com");
				pstmt.setString(3, "Hanoi");
				pstmt.setString(4, "Hanoi");
				pstmt.setString(5, "Vietnam");
				pstmt.setString(6, "agent");
				pstmt.setString(7, "agent");
				pstmt.setBytes(8, user_imgbytes);
				pstmt.setString(9,"");
				pstmt.setString(10,"agent");
				pstmt.setInt(11,1);
				pstmt.executeUpdate();
				
				//stmt.executeUpdate(sql);
			}
			
			sql = "CREATE TABLE IF NOT EXISTS camera_info (id int NOT NULL AUTO_INCREMENT, user_id text,"
                    +"adminid text, camera1_name text, camera1_url text, camera2_name text, camera2_url text,"
					+ "camera3_name text, camera3_url text,camera4_name text, camera4_url text, PRIMARY KEY (id)) "
                    +"ENGINE = InnoDB DEFAULT CHARSET = utf8 COLLATE = utf8_unicode_ci  AUTO_INCREMENT=1;";
			stmt.executeUpdate(sql);
			
		}catch(SQLException e) {
			// TODO Auto-generated catch block
			if (mConnection!=null){
				try {
					mConnection.close();// For recreate tables later connection request
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			LogIn.m_logger.debug("Failed to connect to MySQL database!");
			e.printStackTrace();
		}	
	}
}
