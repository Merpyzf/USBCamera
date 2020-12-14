package com.zijin.camera_lib.hepler;


import com.google.gson.Gson;
import com.zijin.camera_lib.model.http.FaceService;

import java.util.HashMap;

/**
 * Description:
 * Date: 2019-11-27
 *
 * @author wangke
 */
public class ServiceHelper {
    private static FaceService sFaceService;

    public static FaceService getFaceServiceInstance(String baseUrl) {
        if (sFaceService == null) {
            synchronized (Object.class) {
                if (sFaceService == null) {
                    sFaceService = RetrofitHelper.getInstance(baseUrl).create(FaceService.class);
                }
            }
        }
        return sFaceService;
    }

    public static String getParams(String faceBase64) {
        Gson gson = new Gson();
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("faceBase64", faceBase64);
        return gson.toJson(paramsMap);
    }
}
