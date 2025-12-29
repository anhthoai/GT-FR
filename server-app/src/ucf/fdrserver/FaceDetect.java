package ucf.fdrserver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ucf.fdrssutil.globalUtil;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Image;
/**
 * Servlet implementation class FaceDetect
 */
@WebServlet("/FaceDetect")
public class FaceDetect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String fileDir; 
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FaceDetect() {
		super();
		// TODO Auto-generated constructor stub
	}
	public void init( ){
		String root_path = getServletContext().getRealPath("/");
		fileDir = root_path+"data/";
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
		String userid = request.getParameter("userid");
		String crop_info = request.getParameter("crop_info");
		long file_id = Long.parseLong(request.getParameter("file_id"));
		String filePath = fileDir+userid+"/request/";
		String in_fn = filePath+file_id+".jpg";
		String crop_fn = filePath+file_id+"_cr.jpg";
		PrintWriter out = response.getWriter();
		String strMobile = request.getParameter("mobile");
		String strVideo = request.getParameter("video");
		int deviceID = 0;
		if( strMobile != null )
			deviceID = Integer.parseInt(strMobile);
		if (strVideo!=null){
			deviceID=3;
		}
		try {
			if (crop_info.equals("cancel")){
				globalUtil.CopyFile(in_fn, crop_fn);
			}else{
				Image src = ImageIO.read(new File(in_fn));
				String[] cropInfo = crop_info.split("-");
				int x= Integer.parseInt(cropInfo[0]);
				int y= Integer.parseInt(cropInfo[1]);
				int w= Integer.parseInt(cropInfo[2]);
				int h= Integer.parseInt(cropInfo[3]);

				BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				dst.getGraphics().drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);
				
				int nor_w = 840;
	                
                if (w>nor_w){
                	int rw = nor_w;
                	float sc = (float)nor_w/w;
                	int rh = (int) (h*sc);
                	
                	BufferedImage resizeImage = resizeImage(dst,dst.getType(), rw, rh);
                	ImageIO.write(resizeImage, "jpg", new File(crop_fn));
                }
                else
                	ImageIO.write(dst, "jpg", new File(crop_fn));
			}
			if( deviceID == 0 )
				out.print("success");
			if(deviceID==3) out.print("video_success");
			if((0<deviceID)&&(deviceID<3)){
				String strJson = "{success:1}";
				JSONObject obj = new JSONObject(strJson);
				out.print(obj);
			}
		}catch (Exception e){
			LogIn.m_logger.debug("FaceDetect.java: Exception!!!" + e.toString());
			if( deviceID == 0 )
				out.print("error");
			else{
				String strJson = "{error: face crop error}";
				JSONObject obj = new JSONObject(strJson);
				out.print(obj);
			}
			//e.printStackTrace();
		}

	}
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
	    BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
	    Graphics2D g = resizedImage.createGraphics();
	    g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
	    g.dispose();

	    return resizedImage;
	}

}
