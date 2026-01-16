package com.cg.demo.utils;

/**
 * @author:lee
 * @Date:2025/8/11 16:40
 * @Describe:
 */
public class VersionCompareUtils {
    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * @param version1 版本号1
     * @param version2 版本号2
     * @return 比较结果
     */
    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        // 分割版本号
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");

        int minLength = Math.min(version1Array.length, version2Array.length);
        int index = 0;

        // 循环比较每个部分
        while (index < minLength) {
            if (Integer.parseInt(version1Array[index]) > Integer.parseInt(version2Array[index])) {
                return 1;
            } else if (Integer.parseInt(version1Array[index]) < Integer.parseInt(version2Array[index])) {
                return -1;
            }
            index++;
        }

        // 如果前面部分都相同，则版本号长的更大
        if (version1Array.length > version2Array.length) {
            return 1;
        } else if (version1Array.length < version2Array.length) {
            return -1;
        } else {
            return 0;
        }
    }
}