package ucf.fdrssutil;

import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ucf.fdrserver.LogIn;


public class MySQLConfig {
	private static final long serialVersionUID = 1L;
    public static Connection mConnection;
	public static String userinfo_path;
    public static String db_url= "jdbc:mysql://localhost:3306/";
    // MySQL Connector/J 8+ (Java 17 compatible) connection parameters.
    // If db_url already contains '?', these are not appended.
    public final static String connParams = "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
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
	public static Connection getConnection()
	{
		File f = new File(userinfo_path);
		if (f.exists()==true)
			readUserInfo(userinfo_path);

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			if (mConnection == null || mConnection.isClosed()){
				mConnection = DriverManager.getConnection(buildDbJdbcUrl(), user, password);
				LogIn.m_logger.info("Connected to MySQL database successfully!");
			}

			if (install == 1){
				CreateTables(mConnection);
			}

			return mConnection;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// If DB doesn't exist yet, try to create it and reconnect (error 1049).
			if (isUnknownDatabase(e)) {
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					try (Connection serverCon = DriverManager.getConnection(buildServerJdbcUrl(), user, password);
					     Statement stmt = serverCon.createStatement()) {
						String sql = "CREATE DATABASE IF NOT EXISTS " + db_name + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
						stmt.executeUpdate(sql);
						LogIn.m_logger.info("[MySQLConfig] SQL = " + sql);
					}

					mConnection = DriverManager.getConnection(buildDbJdbcUrl(), user, password);
					if (install == 1) CreateTables(mConnection);
					return mConnection;
				} catch (Exception e1) {
					LogIn.m_logger.debug("Failed to create/connect database after unknown database error.");
					e1.printStackTrace();
					return null;
				}
			}

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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogIn.m_logger.debug("Failed to disconnect from MySQL database!");
			e.printStackTrace();
		}		
	}

	private static String buildDbJdbcUrl() {
		return appendParams((db_url + db_name).trim());
	}

	private static String buildServerJdbcUrl() {
		return appendParams(db_url.trim());
	}

	private static String appendParams(String url) {
		if (url == null) return null;
		if (url.contains("?")) return url;
		return url + connParams;
	}

	private static boolean isUnknownDatabase(SQLException e) {
		return e != null && e.getErrorCode() == 1049;
	}
	@SuppressWarnings({ "deprecation" })
	public static void readUserInfo(String fn)
	{
		try {
			// Support both legacy config (fixed line order) and key-based config.
			// Preferred format (key-based):
			//   host=localhost
			//   port=3306
			//   database=frdssdata
			//   username=frdssdata
			//   password=frdssdata
			// Optional:
			//   db_url=jdbc:mysql://localhost:3306/
			//   db_name=frdssdata
			//   user=frdssdata
			//   threshold=50
			//   enablelog=1
			//   install=1

			Map<String, String> kv = new HashMap<>();
			try (DataInputStream dis = globalUtil.getInputStream(fn);
			     BufferedReader br = new BufferedReader(new InputStreamReader(dis, StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.startsWith("#") || line.startsWith("//")) continue;
					int idx = line.indexOf('=');
					if (idx <= 0) continue;
					String key = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
					String value = line.substring(idx + 1).trim();
					kv.put(key, value);
				}
			}

			// db_url / db_name / user / password (legacy keys)
			String cfgDbUrl = kv.get("db_url");
			String cfgDbName = kv.getOrDefault("db_name", kv.get("database"));
			// Support legacy keys used by older deployments: mysql_user / mysql_password
			String cfgUser = firstNonNull(
					kv.get("user"),
					kv.get("username"),
					kv.get("db_user"),
					kv.get("mysql_user")
			);
			String cfgPass = firstNonNull(
					kv.get("password"),
					kv.get("db_password"),
					kv.get("mysql_password")
			);

			// host/port based (preferred if db_url missing)
			String host = kv.getOrDefault("host", kv.get("db_host"));
			String port = kv.getOrDefault("port", kv.get("db_port"));

			if (cfgDbUrl != null && !cfgDbUrl.isEmpty()) {
				db_url = cfgDbUrl;
			} else if (host != null && !host.isEmpty()) {
				String p = (port != null && !port.isEmpty()) ? port : "3306";
				db_url = "jdbc:mysql://" + host + ":" + p + "/";
			}

			if (cfgDbName != null && !cfgDbName.isEmpty()) db_name = cfgDbName;
			if (cfgUser != null && !cfgUser.isEmpty()) user = cfgUser;
			// IMPORTANT: do not overwrite password with empty string from config parsing
			if (cfgPass != null && !cfgPass.isEmpty()) password = cfgPass;

			String cfgThreshold = firstNonNull(kv.get("threshold"), kv.get("similarity threshold"));
			if (cfgThreshold != null && !cfgThreshold.isEmpty()) {
				try { threshold = Double.parseDouble(cfgThreshold); } catch (NumberFormatException ignored) {}
			}

			String cfgEnableLog = kv.get("enablelog");
			if (cfgEnableLog != null && !cfgEnableLog.isEmpty()) {
				try { enablelog = Integer.parseInt(cfgEnableLog); } catch (NumberFormatException ignored) {}
			}

			String cfgInstall = kv.get("install");
			if (cfgInstall != null && !cfgInstall.isEmpty()) {
				try { install = Integer.parseInt(cfgInstall); } catch (NumberFormatException ignored) {}
			}

			// Helpful debug (no password)
			LogIn.m_logger.info("[MySQLConfig] Loaded DB config from: " + fn
					+ " | url=" + db_url
					+ " | db=" + db_name
					+ " | user=" + user);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	

	private static String firstNonNull(String... values) {
		if (values == null) return null;
		for (String v : values) {
			if (v != null) return v;
		}
		return null;
	}
	
	private static void CreateTables(Connection con){
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

			// MySQL Connector/J 8 returns TYPE_FORWARD_ONLY by default -> use rs.next() (not rs.first()).
			sql = "SELECT 1 FROM users_info WHERE member = 'agent' LIMIT 1";
			ResultSet rs =	stmt.executeQuery(sql);
			if (!rs.next()){
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
			// Table creation failure should not close the shared connection; let the app continue.
			LogIn.m_logger.debug("Failed to create/verify tables in MySQL database!");
			e.printStackTrace();
		}	
	}
}
