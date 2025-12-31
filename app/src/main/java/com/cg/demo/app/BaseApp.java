package com.cg.demo.app;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.bean.MessageEvent;
import com.cg.demo.utils.NeverCrashUtils;
import com.cg.demo.network.RxHttpManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author:lee
 * @Date:2025/8/8 9:33
 * @Describe:
 */
public class BaseApp extends Application {
    private static BaseApp instance;
    private String mBroadcastAction = "android.intent.ACTION_SCAN_OUTPUT";
    private String mBroadcastLabel = "barcode";

    @Override
    public void onCreate() {
        super.onCreate();
        setApplication(this);
        crashException();
        //当 App 中出现多进程, 并且您需要适配所有的进程, 就需要在 App 初始化时调用 initCompatMultiProcess()
        //在 Demo 中跳转的三方库中的 DefaultErrorActivity 就是在另外一个进程中, 所以要想适配这个 Activity 就需要调用 initCompatMultiProcess()
//        AutoSize.initCompatMultiProcess(this);
        EventBus.getDefault().register(this);
        //如果在某些特殊情况下出现 InitProvider 未能正常实例化, 导致 AndroidAutoSize 未能完成初始化
        //可以主动调用 AutoSize.checkAndInit(this) 方法, 完成 AndroidAutoSize 的初始化后即可正常使用
//        AutoSize.checkAndInit(this);
        RxHttpManager.init();

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(mBroadcastAction);
//        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(MessageEvent event) {
        if (event.getSender() == BaseApp.instance) return;
        LogUtils.v("收到消息：" + event.getMsg().toString());
        EventBus.getDefault().removeStickyEvent(event);
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

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mBroadcastAction.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String barcode = bundle.getString(mBroadcastLabel);
                    LogUtils.v("onReceive: barcode =" + barcode);
                    EventBus.getDefault().postSticky(new MessageEvent(barcode, BaseApp.instance));
                }
            }
        }
    };
}
