package com.zijin.camera_lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.zijin.camera_lib.hepler.DataPersistenceHelper;
import com.zijin.camera_lib.hepler.PictureHelper;

import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity {
    // ui
    private RelativeLayout rlContainer;
    private TextureView previewView;
    private ImageView ivTakePhoto;
    private LinearLayout emptyHolderView;
    private TextView tvEmptyMsg;
    private RelativeLayout picturePreviewContainer;
    private ImageView ivPicturePreview;
    private Button btnRetry;
    private Button btnDone;
    // logic
    private UVCCameraProxy uvcCamera;
    private Context context;
    private SoundPool soundPool;
    private boolean takePictureSound = false;
    private Point previewSize = new Point(1280, 720);
    private ArrayList<String> supportedPreviewSize = new ArrayList<>();
    private String currTakePicturePath;
    public static final int REQ_START_CAMERA = 0x0814;

    private Handler messageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String extraKey = "base64Picture";
            Intent intent = new Intent();
            intent.putExtra(extraKey, "ok, we got data!");
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }
    });

    public static void start(Activity context, String size, boolean takePictureSound) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("size", size);
        intent.putExtra("takePictureSound", takePictureSound);
        context.startActivityForResult(intent, REQ_START_CAMERA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        this.context = this;
        initWidget();
        initData();
        initCamera();
        initEvent();
    }

    private void initData() {
        Intent intent = getIntent();
        // init camera preview size
        String size = intent.getStringExtra("size");
        String[] items = size.split("_");
        previewSize.x = Integer.valueOf(items[0]);
        previewSize.y = Integer.valueOf(items[1]);
        takePictureSound = intent.getBooleanExtra("takePictureSound", false);
    }

    private void initWidget() {
        rlContainer = findViewById(R.id.rlContainer);
        previewView = findViewById(R.id.previewView);
        ivTakePhoto = findViewById(R.id.ivTakePhoto);
        emptyHolderView = findViewById(R.id.emptyHolderView);
        tvEmptyMsg = findViewById(R.id.tvEmptyMsg);
        picturePreviewContainer = findViewById(R.id.llPicturePreviewContainer);
        ivPicturePreview = findViewById(R.id.ivPicturePreview);
        btnRetry = findViewById(R.id.btnRetry);
        btnDone = findViewById(R.id.btnDone);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }

    private void initCamera() {
        uvcCamera = new UVCCameraProxy(context);
        uvcCamera.getConfig()
                .isDebug(true)
                .setPicturePath(PicturePath.APPCACHE);
        uvcCamera.setPreviewTexture(previewView);
        uvcCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {
                uvcCamera.requestPermission(usbDevice);
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                if (granted) {
                    uvcCamera.connectDevice(usbDevice);
                } else {
                    emptyHolderView.setVisibility(View.VISIBLE);
                    tvEmptyMsg.setText("权限授予异常，请插拔USB接口重新授权");
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                uvcCamera.openCamera();
                emptyHolderView.setVisibility(View.GONE);
            }

            @Override
            public void onCameraOpened() {
                // 1920 * 1080
                // 1280 * 720
                // 640 * 480
                // 320 * 240
                resizePreview(previewView, previewSize);
                uvcCamera.setPreviewSize(previewSize.x, previewSize.y);
                uvcCamera.startPreview();
                emptyHolderView.setVisibility(View.GONE);
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                uvcCamera.closeCamera();
                emptyHolderView.setVisibility(View.VISIBLE);
                tvEmptyMsg.setText("未识别到USB摄像头，请检查连接");
            }
        });

    }

    private void resizePreview(TextureView previewView, Point size) {
        Point screenSize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(screenSize);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) previewView.getLayoutParams();
        params.width = screenSize.x;
        params.height = (int) (screenSize.x / (size.x / (size.y * 1.0f)));
        previewView.setLayoutParams(params);
    }

    private void initEvent() {
        ivTakePhoto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ivTakePhoto.setScaleX(0.9f);
                        ivTakePhoto.setScaleY(0.9f);
                        break;
                    case MotionEvent.ACTION_UP:
                        ivTakePhoto.setScaleX(1f);
                        ivTakePhoto.setScaleY(1f);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        ivTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rlContainer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rlContainer.setForeground(new ColorDrawable(ContextCompat.getColor(context, R.color.colorWhite)));
                            rlContainer.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rlContainer.setForeground(null);
                                    uvcCamera.takePicture();
                                    playTakePictureSound();
                                }
                            }, 100);
                        }
                    }, 100);
                } else {
                    uvcCamera.takePicture();
                    playTakePictureSound();
                }
            }
        });

        uvcCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(String path) {
                picturePreviewContainer.setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ivPicturePreview.setImageBitmap(bitmap);
                currTakePicturePath = path;
            }
        });

        this.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPicture(messageHandler, currTakePicturePath);
            }
        });

        this.btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picturePreviewContainer.setVisibility(View.GONE);
            }
        });
    }

    private void playTakePictureSound() {

    }

    private void processPicture(final Handler messageHandler, final String picturePath) {
        if (picturePath == null) {
            Toast.makeText(context, "拍摄图片路径获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                String base64Str = PictureHelper.processPicture(bitmap, PictureHelper.JPEG);
                DataPersistenceHelper.saveBase64Picture(context, base64Str);
                messageHandler.sendEmptyMessage(814);
            }
        }).start();
    }
}