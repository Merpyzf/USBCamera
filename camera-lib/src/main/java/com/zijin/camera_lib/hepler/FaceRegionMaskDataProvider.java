package com.zijin.camera_lib.hepler;

import android.app.Activity;
import android.graphics.Region;

import com.zijin.camera_lib.R;
import com.zijin.camera_lib.model.FaceRegionMask;
import com.zijin.camera_lib.model.SmartSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 根据不同设备的分辨提供与之匹配的人脸识别区域蒙板
 * Date: 12/13/20
 *
 * @author wangke
 */
public class FaceRegionMaskDataProvider {
    private static final List<FaceRegionMask> faceRegionMasks = new ArrayList<>();

    static {
        faceRegionMasks.add(new FaceRegionMask(R.drawable.face_region_mask_1920_1080, 1920, 1080, new Region(624, 166, 1300, 828)));
        faceRegionMasks.add(new FaceRegionMask(R.drawable.face_region_mask_1280_768, 1280, 768, new Region(460, 132, 888, 602)));
    }

    private FaceRegionMaskDataProvider() {

    }

    /**
     * 根据当前设备的分辨率获取与之匹配的蒙板信息
     *
     * @param activity
     * @return
     */
    public static FaceRegionMask getMatchFaceRegionMask(Activity activity, SmartSize previewSize) {
        if (previewSize != null) {
            for (FaceRegionMask faceRegionMask : faceRegionMasks) {
                if (faceRegionMask.isMatch(previewSize.getLonger(), previewSize.getShorter())) {
                    return faceRegionMask;
                }
            }
        }
        return null;
    }
}
