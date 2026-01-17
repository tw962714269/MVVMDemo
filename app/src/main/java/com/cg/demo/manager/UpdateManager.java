package com.cg.demo.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.constant.C;
import com.cg.demo.utils.FileUtils;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 版本更新
 */
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
    private Future<?> downloadFuture;
    private DownloadRunnable downloadRunnable;

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

    public void setUpdateInfo(ReleaseAppVersionDTO mUpdateInfo) {
        this.mUpdateInfo = mUpdateInfo;
    }

    // 开始下载APK
    public void startDownload(Activity activity) {
        if (mUpdateInfo == null) {
            XToastUtils.info("暂无更新信息");
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
        if (!mContext.getPackageManager().canRequestPackageInstalls()) {
            //请求授权安装应用权限
            Uri packageURI = Uri.parse("package:" + ActivityUtils.getTopActivity().getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
            ActivityUtils.getTopActivity().startActivityForResult(intent, 1);
            return;
        }

        // 创建下载任务
        downloadApk();
    }

    // 创建下载任务
    private void downloadApk() {
        cancelDownloadTask();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ActivityUtils.getTopActivity())
                .title("版本更新")
                .progress(false, mUpdateInfo.getFileSize().intValue(), true)
                .cancelable(false);
        downloadRunnable = new DownloadRunnable(builder);
        downloadFuture = mExecutorService.submit(downloadRunnable);
    }

    // 取消下载任务
    private void cancelDownloadTask() {
        if (downloadFuture != null && !downloadFuture.isCancelled() && !downloadFuture.isDone()) {
            downloadFuture.cancel(true); // 中断任务
            LogUtils.d("下载任务已取消");
        }

        downloadRunnable = null;
        downloadFuture = null;
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
        File apkDir = new File(C.APK_STORAGE_DIR);
        if (!apkDir.exists()) {
            apkDir.mkdirs();
        }
        return new File(apkDir, "app_" + versionName + ".apk");
    }

    // 下载APK核心逻辑（使用OkHttp实现断点续传）
    private class DownloadRunnable implements Runnable {
        private MaterialDialog.Builder builder;
        private MaterialDialog downloadDialog;

        public DownloadRunnable(MaterialDialog.Builder builder) {
            this.builder = builder;
        }

        @Override
        public void run() {
            // 检测任务是否被取消
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // 下载路径
            File apkFile = getApkFile(mUpdateInfo.getVersionName());
            if (apkFile.exists()) {
                if (apkFile.length() == mUpdateInfo.getFileSize()) {
                    mMainHandler.post(() -> {
                        builder.content("软件安装包已存在(如安装遇到问题,请重新下载)")
                                .autoDismiss(false)
                                .negativeText("重新下载")
                                .onNegative((dialog, which) -> {
                                    apkFile.delete();
                                    downloadApk();
                                    dialog.dismiss();
                                })
                                .positiveText("立即安装")
                                .onPositive((dialog, which) -> {
                                    installApk(apkFile);
                                }).show().setProgress(mUpdateInfo.getFileSize().intValue());
                    });
                    return;
                }
                apkFile.delete();
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .build();
            mMainHandler.post(() -> {
                downloadDialog = builder.content("正在下载中,请稍等...")
                        .negativeText("取消")
                        .onNegative((dialog, which) -> {
                            cancelDownloadTask();
                            mProgressListener.onCancel();
                            dialog.dismiss();
                        }).show();
            });

            // 已下载的大小
            long downloadedSize = apkFile.exists() ? apkFile.length() : 0;
            // 总大小 todo 文件大小
            long totalSize = mUpdateInfo.getFileSize();
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
                FileUtils.writeFileFromIS(apkFile, body.byteStream(), totalSize,
                        new FileUtils.OnProgressUpdateListener() {
                            @Override
                            public void onProgress(long progress) {
                                // 进度回调到主线程
                                mMainHandler.post(() -> {
                                    if (mProgressListener != null) {
                                        mProgressListener.onProgress(progress);
                                    }
                                    downloadDialog.setProgress((int) progress);
                                });
                            }

                            @Override
                            public void onFinished(long progress) {
                                mProgressListener.onComplete();
                                if (mProgressListener != null) {
                                    mProgressListener.onComplete();
                                }
                            }
                        });

                // 下载完成，安装APK
                mMainHandler.post(() -> {
                    if (downloadDialog == null) {
                        return;
                    }

                    if (downloadDialog.isShowing()) {
                        downloadDialog.dismiss();
                    }

                    downloadDialog.getBuilder()
                            .content("下载完成")
                            .autoDismiss(false)
                            .positiveText("立即安装")
                            .onPositive((dialog, which) -> {
                                installApk(apkFile);
                            }).show().setProgress(mUpdateInfo.getFileSize().intValue());
                    installApk(apkFile);
                });

            } catch (Exception e) {
                e.printStackTrace();
                mMainHandler.post(() -> {
                    XToastUtils.error("下载失败：" + e.getMessage());
                    if (mProgressListener != null) {
                        mProgressListener.onFailed(e.getMessage());
                    }

                    if (downloadDialog == null) {
                        return;
                    }

                    if (downloadDialog.isShowing()) {
                        downloadDialog.dismiss();
                    }

                    if (e instanceof UnknownHostException) {
                        return;
                    }
                    if (e.getMessage() != null && e.getMessage().contains("Software caused connection abort")) {
                        return;
                    }

                    downloadDialog = builder.content("下载遇到问题,请重新下载...")
                            .negativeText("重新下载")
                            .onNegative((dialog, which) -> {
                                downloadApk();
                                dialog.dismiss();
                            }).show();
                });
            }
        }
    }

    // 权限请求回调处理
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload((Activity) mContext);
            } else {
                XToastUtils.warning("存储权限被拒绝，无法下载更新");
            }
        } else if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload((Activity) mContext);
            } else {
                XToastUtils.warning("安装权限被拒绝，无法完成更新");
            }
        }
    }

    // 设置下载进度监听
    public void setOnDownloadProgressListener(OnDownloadProgressListener listener) {
        this.mProgressListener = listener;
    }

    // 下载进度回调
    public interface OnDownloadProgressListener {
        default void onProgress(long progress) {

        }

        default void onFailed(String errorMsg) {

        }

        default void onComplete() {

        }

        default void onCancel() {

        }
    }
}