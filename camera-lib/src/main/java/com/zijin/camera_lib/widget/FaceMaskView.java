package com.zijin.camera_lib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Description: 人脸识别区域蒙版
 * Date: 12/2/20
 *
 * @author wangke
 */
public class FaceMaskView extends View {


    public FaceMaskView(Context context) {
        super(context);
    }

    public FaceMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
