package com.cg.demo.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.constant.C;
import com.cg.demo.databinding.AcSplashBinding;
import com.cg.demo.manager.NetworkType;
import com.cg.demo.manager.RequestFailDialogManager;
import com.cg.demo.manager.UpdateManager;
import com.cg.demo.ui.login.LoginAc;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

public class SplashAc extends BaseMvvmAc<AcSplashBinding, SplashViewModel> {

    private UpdateManager mUpdateManager;
    private boolean networkIsConnect;

    @Override
    protected int initContentView(Bundle savedInstanceState) {
        return R.layout.ac_splash;
    }

    @Override
    protected int initVariableId() {
        return BR.viewModel;
    }

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mUpdateManager.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void initEvents() {
        super.initEvents();
        initListener();
        initDataObservables();
    }

    private void initListener() {
        binding.particleview.setOnParticleAnimListener(() -> {
            ActivityUtils.startActivity(new Intent(this, LoginAc.class));
            ActivityUtils.finishActivity(this);
        });
    }

    private void initDataObservables() {
        viewModel.requestLiveData.observe(this, requestCode -> {
            if (requestCode == -2) {
                // 版本信息请求失败,弹窗重新请求
                requestFailDialog("获取最新版本失败", 0);
                return;
            }
            if (requestCode < 0) {
                ReleaseAppVersionDTO releaseAppVersionDTO = viewModel.releaseVersionLiveData.getValue();

                MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .title("发现新版本 v" + releaseAppVersionDTO.getVersionName())
                        .content(releaseAppVersionDTO.getNotes())
                        .positiveText("立即更新")
                        .cancelable(releaseAppVersionDTO.getStatus() == 0)
                        .onPositive((dialog, which) -> {
                            showDownloadingDialog(releaseAppVersionDTO);
                        })
                        .cancelListener(dialogInterface -> {
                            binding.particleview.startAnim();
                        });

                if (releaseAppVersionDTO.getStatus() == 0) {
                    builder.negativeText("稍后更新")
                            .onNegative((dialog, which) -> {
                                binding.particleview.startAnim();
                            });
                }
                builder.show();
                return;
            }

            // 删除APK目录
            FileUtils.deleteAllInDir(C.APK_STORAGE_DIR);
            // 无需版本更新
            binding.particleview.startAnim();
        });
    }

    private void showDownloadingDialog(ReleaseAppVersionDTO releaseAppVersionDTO) {
        // 进行版本更新
        mUpdateManager = UpdateManager.getInstance(this);
        mUpdateManager.setUpdateInfo(releaseAppVersionDTO);
        mUpdateManager.setOnDownloadProgressListener(new UpdateManager.OnDownloadProgressListener() {
            @Override
            public void onFailed(String errorMsg) {
                UpdateManager.OnDownloadProgressListener.super.onFailed(errorMsg);
                // 下载遇到问题,检测网络是否连接
                if (!networkIsConnect) {
                    requestFailDialog("当前无网络,请连接网络后重试", 1);
                }
            }

            @Override
            public void onCancel() {
                UpdateManager.OnDownloadProgressListener.super.onCancel();
                LogUtils.i("下载取消");
                if (releaseAppVersionDTO.getUpStatus() == 0) {
                    binding.particleview.startAnim();
                    return;
                }
                ActivityUtils.finishAllActivities();
            }
        });

        mUpdateManager.startDownload(this);
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable, NetworkType networkType) {
        super.onNetworkStateChanged(isAvailable, networkType);
        runOnUiThread(() -> {
            if (isAvailable) {
                String networkName;
                switch (networkType) {
                    case MOBILE:
                        networkName = "移动网络";
                        break;
                    case WIFI:
                        networkName = "WiFi";
                        break;
                    case OTHER:
                        networkName = "其他网络";
                        break;
                    case NONE:
                        networkName = "无网络";
                        break;
                    default:
                        networkName = "未知网络";
                        break;
                }
                // 网络可用逻辑
                XToastUtils.info("网络已连接：" + networkName);
                networkIsConnect = true;
//                // 隐藏无网络弹窗
//                NoNetworkManager.getInstance().hideNoNetworkView();
            } else {
                // 网络不可用逻辑
                XToastUtils.info("网络已断开");
                networkIsConnect = false;
//                // 显示无网络弹窗
//                 NoNetworkManager.getInstance().showNoNetworkView();
            }
        });
    }

    private void requestFailDialog(String msg, int type) {
        RequestFailDialogManager requestFailDialogManager = RequestFailDialogManager.getInstance();
        requestFailDialogManager.init(this);
        requestFailDialogManager.showFailDialog(msg, new RequestFailDialogManager.RetryCallback() {

            @Override
            public void onRetry() {
                if (type == 0) {
                    viewModel.getReleaseAppVersion();
                    return;
                }
                mUpdateManager.startDownload(SplashAc.this);
            }

            @Override
            public void onCancel() {
                RequestFailDialogManager.RetryCallback.super.onCancel();
                ActivityUtils.finishAllActivities();
            }
        });
    }
}