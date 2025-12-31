package com.cg.demo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.cg.demo.database.MMkvHelper;

import java.util.Locale;


public class LanguageUtils {

    public static String getSystemLanguage() {
        String language = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        return language;
    }

    public static Locale getCurrentLanguage() {
        Locale locale = MMkvHelper.getInstance().getLanguage();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public static void switchChinese() {
        changeLanguage(Locale.SIMPLIFIED_CHINESE);
    }

    public static void switchEnglish() {
        changeLanguage(Locale.US);
    }

    public static void switchLanguage(Locale locale) {
        changeLanguage(locale);
    }

    private static void changeLanguage(Locale locale) {
        MMkvHelper.getInstance().saveLanguage(locale);
    }

    /**
     * @param context
     * @param locale  想要切换的语言类型 比如 "en" ,"zh"
     */
    @SuppressLint("NewApi")
    public static void setConfiguration(Context context, Locale locale) {
        if (locale == null) {
            return;
        }
        //获取应用程序的所有资源对象
        Resources resources = context.getResources();
        //获取设置对象
        Configuration configuration = resources.getConfiguration();
        //如果API < 17
        //API 25  Android 7.7.1
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }


    public static Context attachBaseContext(Context context) {
        Locale locale = LanguageUtils.getCurrentLanguage();
        return updateResources(context, locale);
    }

    private static Context updateResources(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }
}
