package ucf.fdrssutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
public class globalUtil {
	public final static byte[] retMark = new byte[] {0x0d, 0x0a};
	private final static float ZERO_SIMILARITY_DIFF = 0.8f;
	private final static float FULL_SIMILARITY_DIFF = 0.6f;

	public static void writeString(DataOutputStream dos, String str)
	{
		byte[] bb = str.getBytes();
		try {
			dos.write(bb);
			dos.write(retMark);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    public static DataOutputStream getOutputStream(String path) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(path);
        DataOutputStream dis = new DataOutputStream(new BufferedOutputStream(outputStream));

        return dis;
    }

    public static DataInputStream getInputStream(String path) throws IOException {
		FileInputStream inputStream = new FileInputStream(path);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));

        return dis;
    }

	public static void CopyFile(String src, String dst)
	{
		 try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			byte[] buffer = new byte[65536 * 2];
    		int read;
    		while ((read = in.read(buffer)) != -1) {
    			out.write(buffer, 0, read);
    		}
    		in.close();
    		out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void WriteData(String fn, byte [] data)
	{
		 try {
			OutputStream out = new FileOutputStream(fn);

			out.write(data, 0, data.length);

    		out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getImagefn(int id)
	{
		String res;
		if (id<10) res = "00"+id+".jpg";
		else if (id<100) res = "0"+id+".jpg";
		else res = id+".jpg";

		return res;
	}
	public static void delFolderContent(String dir_name)
	{
		File dir = new File(dir_name);
		if (dir.isDirectory()==true){
			File [] files = dir.listFiles();
			for (int i=0;i<files.length;i++){
				files[i].delete();
			}
		}
	}
	
	public static String ValidJSON(String str){
		return escapeSpecialChars(NullProc(str));
	}
	
	public static String NullProc(String str){
		String result = "";
		if (str==null){
			result = "";	
		}else if(str.compareToIgnoreCase("null")==0){
			result = "";
		}else{
			result = str;
		}
		return result;
	}
	// while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0)
	// JSON Escape special character
	public static String escapeSpecialChars(String str) {
		return str
			.replace("\\", "\\\\")
			//.replace("/", "\\/")
			.replace("'", "\\'")
			.replace("\"", "\\\"")
			.replace("\b", "\\b")
			.replace("\f", "\\f")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	};
	
	public static String ValidSQL(String str ){
		if (str == null) {
			return "";
		}else{
			String result;
			result = str.replace("\\", "\\\\");  // due to MYSQL syntax
			result = result.replace("'", "\\'"); // due to MYSQL syntax
			return result;
		}
	}
	// Format Date as "YYYY-MM-DD"
	public static String formatDate(Date d){
		int yy=d.getYear()+1900;
		int mm=d.getMonth()+1;
		int dd=d.getDate();
		String ys =yy+"-";
		String ms="";
		if (mm<10){
			ms="0"+mm+"-";
		}else{
			ms=mm+"-";
		}
		String ds="";
		if (dd<10){
			ds="0"+dd;
		}else{
			ds=""+dd;
		}
		return ys+ms+ds;
	}
	public static float getDiffFromDescriptor(String desc1, String desc2){
		if (desc1 ==null || desc2 ==null)
			return 1000000;
		if (desc1.length() == 0 || desc2.length() ==2)
			return 2000000;
		String[] arrDesc1 = desc1.split(" ");
		String[] arrDesc2 = desc2.split(" ");
		if (arrDesc1.length !=512 || arrDesc2.length !=512)
			return 3000000;
		double fDesc1, fDesc2, diff;
		diff =0;
		for (int  i =0; i< 512; i++){
			fDesc1 = Double.parseDouble(arrDesc1[i]);
			fDesc2 = Double.parseDouble(arrDesc2[i]);
			diff = diff + Math.pow(fDesc1 - fDesc2, 2);
		}
		diff = Math.pow(diff, 0.5);
		return (float) diff;
	}
	
	public static float getSimilarityFromDescriptorDiff(float diff){
		if (diff < FULL_SIMILARITY_DIFF)
			return 100;
		if (diff > ZERO_SIMILARITY_DIFF)
			return 0;
		float delta = ZERO_SIMILARITY_DIFF - FULL_SIMILARITY_DIFF;
		float a = -0.1f / delta;
		float b = 1.0f - FULL_SIMILARITY_DIFF * a;
		float rate = a * diff + b;
		return rate * 100;
	}
	
	public static float GetSimilarity(String feature1, String feature2)
	{
		float diff = getDiffFromDescriptor(feature1, feature2);
		return getSimilarityFromDescriptorDiff(diff);
	}
}
