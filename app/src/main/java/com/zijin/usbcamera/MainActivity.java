package com.zijin.usbcamera;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zijin.camera_lib.CameraActivity;
import com.zijin.camera_lib.hepler.DataPersistenceHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.start(MainActivity.this, "1280_720", false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String base64Prefix = "data:image/png;base64,";
        if (requestCode == CameraActivity.REQ_START_CAMERA && resultCode == RESULT_OK) {
            String base64Content = DataPersistenceHelper.getBase64Picture(MainActivity.this);
            String resultData = base64Prefix + base64Content;
            Log.i("wk", "base64: " + resultData);
        }
    }
}