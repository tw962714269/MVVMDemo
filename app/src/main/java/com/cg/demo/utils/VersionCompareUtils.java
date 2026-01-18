package com.cg.demo.utils;

import com.blankj.utilcode.util.AppUtils;

/**
 * @author:lee
 * @Date:2025/8/11 16:40
 * @Describe:
 */
public class VersionCompareUtils {
    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     *
     * @param apkVersion 安装包版本号
     * @return 比较结果
     */
    public static boolean compareVersion(String apkVersion) {
        String appVersionName = AppUtils.getAppVersionName();
        if (appVersionName.equals(apkVersion)) {
            return false;
        }

        // 分割版本号
        String[] version1Array = appVersionName.split("\\.");
        String[] version2Array = apkVersion.split("\\.");

        int minLength = Math.min(version1Array.length, version2Array.length);
        int index = 0;

        // 循环比较每个部分
        while (index < minLength) {
            if (Integer.parseInt(version1Array[index]) > Integer.parseInt(version2Array[index])) {
                return false;
            } else if (Integer.parseInt(version1Array[index]) < Integer.parseInt(version2Array[index])) {
                return true;
            }
            index++;
        }

        // 如果前面部分都相同，则版本号长的更大
        return version1Array.length >= version2Array.length;
    }
}