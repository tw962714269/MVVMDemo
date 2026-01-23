package com.cg.demo.ui.login;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.cg.demo.base.BaseViewModel;
import com.cg.demo.bean.LoginBean;
import com.cg.demo.utils.RSAUtil;
import com.cg.demo.utils.SPFullUtils;

import lombok.Getter;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class LoginViewModel extends BaseViewModel<LoginModel> {
    // ========== 定义【普通Java变量】用于XML双向绑定，必须给get/set方法 ==========
    @Getter
    private String usernameInput; // 账号输入内容-普通变量
    @Getter
    private String passwordInput; // 密码输入内容-普通变量


    // 私有化的可变LiveData - 只在ViewModel内部修改
    private final MutableLiveData<String> mUsernameInput = new MutableLiveData<>("");
    private final MutableLiveData<String> mPasswordInput = new MutableLiveData<>("");
    private final MutableLiveData<String> mLoginMsg = new MutableLiveData<>();

    // 对外暴露不可变LiveData - View层只能观察，不能修改
    public LiveData<String> loginMsg = mLoginMsg;

    // 核心校验：是否可以点击登录按钮（账号非空+密码非空+密码≥6位），自动根据输入变化更新
    public LiveData<Boolean> canLogin = Transformations.map(mUsernameInput, username -> {
        String pwd = mPasswordInput.getValue() == null ? "" : mPasswordInput.getValue();
        return !username.trim().isEmpty() && !pwd.trim().isEmpty() && pwd.trim().length() >= 6;
    });

    public void setUsernameInput(String usernameInput) {
        this.usernameInput = usernameInput;
        mUsernameInput.setValue(usernameInput);
    }

    public void setPasswordInput(String passwordInput) {
        this.passwordInput = passwordInput;
        mPasswordInput.setValue(passwordInput);
    }

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mPasswordInput.observeForever(password -> {
            String username = mUsernameInput.getValue();
            mUsernameInput.setValue(username);
        });
    }

    public void login() {
        String username = mUsernameInput.getValue().trim();
        String password = mPasswordInput.getValue().trim();

        // 这里的校验可以省略，因为按钮已经被控制不可点击了，做兜底即可
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            mLoginMsg.setValue("账号或密码不可为空");
            return;
        }

        //构建登录实例
        LoginBean.LoginDTO loginDTO = LoginBean.LoginDTO.builder()
                .loginName(username)
                .loginCode(RSAUtil.encory(password))
                .loginType(2)
                .build();

        //登录请求
        executeRequest((onStart, onSuccess, onError) -> {
            model.login(loginDTO, disposable -> {
                //请求开始，当前在主线程回调
                onStart.onStart(disposable);
            }, () -> {
                //请求结束，当前在主线程回调
            }, data -> {    //订阅观察者，
                if (data.getData() != null && !TextUtils.isEmpty(data.getData().getToken())) {
                    mLoginMsg.postValue("登录成功");
                    SPFullUtils.getInstance().putUserToken(data.getData().getToken());
                    onSuccess.onSuccess(data.getData());
                }
            }, throwable -> {
                onError.onError(throwable);
            });
        });
    }
}
