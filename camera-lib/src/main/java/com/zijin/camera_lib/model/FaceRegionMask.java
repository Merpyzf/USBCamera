package com.zijin.camera_lib.model;

import android.graphics.Point;
import android.graphics.Region;

import java.sql.Struct;

/**
 * Description: 人脸区域蒙板
 * Date: 12/13/20
 *
 * @author wangke
 */
public class FaceRegionMask {
    // 蒙板资源
    private int faceMaskRes;
    // 蒙板资源图像的宽度
    private final int width;
    // 蒙板资源图像的高度
    private final int height;
    // 人脸所在区域
    private Region faceMaskRegion;

    public FaceRegionMask(int faceMaskRes, int width, int height, Region faceMaskRegion) {
        this.width = width;
        this.height = height;
        this.faceMaskRes = faceMaskRes;
        this.faceMaskRegion = faceMaskRegion;
    }

    public int getFaceMaskRes() {
        return faceMaskRes;
    }

    public void setFaceMaskRes(int faceMaskRes) {
        this.faceMaskRes = faceMaskRes;
    }

    public Region getFaceMaskRegion() {
        return faceMaskRegion;
    }

    public void setFaceMaskRegion(Region faceMaskRegion) {
        this.faceMaskRegion = faceMaskRegion;
    }

    public boolean isMatch(int width, int height) {
        return (width == this.width && height == this.height);
    }
}
