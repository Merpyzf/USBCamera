package com.zijin.camera_lib.hepler;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Size;

/**
 * Description:
 * Date: 12/9/20
 *
 * @author wangke
 */
public class UIHelper {
    private UIHelper() {

    }

    /**
     * 获取当前屏幕的尺寸
     *
     * @param activity
     * @return
     */
    public static Point getScreenSize(Activity activity) {
        Point size = new Point();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        size.x = width;
        size.y = height;
        // 横屏
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            size.x = height;
            size.y = width;
            // 竖屏
        } else {
            size.x = width;
            size.y = height;
        }
        return size;
    }
}

