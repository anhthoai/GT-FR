package ucf.fdrserver;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

import ucf.fdrssutil.globalUtil;

/**
 * Servlet implementation class PhotoUpload
 * General Photo Upload
 */
@WebServlet("/PhotoUpload")
public class PhotoUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_rootDir, m_fileDir, m_tempDir, m_exePath;
	private int m_maxFileSize = 10*1024 * 1024;
	private int m_maxMemSize = 1*1024 * 1024;
	private File m_file;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PhotoUpload() {
        super();
       
    }
    public void init( ){
    	m_rootDir = getServletContext().getRealPath("/");
    	m_fileDir = m_rootDir+"data/";
        m_tempDir = m_rootDir+"temp/";
        //exe_path = root_path+"externalexec/FaceUtility.exe"; // Windows
    	m_exePath = m_rootDir+"externalexec/FaceUtility"; //IF Linux 
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
		String strMobile = request.getParameter("mobile");
		String strRecord = request.getParameter("record");
		
		int deviceID = 0;
		int recordFlag = 0;
		if( strMobile != null )
			deviceID = Integer.parseInt(strMobile);
		if( strRecord != null )
			recordFlag = Integer.parseInt(strRecord);

		String tempDir = m_tempDir+userid+"/request/";
		String filePath = m_fileDir+userid+"/request/";
		if (recordFlag ==1){
			tempDir = tempDir+userid+"/record/";
			filePath = m_fileDir+userid+"/record/";
		}

		double rand = Math.random();
		long file_id = (long) (rand*999999999);
		String outfn = file_id+".jpg";
		String resizefn = file_id+"_re.jpg";
		float sc=1.0f;
		int w = 0,h = 0;
		File f = new File(tempDir);
		if (f.exists()==false) f.mkdirs();
		else{
			//globalUtil.delFolderContent(tempPath);
		}
		f = new File(filePath);
		if (f.exists()==false) f.mkdirs();
		else{
			globalUtil.delFolderContent(filePath);
		}
		java.io.PrintWriter out = response.getWriter( );
		DiskFileItemFactory factory = new DiskFileItemFactory();
	    // maximum size that will be stored in memory
		factory.setSizeThreshold(m_maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(tempDir));
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
		         if ( !fi.isFormField () )
		         {
		            // Write the file
                    m_file = new File( filePath + outfn);
		            
		            fi.write( m_file );
		            n++;
		            
		            File upload_file = new File(filePath + outfn);
		            try{         	
		            	ImageInformation info = readImageInformation(upload_file);
		            	if (info.orientation!=1){
		            		BufferedImage inImage = ImageIO.read(upload_file);//change path to where file is located		                
		                	AffineTransform trans = getExifTransformation(info);
		                	boolean change_ori = true;
		                	if (info.orientation<5) change_ori=false;
		                	BufferedImage transformImage = transformImage(inImage, trans, change_ori);
		                	ImageIO.write(transformImage, "jpg", upload_file);         	                	
		                }
		            }
		            catch(Exception e){
		            	//change path to where file is located
		            }
		            BufferedImage originalImage = ImageIO.read(upload_file);
		            
	                w = originalImage.getWidth();
	                h = originalImage.getHeight();
	                int nor_w = 550;
	                
	                if (w>nor_w && recordFlag !=1){
	                	int rw = nor_w;
	                	sc = (float)nor_w/w;
	                	int rh = (int) (h*sc);
	                	
	                	BufferedImage resizeImage = resizeImage(originalImage,originalImage.getType(), rw, rh);
	                	
	                	ImageIO.write(resizeImage, "jpg", new File(filePath + resizefn));
	                	
	                }
	                else{
	                	if (recordFlag !=1)
	                		globalUtil.CopyFile(filePath+outfn, filePath+resizefn);
	                }

		         }
		      }
		      if (n>0){
		    	  String strJson = "{file_id:"+file_id+",scale:"+sc+",width:"+w+",height:"+h+"}";
				  JSONObject obj = new JSONObject(strJson);
				  out.print(obj);
		    	  //out.print(file_id+":"+String.valueOf(sc)+":"+w+":"+h);
		      }
		      else{
		    	  if( deviceID == 0 )
		    		  out.print("no file");
		    	  else{
		    		  String strJson = "{error:no file}";
					  JSONObject obj = new JSONObject(strJson);
					  out.print(obj);
		    	  }
		      }
	   }catch(Exception ex) {
		   if( deviceID == 0 )
			   out.print("error");
		   else{
			   String strJson = "{error:failed upload photo}";
			   JSONObject obj = new JSONObject(strJson);
			   out.print(obj);
		   }
	   }
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
	
	// Look at http://chunter.tistory.com/143 for information
	public static AffineTransform getExifTransformation(ImageInformation info) {

	    AffineTransform t = new AffineTransform();

	    switch (info.orientation) {
	    case 1:
	        break;
	    case 2: // Flip X
	        t.scale(-1.0, 1.0);
	        t.translate(-info.width, 0);
	        break;
	    case 3: // PI rotation 
	        t.translate(info.width, info.height);
	        t.rotate(Math.PI);
	        break;
	    case 4: // Flip Y
	        t.scale(1.0, -1.0);
	        t.translate(0, -info.height);
	        break;
	    case 5: // - PI/2 and Flip X
	        t.rotate(-Math.PI / 2);
	        t.scale(-1.0, 1.0);
	        break;
	    case 6: // -PI/2 and -width
	        t.translate(info.height, 0);
	        t.rotate(Math.PI / 2);
	        break;
	    case 7: // PI/2 and Flip
	        t.scale(-1.0, 1.0);
	        t.translate(-info.height, 0);
	        t.translate(0, info.width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    case 8: // PI / 2
	        t.translate(0, info.width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    }

	    return t;
	}
	
	public static BufferedImage transformImage(BufferedImage image, AffineTransform transform, boolean change_ori) throws Exception {

	    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

	    //BufferedImage destinationImage = op.createCompatibleDestImage(image, (image.getType() == BufferedImage.TYPE_BYTE_GRAY) ? image.getColorModel() : null );
	    BufferedImage destinationImage=null;
	    if (change_ori==true)
	    	destinationImage = new BufferedImage( image.getHeight(), image.getWidth(), image.getType() );
	    else
	    	destinationImage = new BufferedImage( image.getWidth(), image.getHeight(), image.getType() );
	    
	    destinationImage = op.filter(image, destinationImage);
	    return destinationImage;
	}

}
