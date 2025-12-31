package com.cg.demo.network;


import android.app.Application;
import android.util.Log;

import com.cg.demo.network.interceptor.CancelRepeatRequestInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotation.Converter;
import rxhttp.wrapper.annotation.OkClient;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.converter.FastJsonConverter;
import rxhttp.wrapper.converter.XmlConverter;
import rxhttp.wrapper.cookie.CookieStore;
import rxhttp.wrapper.ssl.HttpsUtils;
import rxhttp.wrapper.ssl.HttpsUtils.SSLParams;

public class RxHttpManager {
    @Converter(name = "XmlConverter")  //非必须
    public static IConverter xmlConverter = XmlConverter.create();
    @Converter(name = "FastJsonConverter")  //非必须
    public static IConverter fastJsonConverter = FastJsonConverter.create();

    @OkClient(name = "SimpleClient", className = "Simple")  //非必须
    public static OkHttpClient simpleClient1 = new OkHttpClient.Builder().build();

    public static void init() {
        File file = new File(getCurApplication().getExternalCacheDir(), "RxHttpCookie");
        SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new CookieStore(file))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new CancelRepeatRequestInterceptor()) // 防重复请求拦截器,取消前一次
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
                .hostnameVerifier((hostname, session) -> true) //忽略host验证
                .build();

        //RxHttp初始化，非必须
        RxHttpPlugins.init(client)                     //自定义OkHttpClient对象
                .setDebug(true)      //调试模式/分段打印/json数据缩进空间
                .setConverter(FastJsonConverter.create())  //设置全局的转换器，非必须
                .setOnParamAssembly(p -> {                 //设置公共参数，非必须
                    //1、可根据不同请求添加不同参数，每次发送请求前都会被回调
                    //2、如果希望部分请求不回调这里，发请求前调用RxHttp#setAssemblyEnabled(false)即可
                    //添加公共请求头
//                    p.addHeader("kp-app-info", "{osName: android, osVersion: " + DeviceUtils.getAndroidID() + ", " +
//                                    "platformVersion: " + DeviceUtils.getSDKVersionName() + ", " +
//                                    "operatingSystemVersion: " + DeviceUtils.getSDKVersionCode() + "," +
//                                    " appName: %E9%B2%B2%E9%B9%8F%E7%A7%91%E6%8A%80," +
//                                    " appVersion: " + AppUtils.getAppVersionName() + "," +
//                                    " appBuildNumber: " + AppUtils.getAppVersionCode() + "," +
//                                    " packageName: " + getCurApplication().getPackageName() + "," +
//                                    "manufacturer: " + DeviceUtils.getManufacturer() + "," +
//                                    "brand: " + DeviceUtils.getModel() + "," +
//                                    "model: x86_64," +
//                                    "deviceId: " + DeviceUtils.getUniqueDeviceId() + "}")
//                            .addHeader("token", SPFullUtils.getInstance().getUserToken());
                    p.addHeader("token", "e8098c59-1e08-48f7-a9ef-6904720a5eb2")
                            .addHeader("kp-app-info", "{osName: android, osVersion: a807f09c068034d8, platformVersion: 10, operatingSystemVersion: 29, appName: %E9%B2%B2%E9%B9%8F%E7%A7%91%E6%8A%80, appVersion: 1.0, appBuildNumber: 1, packageName: com.kp.tmsapp,manufacturer: DS-MDT201,brand: DS-MDT201,model: x86_64,deviceId: 2c3254930ad4938e1ad6db9939f3ac3df}");
                });
    }

    /**
     * 获取当前应用的Application
     * 先使用ActivityThread里获取Application的方法，如果没有获取到，
     * 再使用AppGlobals里面的获取Application的方法
     *
     * @return
     */
    public static Application getCurApplication() {
        Application application = null;
        try {
            Class atClass = Class.forName("android.app.ActivityThread");
            java.lang.reflect.Method currentApplicationMethod = atClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create", "curApp class1:" + application);
        } catch (Exception e) {
            Log.d("fw_create", "e:" + e.toString());
        }

        if (application != null)
            return application;

        try {
            Class atClass = Class.forName("android.app.AppGlobals");
            java.lang.reflect.Method currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication");
            currentApplicationMethod.setAccessible(true);
            application = (Application) currentApplicationMethod.invoke(null);
            Log.d("fw_create", "curApp class2:" + application);
        } catch (Exception e) {
            Log.d("fw_create", "e:" + e.toString());
        }

        return application;
    }

}
