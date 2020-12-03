package com.zijin.camera_lib.model.http;


import com.zijin.camera_lib.model.dto.FaceResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Description:
 * Date: 11/26/20
 *
 * @author wangke
 */
public interface FaceService {

    @POST("open-api/v2/hikvision/faceLogin")
    Call<FaceResult> verifyFace(@Body RequestBody params);
}
