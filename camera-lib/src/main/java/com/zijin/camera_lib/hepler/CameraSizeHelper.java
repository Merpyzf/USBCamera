package com.zijin.camera_lib.hepler;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.Display;

import com.zijin.camera_lib.model.SmartSize;

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
    // 图片和视频的标准高清尺寸
    private static SmartSize SIZE_1080P = new SmartSize(1920, 1080);
    private static float BEST_RATIO = (float) (16 / 9.0);

    private CameraSizeHelper() {
    }

    /**
     * 获取当前显示设备的屏幕尺寸
     *
     * @param display
     * @return
     */
    public static SmartSize getDisplaySmartSize(Display display) {
        Point outSize = new Point();
        display.getRealSize(outSize);
        return new SmartSize(outSize.x, outSize.y);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static SmartSize getPreviewSmartSize(Display display, CameraCharacteristics characteristics) {
        SmartSize screenSize = getDisplaySmartSize(display);
        boolean hdScreen = screenSize.getLonger() >= SIZE_1080P.getLonger() || screenSize.getShorter() >= SIZE_1080P.getShorter();
        // 确定当前预览输出画面的最大尺寸，如果设备的分辨率高于1920*1080则最大按1920*1080的尺寸，否则按照屏幕分辨率大小
        SmartSize maxSize = hdScreen ? SIZE_1080P : screenSize;
        StreamConfigurationMap config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] allSizes = config.getOutputSizes(SurfaceTexture.class);
        List<SmartSize> smartSizes = sizesToSmartSizeArray(allSizes);
        Collections.sort(smartSizes, new Comparator<SmartSize>() {
            @Override
            public int compare(SmartSize o1, SmartSize o2) {
                int o1Count = o1.getSize().getWidth() * o1.getSize().getHeight();
                int o2Count = o2.getSize().getWidth() * o2.getSize().getHeight();
                return Integer.compare(o2Count, o1Count);
            }
        });
        // 选择一个合适的预览尺寸
        // 输出预览尺寸的大小不超过预览显示View的大小，并且长宽比要满足 16：9
        for (SmartSize smartSize : smartSizes) {
            if (smartSize.getLonger() <= screenSize.getLonger() && smartSize.getShorter() <= screenSize.getShorter()
                    && smartSize.getAspectRatio() > 1.7 && smartSize.getAspectRatio() < 1.8
            ) {
                return smartSize;
            }

        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static List<SmartSize> sizesToSmartSizeArray(Size[] allSizes) {
        List<SmartSize> smartSizes = new ArrayList<>();
        for (Size size : allSizes) {
            smartSizes.add(new SmartSize(size.getWidth(), size.getHeight()));
        }
        return smartSizes;
    }
}
