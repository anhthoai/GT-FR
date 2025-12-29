package vn.com.goldtek.facenotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import vn.com.goldtek.facenotify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public Bundle bundle = null;
    public String ServerURL = "";
    public String AdminID = "";
    SharedPreferences SM;
    public Boolean token_registered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        checkPermissions();
    }

    private void checkPermissions(){
        Log.e("FaceRec", "JNI : Check Permission Start");
        String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };
        int n=0;
        for (int i=0;i<permissions.length;i++){
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) this,	permissions[i]);
                n++;
            }
        }

        if (n>0){
            Log.e("FaceRec", "JNI : Check Permission Successed");
            ActivityCompat.requestPermissions((Activity) this, permissions,	10);
        }else{
            Log.e("FaceRec", "JNI : Check Permission Successed");

            String PATH = getFilesDir().getAbsolutePath()+ File.separator + "FaceRec" + File.separator;

            File file = new File(PATH+"server_url.txt");
            if(!file.exists()) {
                new File(PATH).mkdirs();
                UtilFile.CopyDataToSdcard(this, "server_url.txt", PATH + "server_url.txt");
            }

            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

            ServerURL = text.toString().trim();

            if (ServerURL.equals(""))
            {
                Toast.makeText(this, "Please Set Server URL to receive notification", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Server URL is " + ServerURL, Toast.LENGTH_LONG).show();
            }
            //read admin id
            File admin_file = new File(PATH+"admin_config.txt");
            if(!admin_file.exists()) {
                UtilFile.CopyDataToSdcard(this, "admin_config.txt", PATH + "admin_config.txt");
            }

            StringBuilder admin_text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(admin_file));
                String line;

                while ((line = br.readLine()) != null) {
                    admin_text.append(line);
                    admin_text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }

            AdminID = admin_text.toString().trim();

            if (AdminID.equals(""))
            {
                Toast.makeText(this, "Please Set Admin ID to receive notification", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "Admin ID is " + AdminID, Toast.LENGTH_LONG).show();
            }
            //**********************

            SM = getSharedPreferences("token", 0);
            token_registered = SM.getBoolean("token_registered", false);
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getToken failed", task.getException());
                                return;
                            }

                            // Get new FCM token
                            String token = task.getResult();
                            if (!token_registered) {
                                SharedPreferences.Editor editor = SM.edit();
                                editor.putBoolean("token_registered", true);
                                editor.commit();
                                // send token to server for register
                                if (!ServerURL.equals(""))
                                    new MainActivity.SendTokenToRegisterTask().execute(token);
                            }
                        }
                    });

            bundle = getIntent().getExtras();

            Fragment fragment = new notification();

            if (bundle != null) {
                String image_url = bundle.getString("image");
                String name = bundle.getString("name");
                String time = bundle.getString("time");
                String group = bundle.getString("group");
                //new DownloadImageTask().execute(image_url, name, time, group);
                Bundle bundle1 = new Bundle();
                bundle1.putString("name", name);
                bundle1.putString("time", time);
                bundle1.putString("group", group);
                bundle1.putString("image_url", image_url);
                fragment.setArguments(bundle1);
            }
//        else
//        {
//            String image_url = getIntent().getStringExtra("image");
//            String name = getIntent().getStringExtra("name");
//            String time = getIntent().getStringExtra("time");
//            String group = getIntent().getStringExtra("group");
//            //new DownloadImageTask().execute(image_url, name, time, group);
//            if (image_url!=null && name!=null && time!= null && group != null)
//            {
//                Bundle bundle1 = new Bundle();
//                bundle1.putString("name", name);
//                bundle1.putString("time", time);
//                bundle1.putString("group", group);
//                bundle1.putString("image_url", image_url);
//                fragment.setArguments(bundle1);
//            }
//        }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.containerBody, fragment).commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                break;
        }

    }

    public class SendTokenToRegisterTask extends AsyncTask<String, String, String>{
        protected String doInBackground(String... params) {
            String token = params[0];
            try {

                URL url = new URL(ServerURL + "RegisterDevice?token=" + token + "&adminid=" + AdminID);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoOutput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("token", token);

                } catch (JSONException e) {
                    Log.e(TAG, "Error generating JSON for user login");
                }
                bufferedWriter.write(jsonObject.toString());

                bufferedWriter.flush();

                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                String result = "";

                String line = "";

                while ((line = bufferedReader.readLine()) != null) {

                    result += line;
                }

                bufferedReader.close();

                inputStream.close();
                httpURLConnection.disconnect();

            } catch (MalformedURLException e) {

                e.printStackTrace();
                return "fail";

            } catch (IOException e) {

                e.printStackTrace();
                return "fail";
            }

            return "success";
        }

        protected void onPostExecute(String result) {
            if (result.equals("success"))
            {
                Toast.makeText(getApplicationContext(), "Token was registered to server!", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to register token to server!", Toast.LENGTH_LONG).show();
            }
        }
    }

//    public Bitmap getBitmapfromUrl(String imageUrl) {
//        try {
//            URL url = new URL(imageUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            Bitmap bitmap = BitmapFactory.decodeStream(input);
//            return bitmap;
//
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return null;
//
//        }
//    }
}
