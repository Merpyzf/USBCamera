package com.zijin.camera_lib.hepler;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.zijin.camera_lib.model.SmartSize;

import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Description:
 * Date: 12/9/20
 *
 * @author wangke
 */
public class CameraSizeHelper {
    // 定义常用的屏幕分辨率基准
    private static List<SmartSize> BASE_SCREEN_SIZES = new ArrayList<SmartSize>();

    static {
        // 1.7
        BASE_SCREEN_SIZES.add(new SmartSize(1920, 1080));
        // 1.6
        BASE_SCREEN_SIZES.add(new SmartSize(1280, 800));
    }

    private CameraSizeHelper() {
    }

    public static SmartSize getPreviewSmartSize1(SmartSize screenSize, List<Camera.Size> supportedPreviewSizes) {
        SmartSize basePreviewSize = BASE_SCREEN_SIZES.get(0);
        boolean hdScreen = screenSize.getLonger() >= basePreviewSize.getLonger() || screenSize.getShorter() >= basePreviewSize.getShorter();
        // 确保预览画面输出的尺寸不高于1920*1080
        SmartSize maxSize = null;
        float aspectRatio;
        if (hdScreen) {
            maxSize = basePreviewSize;
            aspectRatio = maxSize.getLonger() * 1.0f / maxSize.getShorter();
        } else {
            int screenWidth = screenSize.getLonger();
            int screenHeight = screenSize.getShorter();
            aspectRatio = screenWidth * 1.0f / screenHeight;
            // 根据屏幕的长宽比去获取一个与之匹配的预览画面允许的最大基准尺寸
            for (SmartSize size : BASE_SCREEN_SIZES) {
                float baseAspectRatio = size.getLonger() * 1.0f / size.getShorter();
                if (aspectRatio >= baseAspectRatio && aspectRatio <= baseAspectRatio + 0.1) {
                    maxSize = size;
                    break;
                }
            }
        }
        List<SmartSize> smartSizes = sizesToSmartSizeArray(supportedPreviewSizes);
        Collections.sort(smartSizes, new Comparator<SmartSize>() {
            @Override
            public int compare(SmartSize o1, SmartSize o2) {
                int o1Count = o1.getSize().getWidth() * o1.getSize().getHeight();
                int o2Count = o2.getSize().getWidth() * o2.getSize().getHeight();
                return Integer.compare(o2Count, o1Count);
            }
        });
        // 输出预览尺寸的大小不超过预览显示View的大小，并且长宽比要满足选定的基准尺寸
        for (SmartSize smartSize : smartSizes) {
            if (smartSize.getLonger() <= maxSize.getLonger() && smartSize.getShorter() <= maxSize.getShorter()
                    && smartSize.getAspectRatio() >= aspectRatio && smartSize.getAspectRatio() <= aspectRatio + 0.1
            ) {
                return smartSize;
            }
        }
        return null;
    }

    private static List<SmartSize> sizesToSmartSizeArray(List<Camera.Size> supportedPreviewSizes) {
        List<SmartSize> smartSizes = new ArrayList<>();
        for (Camera.Size size : supportedPreviewSizes) {
            smartSizes.add(new SmartSize(size.width, size.height));
        }
        return smartSizes;
    }

    public static int getCameraDisplayOrientation(Activity activity, Camera.CameraInfo cameraInfo) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
