package com.zijin.camera_lib.hepler;

import android.content.Context;

/**
 * Description:
 * Date: 10/8/20
 *
 * @author wangke
 */
public class DataPersistenceHelper {
    public static final String name = "picture_data";
    public static final String keyBase64Picture = "base64picture";
    public static final String keyOriginalPicture = "originalPicture";

    public static void saveBase64Picture(Context context, String base64Picture) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .putString(keyBase64Picture, base64Picture)
                .commit();
    }

    public static String getBase64Picture(Context context) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(keyBase64Picture, "");
    }


    public static void saveOriginalPicture(Context context, String base64Picture) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .putString(keyOriginalPicture, base64Picture)
                .commit();
    }

    public static String getOriginalPicture(Context context) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(keyOriginalPicture, "");
    }
}
