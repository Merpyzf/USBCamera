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
        try {
            doRequest("test");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //uvcCamera.setPreviewCallback(new PreviewCallback() {
        //    @Override
        //    public void onPreviewFrame(final byte[] yuv) {
        //        Bitmap fameBitmap = null;
        //        Bitmap faceBitmap = null;
        //        Bitmap faceBitmap565 = null;
        //        try {
        //            // 该方法被阻塞不会造成帧的堆积
        //            fameBitmap = ImageUtil.yuv2Bitmap(yuv, previewSize.x, previewSize.y);
        //            faceBitmap = Bitmap.createBitmap(fameBitmap, faceRegion.getBounds().left, faceRegion.getBounds().top, faceRegion.getBounds().width(), faceRegion.getBounds().height());
        //            faceBitmap565 = faceBitmap.copy(Bitmap.Config.RGB_565, true);
        //            // 人脸识别图像的宽高必须是偶数
        //            FaceDetector faceDetector = new FaceDetector(faceBitmap565.getWidth(), faceBitmap565.getHeight(), 1);
        //            FaceDetector.Face[] faces = new FaceDetector.Face[1];
        //            int faceNum = faceDetector.findFaces(faceBitmap565, faces);
        //            if (faceNum == 0) {
        //                // 正在检测人脸
        //                messageHandler.sendEmptyMessage(STATUS_FINDING);
        //            } else {
        //                // 检测到人脸，人脸校验中...
        //                messageHandler.sendEmptyMessage(STATUS_VERIFYING);
        //                doRequest(PictureHelper.processPicture(faceBitmap565, PictureHelper.JPEG));
        //            }
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
        //        } finally {
        //            recycleBitmaps(faceBitmap565, fameBitmap, faceBitmap);
        //        }
        //    }
        //});
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
        UserInfo userInfo = new UserInfo();
        userInfo.setRetCode("000000");
        userInfo.setRetMsg("成功");
        userInfo.setPostName("PostName");
        userInfo.setUserName("UserName");
        userInfo.setUserNo("UserNo");
        userInfo.setTid("tid");
        DataPersistenceHelper.saveBase64Picture(this, "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");
        // 人脸校验成功
        Message message = Message.obtain();
        message.obj = userInfo;
        message.what = STATUS_VERIFY_SUCCESS;
        messageHandler.sendMessage(message);


        //RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), getParams(faceBase64));
        //Call<UserInfo> call = faceService.getUserInfo(authorization, requestBody);
        //Response<UserInfo> response = call.execute();
        //UserInfo userInfoResult = response.body();
        //if (userInfoResult == null || !userInfoResult.isVerifySuccess()) {
        //    // 人脸校验失败
        //    messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
        //    Thread.sleep(500);
        //} else {
        //    // 人脸校验成功
        //    Message message = Message.obtain();
        //    message.obj = userInfoResult;
        //    message.what = STATUS_VERIFY_SUCCESS;
        //    messageHandler.sendMessage(message);
        //    uvcCamera.setPreviewCallback(null);
        //}
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