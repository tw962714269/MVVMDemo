package com.cg.demo.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.base.BaseViewModel;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.utils.VersionCompareUtils;
import com.google.gson.Gson;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class SplashViewModel extends BaseViewModel<SplashModel> {

    /**
     * 接口调用状态
     */
    public MutableLiveData<Integer> requestLiveData = new MutableLiveData<>();
    public MutableLiveData<ReleaseAppVersionDTO> releaseVersionLiveData = new MutableLiveData<>();

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
                if (CollectionUtils.isEmpty(data)) {
                    requestLiveData.postValue(0);
                    return;
                }
                ReleaseAppVersionDTO releaseAppVersionDTO = data.get(0);
                int code = VersionCompareUtils.compareVersion(AppUtils.getAppVersionName(), releaseAppVersionDTO.getVersionName());

                releaseVersionLiveData.postValue(releaseAppVersionDTO);

                requestLiveData.postValue(code);
                onSuccess.onSuccess(releaseAppVersionDTO);
            }, throwable -> {
                requestLiveData.postValue(-2);
                LogUtils.e("http失败：" + throwable.fillInStackTrace());
                onError.onError(throwable);
            });
        });
    }
}
