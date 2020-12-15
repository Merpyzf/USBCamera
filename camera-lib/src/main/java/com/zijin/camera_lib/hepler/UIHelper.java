package com.zijin.camera_lib.hepler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.WindowManager;

import com.zijin.camera_lib.model.SmartSize;

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
     * 获取当前屏幕的实际尺寸
     *
     * @param activity
     * @return
     */
    public static SmartSize getScreenSmartSize(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // 可能有虚拟按键的情况
            display.getRealSize(outPoint);
        } else {
            // 不可能有虚拟按键
            display.getSize(outPoint);
        }
        return new SmartSize(outPoint.x, outPoint.y);
    }
}

