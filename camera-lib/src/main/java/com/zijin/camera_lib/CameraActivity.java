package com.zijin.camera_lib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zijin.camera_lib.hepler.CameraSizeHelper;
import com.zijin.camera_lib.hepler.FaceRegionMaskDataProvider;
import com.zijin.camera_lib.hepler.ImageConvertUtil;
import com.zijin.camera_lib.hepler.PictureHelper;
import com.zijin.camera_lib.hepler.ServiceHelper;
import com.zijin.camera_lib.hepler.ThreadPoolHelper;
import com.zijin.camera_lib.hepler.UIHelper;
import com.zijin.camera_lib.model.FaceRegionMask;
import com.zijin.camera_lib.model.SmartSize;
import com.zijin.camera_lib.model.dto.FaceResult;
import com.zijin.camera_lib.model.http.FaceService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {
    public static final int REQ_START_CAMERA = 0x0914;
    // about camera action
    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_CLOSE_CAMERA = 2;
    private static final int MSG_SET_PREVIEW_SIZE = 3;
    private static final int MSG_SET_PREVIEW_SURFACE = 4;
    private static final int MSG_START_PREVIEW = 5;
    private static final int MSG_STOP_PREVIEW = 6;
    // about face verify status
    private final int STATUS_FINDING = 0x0814;
    private final int STATUS_VERIFYING = 0x0815;
    private final int STATUS_VERIFY_SUCCESS = 0x0816;
    private final int STATUS_VERIFY_FAILED = 0x0817;
    private final int STATUS_ERROR = 0x0818;
    private final int STATUS_NET_ERROR = 0x0819;
    // about permission
    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PREVIEW_FORMAT = ImageFormat.NV21;
    // about handler
    private HandlerThread cameraThread = null;
    private Handler cameraHandler = null;
    private Camera camera;
    private int cameraId = -1;
    private Camera.CameraInfo cameraInfo;
    // about widget
    private TextView tvMessage;
    private ImageView ivFaceMask;
    private SurfaceView previewSurfaceView;
    private SurfaceHolder previewSurfaceHolder;
    private FrameLayout container;
    // about size
    private SmartSize screenSize;
    private SmartSize previewSize;
    private boolean isStartPreview;
    private FaceRegionMask faceRegionMask;
    private boolean findFace;
    private FaceService faceService;
    private final ReentrantLock lock = new ReentrantLock();
    private final MessageHandler messageHandler = new MessageHandler();
    private long frameCount = 0;
    private boolean grantCameraPermission;

    public static void start(Activity activity, String baseUrl) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("base_url", baseUrl);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        screenSize = UIHelper.getScreenSmartSize(this);
        initFaceService();
        startCameraThread();
        initCameraInfo();
        initWidget();
    }

    private void initFaceService() {
        String baseUrl = getIntent().getStringExtra("base_url");
        faceService = ServiceHelper.getFaceServiceInstance(baseUrl);
    }

    private void initWidget() {
        tvMessage = findViewById(R.id.tvMessage);
        ivFaceMask = findViewById(R.id.ivFaceMask);
        previewSurfaceView = findViewById(R.id.previewSurfaceView);
        container = findViewById(R.id.container);
        previewSurfaceView.getHolder().addCallback(new PreviewSurfaceCallback());
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 动态权限检查
        if (!isRequiredPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        } else if (cameraHandler != null) {
            grantCameraPermission = true;
        }
    }

    /**
     * 判断我们需要的权限是否被授予，只要有一个没有授权，我们都会返回 false
     *
     * @return true 权限都被授权
     */
    private boolean isRequiredPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        cameraHandler = new CameraHandler(cameraThread.getLooper());
    }

    private void stopCameraThread() {
        if (cameraThread != null) {
            cameraThread.quitSafely();
        }
        cameraThread = null;
        cameraHandler = null;
    }

    /**
     * 初始化摄像头信息
     */
    private void initCameraInfo() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                this.cameraId = cameraId;
                this.cameraInfo = cameraInfo;
            }
        }
    }

    /**
     * 开启指定摄像头
     */
    @WorkerThread
    private void openCamera(int cameraId) {
        if (camera != null) {
            camera.release();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            camera = Camera.open(cameraId);
            assert camera != null;
            camera.setDisplayOrientation(CameraSizeHelper.getCameraDisplayOrientation(this, cameraInfo));
        }
    }

    /**
     * 关闭相机
     */
    @WorkerThread
    private void closeCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
            cameraId = -1;
            cameraInfo = null;
        }
    }

    /**
     * 根据指定的尺寸要求设置预览尺寸，我们会同时考虑指定尺寸的比例和大小
     */
    @WorkerThread
    private void initPreviewSize() {
        // 初始化相机预览画面的输出尺寸和格式
        previewSize = CameraSizeHelper.getPreviewSmartSize1(screenSize, camera.getParameters().getSupportedPreviewSizes());
        if (previewSize == null) {
            Message message = Message.obtain();
            message.what = STATUS_ERROR;
            message.obj = "相机预览画面尺寸初始化失败";
            return;
        }
        faceRegionMask = FaceRegionMaskDataProvider.getMatchFaceRegionMask(this, previewSize);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(previewSize.getLonger(), previewSize.getShorter());
        if (isPreviewFormatSupported(parameters, PREVIEW_FORMAT) && previewSize != null) {
            parameters.setPreviewFormat(PREVIEW_FORMAT);
            int frameWidth = previewSize.getLonger();
            int frameHeight = previewSize.getShorter();
            int previewFormat = parameters.getPreviewFormat();
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(previewFormat, pixelFormat);
            int bufferSize = (frameWidth * frameHeight * pixelFormat.bitsPerPixel) / 8;
            camera.addCallbackBuffer(new byte[bufferSize]);
            camera.addCallbackBuffer(new byte[bufferSize]);
            camera.addCallbackBuffer(new byte[bufferSize]);
        }
        camera.setParameters(parameters);
        // 预览视图的大小和相机输出的画面保持一致，避免画面出现拉伸
        previewSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                if (previewSize != null) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
                    layoutParams.width = previewSize.getLonger();
                    layoutParams.height = previewSize.getShorter();
                    container.setLayoutParams(layoutParams);
                    if (faceRegionMask != null) {
                        ivFaceMask.setImageResource(faceRegionMask.getFaceMaskRes());
                    }
                }
            }
        });
    }

    /**
     * 判断指定的预览格式是否支持
     */
    private boolean isPreviewFormatSupported(Camera.Parameters parameters, int format) {
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        return supportedPreviewFormats != null && supportedPreviewFormats.contains(format);
    }

    /**
     * 设置预览 Surface
     */
    @WorkerThread
    private void setPreviewSurfaceHolder(@Nullable SurfaceHolder previewSurfaceHolder) {
        if (camera != null && previewSurfaceHolder != null) {
            try {
                camera.setPreviewDisplay(previewSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始预览
     */
    @WorkerThread
    private void sendStartPreviewMsg() {
        if (!isStartPreview) {
            SurfaceHolder previewSurface = this.previewSurfaceHolder;
            if (camera != null && previewSurface != null) {
                camera.setPreviewCallbackWithBuffer(new PreviewCallback());
                camera.startPreview();
            }
        }
    }

    /**
     * 停止预览
     */
    @WorkerThread
    private void stopPreview() {
        if (camera != null) {
            camera.stopFaceDetection();
            camera.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraThread();
    }

    private class PreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            frameCount++;
            if (frameCount % 6 == 0) {
                // 执行人脸验证
                ThreadPoolHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 如果当前正在校验则忽略对后面到来的帧的人脸检测
                        lock.lock();
                        if (findFace) {
                            lock.unlock();
                            return;
                        }
                        final Bitmap faceBitmap565 = getFaceBitmap(data);
                        // 执行人脸检测（人脸识别图像的宽高必须是偶数）
                        FaceDetector faceDetector = new FaceDetector(faceBitmap565.getWidth(), faceBitmap565.getHeight(), 1);
                        FaceDetector.Face[] faces = new FaceDetector.Face[1];
                        int faceNum = faceDetector.findFaces(faceBitmap565, faces);
                        if (faceNum == 0) {
                            // 正在检测人脸
                            messageHandler.sendEmptyMessage(STATUS_FINDING);
                            findFace = false;
                        } else {
                            findFace = true;
                            messageHandler.sendEmptyMessage(STATUS_VERIFYING);
                            // 检测到人脸，人脸校验中
                            String faceBase64 = PictureHelper.processPicture(faceBitmap565, PictureHelper.JPEG);
                            faceBitmap565.recycle();
                            try {
                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), ServiceHelper.getParams(faceBase64));
                                Call<FaceResult> call = faceService.verifyFace(requestBody);
                                Response<FaceResult> response = null;
                                response = call.execute();
                                FaceResult faceResult = response.body();
                                if (faceResult == null || !faceResult.isVerifySuccess()) {
                                    // 人脸校验失败
                                    messageHandler.sendEmptyMessage(STATUS_VERIFY_FAILED);
                                    Thread.sleep(500);
                                    findFace = false;
                                } else {
                                    // 人脸校验成功
                                    Message message = Message.obtain();
                                    message.obj = faceResult;
                                    message.what = STATUS_VERIFY_SUCCESS;
                                    messageHandler.sendMessage(message);
                                }
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                                messageHandler.sendEmptyMessage(STATUS_NET_ERROR);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException interruptedException) {
                                    interruptedException.printStackTrace();
                                }
                                findFace = false;
                            }
                        }
                    }
                });
            }
            camera.addCallbackBuffer(data);
        }
    }

    /**
     * 裁切人脸区域的画面
     *
     * @param data
     * @return
     */
    private Bitmap getFaceBitmap(byte[] data) {
        // 镜像翻转图像
        Matrix matrix = new Matrix();
        // todo 应根据获取帧的图像实时获取需要旋转的角度
        matrix.postRotate(180);
        matrix.postScale(-1, 1);
        Bitmap originalBitmap = ImageConvertUtil.nv21ToBitmap(data, previewSize.getLonger(), previewSize.getShorter());
        final Bitmap previewBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, previewSize.getLonger(), previewSize.getShorter(), matrix, true);
        Bitmap faceBitmap;
        if (faceRegionMask != null) {
            Rect faceRegionBounds = faceRegionMask.getFaceMaskRegion().getBounds();
            faceBitmap = Bitmap.createBitmap(previewBitmap, faceRegionBounds.left, faceRegionBounds.top, faceRegionBounds.width(), faceRegionBounds.height());
        } else {
            faceBitmap = Bitmap.createBitmap(previewBitmap, 0, 0, previewBitmap.getWidth(), previewBitmap.getHeight());
        }
        Bitmap faceBitmap565 = faceBitmap.copy(Bitmap.Config.RGB_565, true);
        originalBitmap.recycle();
        previewBitmap.recycle();
        faceBitmap.recycle();
        return faceBitmap565;
    }

    private class PreviewSurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            previewSurfaceHolder = holder;
            if (grantCameraPermission) {
                sendStartPreviewMsg(holder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            previewSurfaceHolder = null;
        }
    }

    private void sendStartPreviewMsg(SurfaceHolder holder) {
        if (camera == null) {
            cameraHandler.obtainMessage(MSG_OPEN_CAMERA, cameraId, 0).sendToTarget();
        }
        cameraHandler.sendEmptyMessage(MSG_SET_PREVIEW_SIZE);
        cameraHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, holder).sendToTarget();
        cameraHandler.sendEmptyMessage(MSG_START_PREVIEW);
    }

    private class CameraHandler extends Handler {
        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_CAMERA: {
                    openCamera(msg.arg1);
                    break;
                }
                case MSG_CLOSE_CAMERA: {
                    closeCamera();
                    break;
                }
                case MSG_SET_PREVIEW_SIZE: {
                    initPreviewSize();
                    break;
                }
                case MSG_SET_PREVIEW_SURFACE: {
                    SurfaceHolder previewSurface = (SurfaceHolder) msg.obj;
                    setPreviewSurfaceHolder(previewSurface);
                    break;
                }
                case MSG_START_PREVIEW: {
                    sendStartPreviewMsg();
                    isStartPreview = true;
                    break;
                }
                case MSG_STOP_PREVIEW: {
                    stopPreview();
                    isStartPreview = false;
                    break;
                }
                default:
                    throw new IllegalArgumentException("Illegal message: " + msg.what);
            }
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == STATUS_FINDING) {
                tvMessage.setTextColor(Color.WHITE);
                tvMessage.setText(R.string.text_face_finding);
            } else if (msg.what == STATUS_VERIFYING) {
                tvMessage.setTextColor(Color.WHITE);
                tvMessage.setText(R.string.text_face_verifying);
            } else if (msg.what == STATUS_VERIFY_FAILED) {
                tvMessage.setTextColor(Color.RED);
                tvMessage.setText(R.string.text_face_verify_failed);
            } else if (msg.what == STATUS_VERIFY_SUCCESS) {
                tvMessage.setTextColor(Color.GREEN);
                tvMessage.setText(R.string.text_verify_success);
                // 人脸校验成功
                String response = new Gson().toJson(msg.obj);
                Intent intent = new Intent();
                intent.putExtra("response", response);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (msg.what == STATUS_NET_ERROR) {
                tvMessage.setTextColor(Color.RED);
                tvMessage.setText(R.string.text_net_error);
            } else if (msg.what == STATUS_ERROR) {
                String message = (String) msg.obj;
                Toast.makeText(CameraActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            if (camera == null) {
                cameraHandler.obtainMessage(MSG_OPEN_CAMERA, cameraId, 0).sendToTarget();
            }
            sendStartPreviewMsg(previewSurfaceHolder);
            grantCameraPermission = true;
        } else {
            grantCameraPermission = false;
            Toast.makeText(CameraActivity.this, "权限被拒绝，在使用摄像头前必须先授予权限", Toast.LENGTH_LONG).show();
        }
    }
}
