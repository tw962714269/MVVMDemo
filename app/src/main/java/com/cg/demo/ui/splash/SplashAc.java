package com.cg.demo.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.databinding.AcSplashBinding;
import com.cg.demo.manager.UpdateManager;
import com.cg.demo.ui.login.LoginAc;

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
            LogUtils.v("动画加载完成");
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
                // 进行版本更新
                mUpdateManager = UpdateManager.getInstance(this);
                mUpdateManager.setmUpdateInfo(viewModel.releaseVersionLiveData.getValue());
                mUpdateManager.setOnDownloadProgressListener(new UpdateManager.OnDownloadProgressListener() {
                    @Override
                    public void onProgress(int progress) {
                        LogUtils.i("下载进度：" + progress);
                    }

                    @Override
                    public void onFailed(String errorMsg) {
                        LogUtils.e("下载失败：" + errorMsg);
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.v("下载完成");
                    }
                });

                new Handler(Looper.myLooper()).postDelayed(() -> {
                    mUpdateManager.startDownload(this);
                }, 2_000);
                return;
            }
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
}