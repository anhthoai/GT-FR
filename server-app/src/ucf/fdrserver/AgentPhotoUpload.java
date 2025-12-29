package ucf.fdrserver;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
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
import org.json.JSONObject;

import com.mysql.jdbc.MySQLConnection;
import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * General Photo Upload
 */
@WebServlet("/AgentPhotoUpload")
public class AgentPhotoUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir, m_fileDir, m_tempDir, m_exePath;
	private int m_maxFileSize = 10*1024 * 1024;
	private int m_maxMemSize = 2*1024 * 1024;
	private File m_file;
	byte[] rsrc_imgbytes;
	public AgentPhotoUpload() {
		super();

	}
	public void init( ){
		m_rootDir = getServletContext().getRealPath("/");
		m_fileDir = m_rootDir+"data/";
		m_tempDir = m_rootDir+"temp/";
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String adminid = request.getParameter("adminid");
		String name = request.getParameter("name");
		String sex = request.getParameter("sex");
		String birth_str = request.getParameter("birth");
		String home = request.getParameter("home");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");
		String feature = request.getParameter("feature");
		String strMobile = request.getParameter("mobile");
		int deviceID = 0;
		if( strMobile != null )
			deviceID = Integer.parseInt(strMobile);
		String image_path ="";
		String fileDir = m_fileDir+adminid;
		File f = new File(fileDir);
		if (!f.exists())  f.mkdirs();
		image_path = fileDir+"/"+adminid+".jpg";
		
		java.io.PrintWriter out = response.getWriter( );
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(m_maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(fileDir));
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(m_maxFileSize);
		try{ 
			// Parse the request to get file items.
			List<FileItem> fileItems = upload.parseRequest(request);
			// Process the uploaded file items
			Iterator<FileItem> i = fileItems.iterator();
			int n = 0;
			while ( i.hasNext () ) 
			{
				FileItem fi = (FileItem)i.next();
				if ( !fi.isFormField () )
				{
					m_file = new File(image_path);
					fi.write( m_file );
					n++;
				}
			}
			if (n>0){
				//save register data to db
				DataInputStream distream;
				File in_f= new File(image_path);
				try {
					distream = globalUtil.getInputStream(image_path);
					rsrc_imgbytes=new byte[(int)in_f.length()];
					distream.read(rsrc_imgbytes);
					distream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				MySQLConnection con=MySQLConfig.getConnection();
				PreparedStatement pstmt;
				String query = "INSERT INTO person_info (name,sex,birthday,home,email,phone,person_img, feature, average) VALUES (?,?,?,?,?,?,?,?,?)";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1,name);
				pstmt.setString(2,sex);
				pstmt.setString(3,birth_str);
				pstmt.setString(4, home);
				pstmt.setString(5, email);
				pstmt.setString(6, phone);
				pstmt.setBytes(7, rsrc_imgbytes);
				pstmt.setString(8,feature);
				pstmt.setFloat(9,0);
				pstmt.executeUpdate();
				//
				//request part
				String strJson = "{result:ok}";
				JSONObject obj = new JSONObject(strJson);
				out.print(obj);
				//
			}
			else{
				String strJson = "{result:fail}";
				JSONObject obj = new JSONObject(strJson);
				out.print(obj);
			}
		}catch(Exception ex) {
			String strJson = "{result:fail}";
			JSONObject obj = new JSONObject(strJson);
			out.print(obj);
		}
	}

}
