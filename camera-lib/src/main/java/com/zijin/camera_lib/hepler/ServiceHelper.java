package com.zijin.camera_lib.hepler;


import com.zijin.camera_lib.model.http.FaceService;

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
}
