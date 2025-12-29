package ucf.firebase;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PushNotifHelper {

	//send notification to single or multiple devices
	public static final String FIREBASE_API_KEY = "AAAAhlxyWdA:APA91bEOOZh4o6urIdcfuj0VgqqyHXqVf3-77s153lMj2kAD9OpQsFTovfmy9F0xDVGMH0lYtWK9XMK1rXd0qhX2gzS45GY2H5NFCnv4suLIq1vXtVn09q0CQtXCfqzqyjZIODEhjnsQ";
	public static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";
	
	public FirebaseResponse sendNotificationToDevice(Notification notification) {
		
		HttpURLConnection httpURLConnection = null;
		if (notification.getDeviceListToSend().size() > 0) {

			try {
				URL url = new URL(FIREBASE_URL);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setUseCaches(false);
				httpURLConnection.setDoInput(true);
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setRequestProperty("Authorization", "key=" + FIREBASE_API_KEY);
				httpURLConnection.setRequestProperty("Content-Type", "application/json");

				// send post request body
				httpURLConnection.setDoInput(true);
				DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
				System.out.println(notification.toJsonString());
				writer.write(notification.toJsonString());
				writer.flush();
				writer.close();
				httpURLConnection.connect();
				httpURLConnection.getResponseCode();
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new FirebaseResponse(httpURLConnection);
	}

}
