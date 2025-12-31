package com.cg.demo.ui.login;

import android.annotation.SuppressLint;

import com.cg.demo.base.BaseModel;
import com.cg.demo.bean.LoginBean;
import com.cg.demo.network.base_api.ApiUtil;
import com.cg.demo.network.base_api.entity.Response;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class LoginModel extends BaseModel {
    @SuppressLint("CheckResult")
    public void login(LoginBean.LoginDTO loginDTO,
                      Consumer<? super Disposable> onSubscribe,
                      Action onFinally,
                      @NonNull Consumer<? super Response<LoginBean.LoginVO>> onNext,
                      Consumer<? super Throwable> onError) {
        ApiUtil.getApi().login(loginDTO)
                .doOnSubscribe(onSubscribe)
                .doFinally(onFinally)
                .subscribe(onNext, onError);
    }
}
