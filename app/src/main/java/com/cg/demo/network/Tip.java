package com.cg.demo.network;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.SPUtils;
import com.cg.demo.utils.ApplicationUtils;
import com.cg.demo.utils.SPFullUtils;


/**
 * 可在任意线程执行本类方法
 */
public class Tip {

    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private static Toast mToast;

    public static void show(int msgResId) {
        show(msgResId, false);
    }

    public static void show(int msgResId, boolean timeLong) {
        show(ApplicationUtils.getApp().getString(msgResId), timeLong);
    }

    public static void show(CharSequence msg) {
        show(msg, false);
    }

    public static void show(final CharSequence msg, final boolean timeLong) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            int duration = timeLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
            mToast = Toast.makeText(ApplicationUtils.getApp(), msg, duration);
            mToast.show();
        });
    }

    public static void finshOnLogin() {
        runOnUiThread(() -> {
            mToast = Toast.makeText(ApplicationUtils.getApp(), "登录过期。请重新登录", Toast.LENGTH_LONG );
            mToast.show();
            ActivityUtils.finishAllActivities();
            CleanUtils.cleanInternalSp();
            ActivityUtils.startActivity("com.kp.tmsapp","com.cg.wmsapp.ui.login.LoginAc");
            SPUtils.getInstance().clear();
            SPFullUtils.getInstance().clear();
        });
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }
}
