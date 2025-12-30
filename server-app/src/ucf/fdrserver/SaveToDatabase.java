package ucf.fdrserver;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Base64;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import ucf.firebase.FirebaseResponse;
import ucf.firebase.Notification;
import ucf.firebase.PushNotifHelper;
import ucf.firebase.FcmV1Client;
import ucf.fdrssutil.MySQLConfig;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import java.sql.Connection;
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
import java.util.Map;



/**
 * General Photo Upload
 */
@WebServlet("/SaveToDatabase")
public class SaveToDatabase extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir;
	private Connection con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");

	// legacy field kept for compatibility with older code paths (unused now)
	@SuppressWarnings("unused")
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
		historyimgdir = m_rootDir+"historyimgs/";
		
    	MySQLConfig.userinfo_path=m_rootDir+"user_config.txt";
    	con = MySQLConfig.getConnection();
    	SERVICE_ACCOUNT_JSON_PATH = m_rootDir + "/FaceRecognition-966aee651648.json";//"D:\\My2019Work\\VietnamThoaiWork\\FDRServer\\FaceRecognition-966aee651648.json";
    	// IMPORTANT: GCS libs in WEB-INF/lib have historical version conflicts.
    	// We must not fail servlet init() just because cloud upload isn't available.
    	try {
			storage =
				    StorageOptions.newBuilder()
				        .setCredentials(
				            ServiceAccountCredentials.fromStream(
				                new FileInputStream(SERVICE_ACCOUNT_JSON_PATH)))
				        .build()
				        .getService();
		} catch (Throwable t) {
			// Includes NoSuchMethodError from dependency mismatch.
			storage = null;
			LogIn.m_logger.warn("[SaveToDatabase] Cloud Storage init failed; continuing without GCS upload.", t);
		}
    	
	}

	private static String buildBaseUrl(HttpServletRequest request) {
		// Respect reverse proxy headers if present (common for HTTPS frontends)
		String proto = request.getHeader("X-Forwarded-Proto");
		if (proto == null || proto.isEmpty()) proto = request.getScheme();

		String host = request.getHeader("X-Forwarded-Host");
		if (host == null || host.isEmpty()) host = request.getHeader("Host");
		if (host == null || host.isEmpty()) host = request.getServerName();

		// If Host already includes port, keep it.
		if (host.contains(":")) {
			return proto + "://" + host;
		}

		int port = request.getServerPort();
		String xfPort = request.getHeader("X-Forwarded-Port");
		if (xfPort != null && !xfPort.isEmpty()) {
			try { port = Integer.parseInt(xfPort.trim()); } catch (NumberFormatException ignored) {}
		}

		// Common reverse-proxy case: TLS is terminated upstream (443) but Tomcat sees 80 internally.
		// If we're building an https URL and we see port=80 with no forwarded port, treat it as default (omit).
		if ("https".equalsIgnoreCase(proto) && port == 80 && (xfPort == null || xfPort.isEmpty())) {
			port = 443;
		}

		boolean defaultPort = ("http".equalsIgnoreCase(proto) && port == 80) || ("https".equalsIgnoreCase(proto) && port == 443);
		return proto + "://" + host + (defaultPort ? "" : (":" + port));
	}

	private static boolean writeEncodeAndJpg(String encodedBase64, String encodePath, String jpgPath) {
		try {
			if (encodedBase64 == null) return false;
			try (FileWriter fw = new FileWriter(new File(encodePath))) {
				fw.write(encodedBase64);
			}
			String sanitized = encodedBase64.replaceAll("\\s+", "");
			if (sanitized.isEmpty()) return false;
			byte[] imageBytes = Base64.getDecoder().decode(sanitized);
			try (FileOutputStream fos = new FileOutputStream(jpgPath)) {
				fos.write(imageBytes);
			}
			return true;
		} catch (Throwable t) {
			System.out.println("[SaveToDatabase] Failed to write encode/jpg: " + t);
			return false;
		}
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setCharacterEncoding("UTF-8");
		// Refresh connection (servlet may live long; DB can drop idle connections)
		con = MySQLConfig.getConnection();

		String adminid = request.getParameter("adminid");
		String query = request.getParameter("query");
		String alarm_id = request.getParameter("alarm_id");
		String access = request.getParameter("access");

		int verify_state = 0;
		String verifyStateParam = request.getParameter("verify_state");
		if (verifyStateParam != null && !verifyStateParam.isEmpty()) {
			try { verify_state = Integer.parseInt(verifyStateParam); } catch (NumberFormatException ignored) {}
		}

		int score = 0;
		String scoreParam = request.getParameter("score");
		if (scoreParam != null && !scoreParam.isEmpty()) {
			try { score = Integer.parseInt(scoreParam); } catch (NumberFormatException ignored) {}
		}

		String name = request.getParameter("name");
		String sex = request.getParameter("sex");
		String birthday = request.getParameter("birthday");
		if (birthday == null || birthday.equals(""))
			birthday = "1900-01-01";
		String home_address = request.getParameter("home_address");
		String email = request.getParameter("email");
		String city = request.getParameter("city");
		String country = request.getParameter("country");
		String group_name = request.getParameter("group_name");
		String to_push = request.getParameter("to_push");
		if (to_push == null || to_push.isEmpty()) to_push = "0";
		System.out.println("[SaveToDatabase] request adminid=" + adminid + " group=" + group_name + " to_push=" + to_push);

		// Use server-defined insert SQL if client doesn't provide one.
		if (query == null || query.trim().isEmpty()) {
			query = "INSERT INTO alarm_info (alarm_id,access,verify_state, score,name, sex, birthday, home_address, city, country, email, phone, adminid, group_name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		}

		if (adminid == null) adminid = "";
		if (alarm_id == null) alarm_id = "";
		if (access == null) access = "";

		String[] parts = access.split(" ");
		if (parts.length < 2) {
			// Fallback to avoid crashing on malformed access time.
			parts = new String[] { "unknown-date", "00:00:00.000" };
		}
		String access_date = parts[0]; // 004
		
		String[] parts1 = parts[1].split(":");
		String access_time = (parts1.length >= 3) ? (parts1[0] + "-" + parts1[1] + "-" + parts1[2]) : "00-00-00.000";
		
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
		
		String fileBase = alarm_id + "_" + access_time + "_" + adminid;
		String detectedEncodePath = image_path + fileBase + "_detected.encode";
		String recognizedEncodePath = image_path + fileBase + "_recognized.encode";
		String fullEncodePath = image_path + fileBase + "_full.encode";
		String detectedJpgPath = image_path + fileBase + "_detected.jpg";
		String recognizedJpgPath = image_path + fileBase + "_recognized.jpg";
		String fullJpgPath = image_path + fileBase + "_full.jpg";

		String encoded_string = request.getParameter("detected");
		if (!writeEncodeAndJpg(encoded_string, detectedEncodePath, detectedJpgPath)) {
			String strJson = "{result : fail}";
			JSONObject obj = new JSONObject(strJson);
			out.print(obj);
			return;
		}
		// (push happens later, after we have written *_recognized and *_full)
				
		encoded_string = request.getParameter("recognized");
		// For unknown faces, "recognized" may be empty; still create file if possible.
		writeEncodeAndJpg(encoded_string, recognizedEncodePath, recognizedJpgPath);

		encoded_string = request.getParameter("full");
		writeEncodeAndJpg(encoded_string, fullEncodePath, fullJpgPath);
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

			// Push notification AFTER successful save so we can include the "recognized" image.
			if ("1".equals(to_push))
			{
				adminid = adminid == null ? "" : adminid.trim();

				// Load tokens and de-duplicate (some DBs may contain duplicate rows)
				java.util.LinkedHashSet<String> tokenSet = new java.util.LinkedHashSet<>();
		    	String sql = "select token from device_lists where adminid='" + adminid + "'";
		    	try {
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql);
					while (rs.next()==true){
						String token = rs.getString("token");
						if (token != null) {
							token = token.trim();
							if (!token.isEmpty()) tokenSet.add(token);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				ArrayList<String> tokens = new ArrayList<>(tokenSet);
				System.out.println("[SaveToDatabase] Push requested. adminid=" + adminid
						+ " group=" + group_name
						+ " tokens=" + tokens.size()
						+ " storage=" + (storage != null)
						+ " verify_state=" + verify_state);

		    	if (tokens.size() > 0)
		    	{
					final String safeGroup = group_name == null ? "" : group_name.trim();
					final String safeName = name == null ? "" : name.trim();
					final String notifTitle =
							(safeGroup.isEmpty() ? "" : ("[" + safeGroup + "] "))
									+ (safeName.isEmpty() ? "Unknown" : safeName);
					final String notifBody = (access == null ? "" : access.trim());

					Map<String, String> dataPayload = new HashMap<>();
					dataPayload.put("time", access == null ? "" : access);
					dataPayload.put("name", safeName);
					dataPayload.put("group", safeGroup);
					dataPayload.put("image", "");
					dataPayload.put("alarm_id", alarm_id == null ? "" : alarm_id);

					// Prefer local JPG URL (more reliable than GCS).
					String baseUrl = buildBaseUrl(request);
					String relDir = "historyimgs/" + access_date + "/";
					String chosenRel = relDir + fileBase + (verify_state == 1 ? "_recognized.jpg" : "_detected.jpg");
					String localUrl = baseUrl + request.getContextPath() + "/" + chosenRel;
					File chosenFile = new File(image_path + fileBase + (verify_state == 1 ? "_recognized.jpg" : "_detected.jpg"));
					if (chosenFile.exists() && chosenFile.length() > 0) {
						dataPayload.put("image", localUrl);
						System.out.println("[SaveToDatabase] image(local)=" + localUrl + " bytes=" + chosenFile.length());
					} else {
						System.out.println("[SaveToDatabase] image(local) missing file=" + chosenFile.getAbsolutePath());
					}

					// Upload image to GCS if available (use recognized image for verify_state=1)
					if (storage != null) {
						try {
							String encodePath =
									image_path + alarm_id + "_" + access_time + "_" + adminid
											+ (verify_state == 1 ? "_recognized.encode" : "_detected.encode");

							BufferedReader br = new BufferedReader(new FileReader(encodePath));
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

								ByteArrayOutputStream os = new ByteArrayOutputStream();
								os.write(imageByte);

								DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
							    DateTime dt = DateTime.now(DateTimeZone.UTC);
							    String dtString = dt.toString(dtf);
							    String objectName = "image" + dtString + ".jpg";
								storage.create(
								            BlobInfo
								                .newBuilder("gt-face-notify.appspot.com", objectName)
								                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
								                .build(),
								                os.toByteArray());
								String image_url = "https://storage.googleapis.com/gt-face-notify.appspot.com/" + objectName;
								// Keep localUrl as default, but if GCS works we can also prefer it.
								dataPayload.put("image", image_url);
							} finally {
							    br.close();
							}
						} catch (Throwable t) {
							System.out.println("[SaveToDatabase] GCS upload failed (continuing without image): " + t);
						}
					}

					boolean sentOk = false;
					try {
						FcmV1Client v1 = FcmV1Client.fromServiceAccountJsonPath(SERVICE_ACCOUNT_JSON_PATH);
						int okCount = 0;
						for (String token : tokens) {
							FcmV1Client.SendResult r = v1.sendToToken(token, notifTitle, notifBody, dataPayload);
							System.out.println("[SaveToDatabase] FCM v1 token=" + token + " http=" + r.httpCode + " err=" + r.errorBody);
							if (r.httpCode >= 200 && r.httpCode < 300) okCount++;
						}
						System.out.println("[SaveToDatabase] FCM v1 okCount=" + okCount + " total=" + tokens.size());
						sentOk = okCount > 0;
					} catch (Throwable t) {
						System.out.println("[SaveToDatabase] FCM v1 send failed: " + t);
					}

					if (!sentOk) {
						Notification notification = new Notification();
						for (String token : tokens) notification.addDeviceToSend(token);
						notification.setTitle(notifTitle);
						notification.setMessageBody(notifBody);
						for (Map.Entry<String, String> e : dataPayload.entrySet()) notification.addDataAttribute(e.getKey(), e.getValue());
						FirebaseResponse fr = new PushNotifHelper().sendNotificationToDevice(notification);
						System.out.println("[SaveToDatabase] Legacy FCM http=" + fr.getFCMResponseCode()
								+ " ok=" + fr.getSuccessMessage()
								+ " err=" + fr.getErrorMessage());
					}
		    	}
			}
			
			
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
	 // (unused legacy helpers removed)
		
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
