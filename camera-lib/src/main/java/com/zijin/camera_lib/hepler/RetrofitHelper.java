package com.zijin.camera_lib.hepler;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Description: Retrofit构造单例帮助类
 * Date: 2020-11-27
 *
 * @author wangke
 */
public class RetrofitHelper {

    private static Retrofit sRetrofit;

    private RetrofitHelper() {
    }

    public static Retrofit getInstance(String baseUrl) {
        if (sRetrofit == null) {
            synchronized (Object.class) {
                if (sRetrofit == null) {
                    sRetrofit = createRetrofit(baseUrl, provideDefaultOkHttpClient());
                }
            }
        }
        return sRetrofit;
    }

    public static Retrofit createRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static OkHttpClient provideDefaultOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}
