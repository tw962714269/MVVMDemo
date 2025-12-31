package com.cg.demo.ui.login;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.BR;
import com.cg.demo.R;
import com.cg.demo.base.BaseMvvmAc;
import com.cg.demo.databinding.AcLoginBinding;
import com.cg.demo.event.EventHandlers;
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

    public class ViewEvents extends EventHandlers {
        public void exit() {
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
        viewModel.loginMsg.observe(this, loginMsg -> {
            XToastUtils.toast(loginMsg);
        });

        viewModel.loginSuccess.observe(this, success -> {
            LogUtils.i("loginSuccess : " + success);
        });

        viewModel.canLogin.observe(this, canLogin -> {
            binding.tvLogin.setEnabled(canLogin);
            binding.tvLogin.setTextColor(canLogin ? 0xff000000 : 0xff999999);
        });
    }
}