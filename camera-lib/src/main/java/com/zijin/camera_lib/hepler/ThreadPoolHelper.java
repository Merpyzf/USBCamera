package com.zijin.camera_lib.hepler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description: 线程池
 * Date: 2020-12-11
 *
 * @author wangke
 */
public class ThreadPoolHelper {

    public static ExecutorService mPoolExecutor;

    private ThreadPoolHelper() {
        mPoolExecutor = Executors.newCachedThreadPool();
    }

    public static ExecutorService getInstance() {
        if (mPoolExecutor == null) {
            synchronized (Object.class) {
                if (mPoolExecutor == null) {
                    new ThreadPoolHelper();
                }
            }
        }
        return mPoolExecutor;
    }
}
