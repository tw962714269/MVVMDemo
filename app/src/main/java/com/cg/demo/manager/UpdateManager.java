package com.cg.demo.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpdateManager {
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_INSTALL_PERMISSION = 1002;

    private Context mContext;
    @Getter
    private ReleaseAppVersionDTO mUpdateInfo;
    // 下载进度回调
    private OnDownloadProgressListener mProgressListener;
    // 单例模式
    private static UpdateManager sInstance;
    // 线程池
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    // 私有化构造
    private UpdateManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    // 获取单例
    public static UpdateManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UpdateManager.class) {
                if (sInstance == null) {
                    sInstance = new UpdateManager(context);
                }
            }
        }
        return sInstance;
    }

    // 开始下载APK
    public void startDownload(Activity activity) {
        if (mUpdateInfo == null) {
            Toast.makeText(mContext, "暂无更新信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查存储权限（Android 6.0+）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
            return;
        }

        // 检查安装权限（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !mContext.getPackageManager().canRequestPackageInstalls()) {
            //请求授权安装应用权限
            Uri packageURI = Uri.parse("package:" + ActivityUtils.getTopActivity().getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
            ActivityUtils.getTopActivity().startActivityForResult(intent, 1);
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
//                    REQUEST_INSTALL_PERMISSION);
            return;
        }

        // 开始下载
        downloadApk();
    }

    // 下载APK核心逻辑（使用OkHttp实现断点续传）
    private void downloadApk() {
        mExecutorService.execute(() -> {
            OkHttpClient client = new OkHttpClient.Builder()
                    .build();

            // 下载路径
            File apkFile = getApkFile(mUpdateInfo.getVersionName());
            // 已下载的大小
            long downloadedSize = apkFile.exists() ? apkFile.length() : 0;
            // 总大小
            long totalSize = 125552153l;
            // 断点续传：设置Range请求头
            Request request = new Request.Builder()
                    .url(mUpdateInfo.getDownAddress())
                    .addHeader("Range", "bytes=" + downloadedSize + "-" + totalSize)
                    .build();

            try {
                Call call = client.newCall(request);
                Response response = call.execute();
                if (!response.isSuccessful()) {
                    throw new IOException("下载失败：" + response.code());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("响应体为空");
                }

                // 写入文件
                FileUtils.writeFileFromIS(apkFile, body.byteStream(), downloadedSize,
                        (progress) -> {
                            // 进度回调到主线程
                            mMainHandler.post(() -> {
                                if (mProgressListener != null) {
                                    mProgressListener.onProgress(progress);
                                }
                            });
                        });

                // 下载完成，安装APK
                mMainHandler.post(() -> installApk(apkFile));

            } catch (Exception e) {
                e.printStackTrace();
                mMainHandler.post(() -> {
                    Toast.makeText(mContext, "下载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (mProgressListener != null) {
                        mProgressListener.onFailed(e.getMessage());
                    }
                });
            }
        });
    }

    // 3. 安装APK
    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(ActivityUtils.getTopActivity(), mContext.getPackageName() + ".fileProvider", apkFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    // 获取APK保存路径
    private File getApkFile(String versionName) {
        File apkDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "apk");
        if (!apkDir.exists()) {
            apkDir.mkdirs();
        }
        return new File(apkDir, "app_" + versionName + ".apk");
    }

    public void setmUpdateInfo(ReleaseAppVersionDTO mUpdateInfo) {
        this.mUpdateInfo = mUpdateInfo;
    }

    // 权限请求回调处理
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload((Activity) mContext);
            } else {
                Toast.makeText(mContext, "存储权限被拒绝，无法下载更新", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload((Activity) mContext);
            } else {
                Toast.makeText(mContext, "安装权限被拒绝，无法完成更新", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 设置下载进度监听
    public void setOnDownloadProgressListener(OnDownloadProgressListener listener) {
        this.mProgressListener = listener;
    }

    // 下载进度回调
    public interface OnDownloadProgressListener {
        void onProgress(int progress);

        void onFailed(String errorMsg);

        void onComplete();
    }
}