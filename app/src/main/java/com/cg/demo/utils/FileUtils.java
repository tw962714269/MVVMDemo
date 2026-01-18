package com.cg.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 写入APK文件
 */
public class FileUtils {
    // 写入文件并计算进度
    public static void writeFileFromIS(File file, InputStream is, long downloadedSize,
                                       OnProgressUpdateListener listener) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        byte[] buffer = new byte[8192];
        int len;
        long total = 0;

        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            total += len;
            if (listener != null) {
                listener.onProgress(total);
            }
        }
        fos.flush();
        fos.close();
        is.close();

        if (listener != null) {
            listener.onFinished(total);
        }
    }

    public interface OnProgressUpdateListener {
        void onProgress(long progress);

        void onFinished(long progress);
    }
}