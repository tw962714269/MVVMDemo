package com.cg.demo.network;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CleanUtils;
import com.cg.demo.ui.login.LoginAc;
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

    public static void logout() {
        runOnUiThread(() -> {
            ActivityUtils.finishAllActivities();
            CleanUtils.cleanInternalSp();
            ActivityUtils.startActivity(LoginAc.class);
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
