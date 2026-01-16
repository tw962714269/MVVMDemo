package com.cg.demo.ui.splash;

import android.annotation.SuppressLint;

import com.cg.demo.base.BaseModel;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.network.base_api.ApiUtil;
import com.cg.demo.network.base_api.entity.Response;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class SplashModel extends BaseModel {

    @SuppressLint("CheckResult")
    public void getReleaseAppVersion(Consumer<? super Disposable> onSubscribe,
                                     Action onFinally,
                                     @NonNull Consumer<? super List<ReleaseAppVersionDTO>> onNext,
                                     Consumer<? super Throwable> onError) {
        ApiUtil.getApi().getReleaseAppVersion()
                .doOnSubscribe(onSubscribe)
                .doFinally(onFinally)
                .subscribe(onNext, onError);
    }
}
