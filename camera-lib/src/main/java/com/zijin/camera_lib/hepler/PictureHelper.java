package com.zijin.camera_lib.hepler;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Description:
 * Date: 10/8/20
 *
 * @author wangke
 */
public class PictureHelper {
    public static final int JPEG = 1;
    public static final int PNG = 2;

    public static String processPicture(Bitmap bitmap, int encodingType) {
        ByteArrayOutputStream pictureData = new ByteArrayOutputStream();
        Bitmap.CompressFormat compressFormat = encodingType == JPEG ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;
        try {
            if (bitmap.compress(compressFormat, 80, pictureData)) {
                byte[] code = pictureData.toByteArray();
                byte[] outPut = Base64.encode(code, Base64.NO_WRAP);
                return new String(outPut);
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
