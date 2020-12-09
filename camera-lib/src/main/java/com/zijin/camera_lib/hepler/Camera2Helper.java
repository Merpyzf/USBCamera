package com.zijin.camera_lib.hepler;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Description:
 * Date: 12/9/20
 *
 * @author wangke
 */
public class Camera2Helper {
    private Camera2Helper() {

    }

    /**
     * 获取当前设备前置摄像头的Id
     *
     * @param cameraManager
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getFrontCameraId(CameraManager cameraManager) {
        String cameraId = "-1";
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                // 必须使用前置
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraId;
    }
}
