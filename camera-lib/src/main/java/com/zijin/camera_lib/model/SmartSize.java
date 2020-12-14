package com.zijin.camera_lib.model;


/**
 * Description:
 * Date: 12/9/20
 *
 * @author wangke
 */
public class SmartSize {
    private Size size;
    private int longer;
    private int shorter;

    public SmartSize(int width, int height) {
        this.size = new Size(width, height);
        this.longer = Math.max(width, height);
        this.shorter = Math.min(width, height);
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public int getLonger() {
        return longer;
    }

    public void setLonger(int longer) {
        this.longer = longer;
    }

    public int getShorter() {
        return shorter;
    }

    public void setShorter(int shorter) {
        this.shorter = shorter;
    }

    public float getAspectRatio() {
        return this.longer / (this.shorter * 1.0f);
    }

    @Override
    public String toString() {
        return "SmartSize{" +
                "longer=" + longer +
                ", shorter=" + shorter +
                '}';
    }
}
