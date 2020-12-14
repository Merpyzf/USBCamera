package com.zijin.camera_lib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Description:
 * Date: 12/9/20
 *
 * @author wangke
 */
public class AutoFitSurfaceView extends SurfaceView {
    private float aspectRatio = 0f;

    public AutoFitSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            Log.e("wk", "尺寸不能为负值");
            return;
        }
        aspectRatio = width * 1.0f / height;
        getHolder().setFixedSize(width, height);
        this.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
