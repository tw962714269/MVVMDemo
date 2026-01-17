package com.cg.demo.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.manager.NetworkMonitorManager;
import com.cg.demo.network.RxHttpManager;
import com.cg.demo.utils.NeverCrashUtils;

/**
 * @author:lee
 * @Date:2025/8/8 9:33
 * @Describe:
 */
public class BaseApp extends Application {
    private static BaseApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        setApplication(this);
        // 全局异常捕获
        crashException();

        //初始化RxHttp网络请求架构
        RxHttpManager.init();

        // 初始化全局网络监听框架
        NetworkMonitorManager.getInstance().init(this);
    }

    /**
     * 全局异常捕获
     */
    private void crashException() {
        NeverCrashUtils.getInstance()
                .setDebugMode(true)
                .setMainCrashHandler((t, e) -> {
                    LogUtils.d("主线程异常");//此处log只是展示，当debug为true时，主类内部log会打印异常信息
                    //做日志记录
                })
                .setUncaughtCrashHandler((t, e) -> {
                    LogUtils.d("子线程异常");//此处log只是展示，当debug为true时，主类内部log会打印异常信息
                    //做日志记录
                })
                .register(this);
    }

    /**
     * 当宿主没有继承自该Application时,可以使用set方法进行初始化baseApplication
     */
    private void setApplication(@NonNull BaseApp application) {
        instance = application;
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(
                    @NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(
                    @NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                ActivityUtils.finishActivity(activity);
            }
        });
    }

    /**
     * 获得当前app运行的Application
     */
    public static BaseApp getInstance() {
        if (instance == null) {
            throw new NullPointerException(
                    "please inherit BaseApplication or call setApplication.");
        }
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // 释放网络监听资源
        NetworkMonitorManager.getInstance().release();
    }
}
