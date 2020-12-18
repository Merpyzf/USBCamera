package com.zijin.camera_lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.usb.UsbDevice;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.utils.ImageUtil;
import com.zijin.camera_lib.hepler.DataPersistenceHelper;
import com.zijin.camera_lib.hepler.PictureHelper;
import com.zijin.camera_lib.hepler.ServiceHelper;
import com.zijin.camera_lib.model.dto.FaceResult;
import com.zijin.camera_lib.model.dto.UserInfo;
import com.zijin.camera_lib.model.http.FaceService;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class UsbFaceVerifyActivity extends AppCompatActivity {
    // ui
    private TextureView previewView;
    private ConstraintLayout emptyContainer;
    private TextView tvEmptyMsg;
    private TextView tvNotify;
    // logic
    private final Point previewSize = new Point(1920, 1080);
    private final Point screenSize = new Point(1920, 1080);
    private final Region faceRegion = new Region();
    public final static int REQ_START_USB_CAMERA = 0x0814;
    // can do things
    public final static int FOR_LOGIN = 0x001;
    public final static int FOR_USER_INFO = 0x002;
    // verify face message
    private final int STATUS_FINDING = 0x0814;
    private final int STATUS_VERIFYING = 0x0815;
    private final int STATUS_VERIFY_SUCCESS = 0x0816;
    private final int STATUS_VERIFY_FAILED = 0x0817;
    private UVCCameraProxy uvcCamera;
    private Context context;
    // token
    private String authorization;
    private int doWhat = FOR_LOGIN;

    private final Handler messageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == STATUS_FINDING) {
                // 人脸检测中
                tvNotify.setText("人脸检测中，请将人脸放入识别区域");
                tvNotify.setTextColor(Color.WHITE);
            } else if (msg.what == STATUS_VERIFYING) {
                // 检测到人脸，人脸识别中
                tvNotify.setText("检测到人脸，识别中");
                tvNotify.setTextColor(Color.WHITE);
            } else if (msg.what == STATUS_VERIFY_SUCCESS) {
                tvNotify.setText("人脸识别成功");
                tvNotify.setTextColor(Color.GREEN);
                // 人脸校验成功
                String response = new Gson().toJson(msg.obj);
                Intent intent = new Intent();
                intent.putExtra("response", response);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (msg.what == STATUS_VERIFY_FAILED) {
                tvNotify.setText("人脸识别失败");
                tvNotify.setTextColor(Color.RED);
            }
            return true;
        }
    });
    private FaceService faceService;

    /**
     * 开启人脸验证进行登录
     *
     * @param context
     * @param size    预览画面尺寸
     * @param baseUrl 项目基础地址
     */
    public static void start4Login(Activity context, String size, String baseUrl) {
        Intent intent = new Intent(context, UsbFaceVerifyActivity.class);
        intent.putExtra("doWhat", FOR_LOGIN);
        intent.putExtra("size", size);
        intent.putExtra("base_url", baseUrl);
        context.startActivityForResult(intent, REQ_START_USB_CAMERA);
    }

    /**
     * 开启人脸验证以获取识别到的用户信息
     *
     * @param context
     * @param authorization 认证token
     * @param size          预览画面尺寸
     * @param baseUrl       项目基础地址
     */
    public static void start4GetUserInfo(Activity context, String authorization, String size, String baseUrl) {
        Intent intent = new Intent(context, UsbFaceVerifyActivity.class);
        intent.putExtra("doWhat", FOR_USER_INFO);
        intent.putExtra("size", size);
        intent.putExtra("base_url", baseUrl);
        intent.putExtra("authorization", authorization);
        context.startActivityForResult(intent, REQ_START_USB_CAMERA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_face_verify);
        this.context = this;
        initScreenSize();
        initWidget();
        initData();
        initFaceRegion();
        initCamera();
        initEvent();
    }

    private void initFaceRegion() {
        //private final Region faceRegion = new Region(662, 160, 1250, 840);
        float withRatio = previewSize.x * 1.0f / screenSize.x;
        float heightRatio = previewSize.y * 1.0f / screenSize.y;
        int left = (int) (662 * withRatio);
        int top = (int) (160 * heightRatio);
        int right = (int) (1250 * withRatio);
        int bottom = (int) (840 * heightRatio);
        faceRegion.set(left, top, right, bottom);
    }

    private void initScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        this.screenSize.x = width;
        this.screenSize.y = height;
    }

    private void initData() {
        Intent intent = getIntent();
        doWhat = intent.getIntExtra("doWhat", 0);
        if (doWhat == FOR_USER_INFO) {
            authorization = intent.getStringExtra("authorization");
        }
        // init camera preview size
        String size = intent.getStringExtra("size");
        String[] items = size.split("_");
        previewSize.x = Integer.parseInt(items[0]);
        previewSize.y = Integer.parseInt(items[1]);
        String baseUrl = intent.getStringExtra("base_url");
        faceService = ServiceHelper.getFaceServiceInstance(baseUrl);
    }

    private void initWidget() {
        previewView = findViewById(R.id.previewView);
        emptyContainer = findViewById(R.id.emptyContainer);
        tvEmptyMsg = findViewById(R.id.tvEmptyMsg);
        tvNotify = findViewById(R.id.tv_notify);

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
                    showEmptyLayout();
                    tvEmptyMsg.setText(getString(R.string.usb_permission_failed));
                }
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                showEmptyLayout();
                tvEmptyMsg.setText("正在启动相机，请稍后");
                uvcCamera.openCamera();
            }

            @Override
            public void onCameraOpened() {
                // support resolution 1920 * 1080  1280 * 720 640 * 480 320 * 240
                resizePreview(previewView, previewSize);
                uvcCamera.setPreviewSize(previewSize.x, previewSize.y);
                uvcCamera.startPreview();
                hideEmptyLayout();
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                showEmptyLayout();
                tvEmptyMsg.setText(getString(R.string.cannot_find_usb));
            }
        });
    }

    private void showEmptyLayout() {
        emptyContainer.setVisibility(View.VISIBLE);
    }

    private void hideEmptyLayout() {
        emptyContainer.setVisibility(View.INVISIBLE);
    }

    /**
     * 根据摄像头所设置成的预览尺寸来调整预览控件的尺寸
     *
     * @param previewView
     * @param size
     */
    private void resizePreview(TextureView previewView, Point size) {
        Point screenSize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(screenSize);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) previewView.getLayoutParams();
        params.width = screenSize.x;
        params.height = (int) (screenSize.x / (size.x / (size.y * 1.0f)));
        previewView.setLayoutParams(params);
    }

    private void initEvent() {
        uvcCamera.setPreviewCallback(new PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] yuv) {
                Bitmap fameBitmap = null;
                Bitmap faceBitmap = null;
                Bitmap faceBitmap565 = null;
                try {
                    // 该方法被阻塞不会造成帧的堆积
                    fameBitmap = ImageUtil.yuv2Bitmap(yuv, previewSize.x, previewSize.y);
                    faceBitmap = Bitmap.createBitmap(fameBitmap, faceRegion.getBounds().left, faceRegion.getBounds().top, faceRegion.getBounds().width(), faceRegion.getBounds().height());
                    faceBitmap565 = faceBitmap.copy(Bitmap.Config.RGB_565, true);
                    // 人脸识别图像的宽高必须是偶数
                    FaceDetector faceDetector = new FaceDetector(faceBitmap565.getWidth(), faceBitmap565.getHeight(), 1);
                    FaceDetector.Face[] faces = new FaceDetector.Face[1];
                    int faceNum = faceDetector.findFaces(faceBitmap565, faces);
                    if (faceNum == 0) {
                        // 正在检测人脸
                        messageHandler.sendEmptyMessage(STATUS_FINDING);
                    } else {
                        // 检测到人脸，人脸校验中...
                        messageHandler.sendEmptyMessage(STATUS_VERIFYING);
                        doRequest(PictureHelper.processPicture(faceBitmap565, PictureHelper.JPEG));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
                } finally {
                    recycleBitmaps(faceBitmap565, fameBitmap, faceBitmap);
                }
            }
        });
    }

    private void doRequest(String faceBase64) throws IOException, InterruptedException {
        if (doWhat == FOR_LOGIN) {
            verifyFace4Login(faceBase64);
        } else {
            verifyFace4UserInfo(faceBase64);
        }
    }

    private void verifyFace4UserInfo(String faceBase64) throws java.io.IOException, InterruptedException {
        if (TextUtils.isEmpty(authorization)) {
            Toast.makeText(context, "校验失败,token不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), getParams(faceBase64));
        Call<UserInfo> call = faceService.getUserInfo(authorization, requestBody);
        Response<UserInfo> response = call.execute();
        UserInfo userInfoResult = response.body();
        if (userInfoResult == null || !userInfoResult.isVerifySuccess()) {
            // 人脸校验失败
            messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
            Thread.sleep(500);
        } else {
            DataPersistenceHelper.saveBase64Picture(this, faceBase64);
            // 人脸校验成功
            Message message = Message.obtain();
            message.obj = userInfoResult;
            message.what = STATUS_VERIFY_SUCCESS;
            messageHandler.sendMessage(message);
            uvcCamera.setPreviewCallback(null);
        }
    }

    private void verifyFace4Login(String faceBase64) throws java.io.IOException, InterruptedException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), getParams(faceBase64));
        Call<FaceResult> call = faceService.verifyFace(requestBody);
        Response<FaceResult> response = call.execute();
        FaceResult faceResult = response.body();
        if (faceResult == null || !faceResult.isVerifySuccess()) {
            // 人脸校验失败
            messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
            Thread.sleep(500);
        } else {
            // 人脸校验成功
            Message message = Message.obtain();
            message.obj = faceResult;
            message.what = STATUS_VERIFY_SUCCESS;
            messageHandler.sendMessage(message);
            uvcCamera.setPreviewCallback(null);
        }
    }

    private String getParams(String faceBase64) {
        Gson gson = new Gson();
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("faceBase64", faceBase64);
        return gson.toJson(paramsMap);
    }

    /**
     * 回收 bitmap 占用的资源
     *
     * @param bitmaps
     */
    private void recycleBitmaps(Bitmap... bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}