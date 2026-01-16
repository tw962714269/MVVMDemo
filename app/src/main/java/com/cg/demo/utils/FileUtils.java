package com.cg.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    // 写入文件并计算进度
    public static void writeFileFromIS(File file, InputStream is, long downloadedSize,
                                       OnProgressUpdateListener listener) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        byte[] buffer = new byte[8192];
        int len;
        long total = downloadedSize;
        long fileSize = file.length() > 0 ? file.length() : 0;

        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            total += len;
            // 计算进度
            int progress = (int) (total * 100 / (fileSize + (total - downloadedSize)));
            if (listener != null) {
                listener.onProgress(progress);
            }
        }
        fos.flush();
        fos.close();
        is.close();

        if (listener != null) {
            listener.onProgress(100);
        }
    }

    public interface OnProgressUpdateListener {
        void onProgress(int progress);
    }
}