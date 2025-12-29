package vn.com.goldtek.facenotify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import vn.com.goldtek.facenotify.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SettingActivity extends AppCompatActivity {

    public Button SettingBtn;
    public EditText serverURL;
    public EditText adminID;
    SharedPreferences SM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SettingBtn = findViewById(R.id.urlSettingBtn);
        adminID = findViewById(R.id.adminIDEditView);
        serverURL = findViewById(R.id.serverURLEditText);
        SM = getSharedPreferences("token", 0);
        SettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serverURL.getText().toString().isEmpty() && adminID.getText().toString().isEmpty()) {

                    serverURL.setError("Enter Server URL");
                    serverURL.requestFocus();

                    adminID.setError("Enter Admin ID");
                    return;
                } else if (serverURL.getText().toString().isEmpty()) {

                    serverURL.setError("Enter Server URL");
                    serverURL.requestFocus();
                    return;
                } else if (adminID.getText().toString().isEmpty()) {

                    adminID.setError("Enter Admin ID");

                    adminID.requestFocus();
                    return;
                }

                String PATH = getFilesDir().getAbsolutePath()+ File.separator + "FaceRec" + File.separator;

                File file = new File(PATH+"server_url.txt");
                try
                {
                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(serverURL.getText().toString().trim());

                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();

                    Toast.makeText(getApplicationContext(), "Successfully set server url. Please restart app!", Toast.LENGTH_LONG).show();

                }
                catch (IOException e)
                {
                    Log.e("Exception", "File write failed: " + e.toString());
                    return;
                }

                File file1 = new File(PATH+"admin_config.txt");
                try
                {
                    FileOutputStream fOut = new FileOutputStream(file1);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(adminID.getText().toString().trim());

                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();

                    Toast.makeText(getApplicationContext(), "Successfully set adminid. Please restart app!", Toast.LENGTH_LONG).show();
                }
                catch (IOException e)
                {
                    Log.e("Exception", "File write failed: " + e.toString());
                    return;
                }

                SharedPreferences.Editor editor = SM.edit();
                editor.putBoolean("token_registered", false);
                editor.commit();
            }
        });
    }
}
