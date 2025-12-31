package com.cg.demo.utils;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.Method;

public class ApplicationUtils {
    private static Application sApp;

    public synchronized static void setApp(Application application) {
        sApp = application;
    }

    public synchronized static Application getApp() {
        if (sApp == null) {
            sApp = getApplicationWithReflection();
        }
        return sApp;
    }

    @SuppressLint("PrivateApi")
    private static Application getApplicationWithReflection() {
        try {
            Method method = Class.forName("android.app.ActivityThread").getMethod("currentActivityThread");
            method.setAccessible(true);
            Object activityThread = method.invoke(null);
            Object app = activityThread.getClass().getMethod("getApplication").invoke(activityThread);
            return (Application) app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
