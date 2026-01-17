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
import com.cg.demo.manager.UpdateManager;
import com.cg.demo.ui.login.LoginAc;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

public class SplashAc extends BaseMvvmAc<AcSplashBinding, SplashViewModel> {

    private UpdateManager mUpdateManager;

    @Override
    protected int initContentView(Bundle savedInstanceState) {
        return R.layout.ac_splash;
    }

    @Override
    protected int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initViews() {
        super.initViews();
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

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mUpdateManager.onRequestPermissionsResult(requestCode, grantResults);
    }

    private void showDownloadingDialog(ReleaseAppVersionDTO releaseAppVersionDTO) {
        // 进行版本更新
        mUpdateManager = UpdateManager.getInstance(this);
        mUpdateManager.setmUpdateInfo(releaseAppVersionDTO);
        mUpdateManager.setOnDownloadProgressListener(new UpdateManager.OnDownloadProgressListener() {
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
}