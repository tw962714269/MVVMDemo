package com.cg.demo.ui.splash;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.base.BaseViewModel;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.manager.NetworkMonitorManager;
import com.cg.demo.manager.RequestFailDialogManager;
import com.cg.demo.manager.UpdateManager;
import com.cg.demo.utils.VersionCompareUtils;
import com.google.gson.Gson;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class SplashViewModel extends BaseViewModel<SplashModel> {
    public MutableLiveData<MaterialDialog> dialogLiveData = new MutableLiveData<>();
    public MutableLiveData<UpdateManager> updateManagerLiveDate = new MutableLiveData<>();
    public MutableLiveData<Boolean> startAnimLiveDate = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        getReleaseAppVersion();
    }

    public void getReleaseAppVersion() {
        executeRequest((onStart, onSuccess, onError) -> {
            model.getReleaseAppVersion(disposable -> {
                LogUtils.i("http开始");
                //请求开始，当前在主线程回调
                onStart.onStart(disposable);
            }, () -> {
                LogUtils.i("http结束");
                //请求结束，当前在主线程回调
            }, data -> {    //订阅观察者，
                LogUtils.i("http成功：" + new Gson().toJson(data));
                onSuccess.onSuccess(data);
                if (CollectionUtils.isEmpty(data)) {
                    startAnimLiveDate.postValue(true);
                    return;
                }

                ReleaseAppVersionDTO releaseAppVersionDTO = data.get(0);
                boolean needUpdate = VersionCompareUtils.compareVersion(releaseAppVersionDTO.getVersionName());
                if (needUpdate) {
                    new Handler(Looper.getMainLooper()).post(() -> showUpdateDialog(releaseAppVersionDTO));
                    return;
                }

                startAnimLiveDate.postValue(true);
            }, throwable -> {
                LogUtils.e("http失败：" + throwable.fillInStackTrace());
                onError.onError(throwable);
                new Handler(Looper.getMainLooper()).post(() -> {
                    // 版本信息请求失败,弹窗重新请求
                    requestFailDialog("获取最新版本失败", 0);
                });
            });
        });
    }

    private void showUpdateDialog(ReleaseAppVersionDTO releaseAppVersionDTO) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ActivityUtils.getTopActivity())
                .title("发现新版本 v" + releaseAppVersionDTO.getVersionName())
                .content(releaseAppVersionDTO.getNotes())
                .positiveText("立即更新")
                .autoDismiss(false)
                .cancelable(releaseAppVersionDTO.getStatus() == 0)
                .onPositive((dialog, which) -> {
                    showDownloadingDialog(dialog,releaseAppVersionDTO);
                })
                .cancelListener(dialogInterface -> {
                    startAnimLiveDate.setValue(true);
                });

        if (releaseAppVersionDTO.getStatus() == 0) {
            builder.negativeText("稍后更新")
                    .onNegative((dialog, which) -> {
                        startAnimLiveDate.setValue(true);
                        dialog.dismiss();
                    });
        }
        MaterialDialog updateDialog = builder.show();
        dialogLiveData.setValue(updateDialog);
    }

    private void showDownloadingDialog(MaterialDialog dialog,ReleaseAppVersionDTO releaseAppVersionDTO) {
        // 进行版本更新
        UpdateManager mUpdateManager = UpdateManager.getInstance(ActivityUtils.getTopActivity());
        updateManagerLiveDate.setValue(mUpdateManager);
        mUpdateManager.setUpdateInfo(releaseAppVersionDTO);
        mUpdateManager.setOnDownloadProgressListener(new UpdateManager.OnDownloadProgressListener() {
            @Override
            public void onFailed(String errorMsg) {
                UpdateManager.OnDownloadProgressListener.super.onFailed(errorMsg);
                // 下载遇到问题,检测网络是否连接
                if (!NetworkMonitorManager.getInstance().isNetworkAvailable()) {
                    requestFailDialog("当前无网络,请连接网络后重试", 1);
                }
            }

            @Override
            public void onCancel() {
                UpdateManager.OnDownloadProgressListener.super.onCancel();
                LogUtils.i("下载取消");
                if (releaseAppVersionDTO.getUpStatus() == 0) {
                    startAnimLiveDate.setValue(true);
                    return;
                }
                ActivityUtils.finishAllActivities();
            }
        });

        if (mUpdateManager.startDownload()) dialog.dismiss();
    }

    private void requestFailDialog(String msg, int type) {
        RequestFailDialogManager requestFailDialogManager = RequestFailDialogManager.getInstance();
        requestFailDialogManager.init(ActivityUtils.getTopActivity());
        requestFailDialogManager.showFailDialog(msg, new RequestFailDialogManager.RetryCallback() {

            @Override
            public void onRetry() {
                if (type == 0) {
                    getReleaseAppVersion();
                    return;
                }
                updateManagerLiveDate.getValue().startDownload();
            }

            @Override
            public void onCancel() {
                RequestFailDialogManager.RetryCallback.super.onCancel();
                ActivityUtils.finishAllActivities();
            }
        });
    }
}
