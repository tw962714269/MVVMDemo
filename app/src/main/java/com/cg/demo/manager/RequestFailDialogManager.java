package com.cg.demo.manager;

// RequestFailDialogManager.java

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cg.demo.R;

/**
 * 适配XUI 1.2.1版本的接口请求失败重试弹窗管理器
 * 单例模式，支持自定义提示文案和重试回调
 */
public class RequestFailDialogManager {
    private static volatile RequestFailDialogManager sInstance;
    private View mFailDialogView; // 弹窗View
    private Activity mActivity;   // 当前Activity
    private boolean isShowing = false; // 弹窗是否显示
    private RetryCallback mRetryCallback; // 重试回调

    // 重试回调接口
    public interface RetryCallback {
        /**
         * 点击重新请求时触发
         */
        void onRetry();

        /**
         * 点击取消时触发（可选实现）
         */
        default void onCancel() {
        }
    }

    private RequestFailDialogManager() {
    }

    /**
     * 获取单例实例
     */
    public static RequestFailDialogManager getInstance() {
        if (sInstance == null) {
            synchronized (RequestFailDialogManager.class) {
                if (sInstance == null) {
                    sInstance = new RequestFailDialogManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化弹窗（适配XUI 1.2.1初始化方式）
     *
     * @param activity 当前Activity（建议传XUIActivity）
     */
    public void init(Activity activity) {
        mActivity = activity;

        // 加载弹窗布局
        LayoutInflater inflater = LayoutInflater.from(activity);
        mFailDialogView = inflater.inflate(R.layout.layout_request_fail, null);

        // 绑定取消按钮事件
        Button btnCancel = mFailDialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            if (mRetryCallback != null) {
                mRetryCallback.onCancel();
            }
            hideDialog();
        });

        // 绑定重试按钮事件
        Button btnRetry = mFailDialogView.findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(v -> {
            if (mRetryCallback != null) {
                mRetryCallback.onRetry();
            }
            hideDialog();
        });
    }

    /**
     * 显示请求失败弹窗
     *
     * @param failMsg  失败提示文案（不传则用默认文案）
     * @param callback 重试回调
     */
    public void showFailDialog(String failMsg, RetryCallback callback) {
        if (mActivity == null || mFailDialogView == null || isShowing) {
            return;
        }

        // 设置回调
        mRetryCallback = callback;

        // 设置提示文案
        TextView tvFailMsg = mFailDialogView.findViewById(R.id.tv_fail_msg);
        if (failMsg != null && !failMsg.isEmpty()) {
            tvFailMsg.setText(failMsg);
        }

        // 添加弹窗到Activity的根布局
        ViewGroup rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        // 先移除防止重复添加
        if (mFailDialogView.getParent() != null) {
            ((ViewGroup) mFailDialogView.getParent()).removeView(mFailDialogView);
        }
        rootView.addView(mFailDialogView);
        isShowing = true;
    }

    /**
     * 重载：使用默认提示文案显示弹窗
     */
    public void showFailDialog(RetryCallback callback) {
        showFailDialog(null, callback);
    }

    /**
     * 隐藏弹窗
     */
    public void hideDialog() {
        if (mActivity == null || !isShowing || mFailDialogView == null) {
            return;
        }

        ViewGroup parent = (ViewGroup) mFailDialogView.getParent();
        if (parent != null) {
            parent.removeView(mFailDialogView);
        }
        isShowing = false;
        mRetryCallback = null; // 清空回调，避免内存泄漏
    }

    /**
     * 释放资源
     */
    public void release() {
        hideDialog();
        mActivity = null;
        mFailDialogView = null;
        mRetryCallback = null;
    }

    /**
     * 判断弹窗是否显示
     */
    public boolean isShowing() {
        return isShowing;
    }
}