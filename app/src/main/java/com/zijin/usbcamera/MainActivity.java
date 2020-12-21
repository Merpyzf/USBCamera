package com.zijin.usbcamera;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zijin.camera_lib.CameraActivity;
import com.zijin.camera_lib.UsbFaceVerifyActivity;
import com.zijin.camera_lib.hepler.DataPersistenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UsbFaceVerifyActivity.start4Login(MainActivity.this, "1280_720", "http://10.2.72.10:8080/");
                //CameraActivity.start(MainActivity.this, "http://10.2.72.10:8080/");
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("base_url", "http://10.2.72.10:8080/");
                startActivityForResult(intent, CameraActivity.REQ_START_CAMERA);
                //Intent intent = new Intent(MainActivity.this, UsbFaceVerifyActivity.class);
                //intent.putExtra("doWhat", UsbFaceVerifyActivity.FOR_USER_INFO);
                //intent.putExtra("size", "1280_720");
                //intent.putExtra("base_url", "http://10.2.72.10:8080/");
                //intent.putExtra("authorization", "lalalal");
                //MainActivity.this.startActivityForResult(intent, UsbFaceVerifyActivity.REQ_START_USB_CAMERA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == UsbFaceVerifyActivity.REQ_START_USB_CAMERA && resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String response = extras.getString("response", "");
                String base64Picture = DataPersistenceHelper.getBase64Picture(this);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject.put("faceBase64", base64Picture);
                    String s = jsonObject.toString();
                    Log.i("wk", s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("wk", "response: " + response);
            } else {
                Log.i("wk", "response is empty");
            }
        } else if (requestCode == CameraActivity.REQ_START_CAMERA && resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String response = extras.getString("response", "");
                Log.i("wk", "response: " + response);
            } else {
                Log.i("wk", "response is empty");
            }
        } else if ((requestCode == CameraActivity.REQ_START_CAMERA || requestCode == UsbFaceVerifyActivity.REQ_START_USB_CAMERA) && resultCode != Activity.RESULT_OK) {
            Log.i("wk", "canceled");
        }
    }
}