package com.zijin.camera_lib;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import java.util.Arrays;

public class Camera2Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView textureView;
    private String cameraId = "";
    private CameraManager cameraManager;
    private Camera2Activity activity;
    private CameraDevice cameraDevice;
    private CaptureRequest captureRequest;
    private CameraCaptureSession session;

    public static void start(Context context) {
        Intent intent = new Intent(context, Camera2Activity.class);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        this.activity = this;
        initWidget();
        initEvent();
    }

    private void initWidget() {
        textureView = findViewById(R.id.textureView);
    }

    private void initEvent() {
        textureView.setSurfaceTextureListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.initCameraInfo();
        }
        this.openCamera();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initCameraInfo() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                // 必须使用前置
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    this.cameraId = cameraId;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(1920, 1080);
        Surface surface = new Surface(surfaceTexture);
        try {
            //创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            final CaptureRequest.Builder  captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置Surface作为预览数据的显示界面
            captureRequestBuilder.addTarget(surface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //创建捕获请求
                        captureRequest = captureRequestBuilder.build();
                        session = session;
                        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                        session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                                super.onCaptureStarted(session, request, timestamp, frameNumber);
                            }

                            @Override
                            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                                super.onCaptureProgressed(session, request, partialResult);
                            }

                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }

                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                super.onCaptureFailed(session, request, failure);
                            }

                            @Override
                            public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
                                super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                            }

                            @Override
                            public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
                                super.onCaptureSequenceAborted(session, sequenceId);
                            }

                            @Override
                            public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
                                super.onCaptureBufferLost(session, request, target, frameNumber);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}