package com.zijin.camera_lib.model.http;


import com.zijin.camera_lib.model.dto.FaceResult;
import com.zijin.camera_lib.model.dto.UserInfo;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Description:
 * Date: 11/26/20
 *
 * @author wangke
 */
public interface FaceService {

    /**
     * 提交人脸验证登录
     *
     * @param params
     * @return
     */
    @POST("open-api/v2/hikvision/faceLogin")
    Call<FaceResult> verifyFace(@Body RequestBody params);

    /**
     * 提交人脸获取当前用户信息
     *
     * @param params
     * @return
     */
    @POST("open-api/v2/hikvision/getUserNo")
    Call<UserInfo> getUserInfo(@Header("Authorization") String authorization, @Body RequestBody params);

}
