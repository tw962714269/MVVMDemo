package com.cg.demo.impl;

import android.view.View;

import com.xuexiang.xui.utils.XToastUtils;

/**
 * FileName: INetView
 * Created by zlx on 2020/9/22 10:38
 * Email: 1170762202@qq.com
 * Description:
 */
public interface INetView {

    void showLoadingDialog();

    void dismissLoadingDialog();

    void showDefaultErrorTip(Throwable throwable);

    void showRequestCancelledTip();
    void onRequestSuccess(Object data);
    void onRequestFailed(Throwable throwable);
    void onRequestCancelled();
    void onRequestCompleted();
}
