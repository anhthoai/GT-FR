package ucf.fdrserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ucf.fdrserver.LogIn;
import ucf.fdrssutil.MySQLConfig;
/**
 * Servlet implementation class GetMobile
 */
@WebServlet("/DownloadFRApp")
public class DownloadFRApp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String root_path, fileDir;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadFRApp() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void init( ){
    	root_path = getServletContext().getRealPath("/");
    	fileDir = root_path+"FRagent/";
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mobile_type = request.getParameter("FRAgent_type");
		if (mobile_type.compareTo("fr_app")==0){
			ServletContext cntx= request.getServletContext();
		    // Get the absolute path of the image
		    String filename = fileDir+"/FRagent.zip";
		    // retrieve mimeType dynamically
		    String mime = cntx.getMimeType(filename);
		    if (mime == null) {
		       response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		       return;
		    }

		    response.setContentType(mime);
		    response.setHeader("Content-Disposition","attachment; filename=FRagent.zip");
		    File file = new File(filename);
		    response.setContentLength((int)file.length());

		    FileInputStream in = new FileInputStream(file);
		    OutputStream out = response.getOutputStream();

		    // Copy the contents of the file to the output stream
		    byte[] buf = new byte[1024];
		    int count = 0;
		    while ((count = in.read(buf)) >= 0) {
		       out.write(buf, 0, count);
		    }
		    out.close();
		    in.close();
		    LogIn.m_logger.info("Requested FRAgent app from:  " + request.getRemoteAddr());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
