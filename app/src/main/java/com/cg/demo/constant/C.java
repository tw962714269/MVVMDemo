package com.cg.demo.constant;

import android.os.Environment;

import com.blankj.utilcode.util.ActivityUtils;

import java.io.File;

/**
 * FileName: C
 * Created by zlx on 2020/9/18 15:40
 * Email: 1170762202@qq.com
 * Description: 常量
 */
public class C {
    // APK存放目录
    public static final String APK_STORAGE_DIR = ActivityUtils.getTopActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator/* + "apk" + File.separator*/;
    public static final String PROJECT_TABS = "PROJECT_TABS";
    public static final String LANGUAGE = "LANGUAGE";
}
