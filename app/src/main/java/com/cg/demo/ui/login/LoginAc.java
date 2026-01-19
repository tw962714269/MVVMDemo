package com.cg.demo.ui.login;

import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.bean.LoginBean;
import com.cg.demo.databinding.AcLoginBinding;
import com.cg.demo.event.EventHandlers;
import com.cg.demo.ui.main.MainAc;
import com.xuexiang.xui.utils.XToastUtils;

public class LoginAc extends BaseMvvmAc<AcLoginBinding, LoginViewModel> {

    @Override
    protected int initContentView(Bundle savedInstanceState) {
        return R.layout.ac_login;
    }

    @Override
    protected int initVariableId() {
        return BR.viewModel;
    }

    private void adjustLayout() {
        adjustLayoutForStatusBar(binding.vTop);
        adjustLayoutForNavigationBar(binding.vBottom);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            adjustLayout();
        }
    }

    public class ViewEvents extends EventHandlers {
        public void login() {
            viewModel.login();
        }
    }

    @Override
    public void initEvents() {
        super.initEvents();
        binding.setEvents(new ViewEvents());
    }

    @Override
    public void initViews() {
        super.initViews();
        initDataObservables();
    }

    @Override
    public void onRequestSuccess(Object data) {
        super.onRequestSuccess(data);
        if (data instanceof LoginBean.LoginVO) {
            ActivityUtils.startActivity(new Intent(this, MainAc.class));
            ActivityUtils.finishActivity(this);
        }
    }

    private void initDataObservables() {
        viewModel.loginMsg.observe(this, loginMsg -> {
            XToastUtils.toast(loginMsg);
        });

        viewModel.canLogin.observe(this, canLogin -> {
            binding.tvLogin.setEnabled(canLogin);
            binding.tvLogin.setTextColor(canLogin ? 0xff000000 : 0xff999999);
            binding.tvLogin.setBackgroundColor(canLogin ? 0xFF08F40C : 0xFFF408F4);
        });
    }
}