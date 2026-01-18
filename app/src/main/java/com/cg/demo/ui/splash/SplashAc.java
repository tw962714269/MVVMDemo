package com.cg.demo.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.databinding.AcSplashBinding;
import com.cg.demo.manager.NetworkType;
import com.cg.demo.manager.UpdateManager;
import com.cg.demo.ui.login.LoginAc;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

public class SplashAc extends BaseMvvmAc<AcSplashBinding, SplashViewModel> {

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            MaterialDialog updateDialog = viewModel.dialogLiveData.getValue();
            if (updateDialog != null && updateDialog.isShowing()) {
                updateDialog.dismiss();
            }
        }
        UpdateManager value = viewModel.updateManagerLiveDate.getValue();
        if (value == null) {
            XToastUtils.warning("下载管理器初始化失败");
            return;
        }
        viewModel.updateManagerLiveDate.getValue().onRequestPermissionsResult(requestCode, resultCode);
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
        viewModel.startAnimLiveDate.observe(this, aBoolean -> {
            binding.particleview.startAnim();
        });
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable, NetworkType networkType) {
        super.onNetworkStateChanged(isAvailable, networkType);
        runOnUiThread(() -> {
            if (!isAvailable) {
                // 网络不可用逻辑
                XToastUtils.info("网络已断开");
            }
        });
    }
}