package com.cg.demo.base;

import static com.xuexiang.xui.utils.XToastUtils.toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.R;
import com.cg.demo.bean.MessageEvent;
import com.cg.demo.impl.IAcView;
import com.cg.demo.impl.INetView;
import com.cg.demo.network.Throwable.Exceptions;
import com.cg.demo.ui.login.LoginAc;
import com.cg.demo.ui.main.MainAc;
import com.cg.demo.ui.splash.SplashAc;
import com.cg.demo.utils.ExitAppUtils;
import com.cg.demo.utils.InputUtils;
import com.cg.demo.utils.LanguageUtils;
import com.cg.demo.utils.NavigationStatusBarUtils;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.MiniLoadingDialog;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * Created by zlx on 2017/6/23.
 */

public abstract class BaseAc extends AppCompatActivity implements INetView, IAcView {
    protected MiniLoadingDialog mMiniLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        afterOnCreate(savedInstanceState);

        //// 仅首页注册退出回调，其他页面不注册（默认返回上一页）
        if (this instanceof SplashAc || this instanceof LoginAc || this instanceof MainAc)
            registerExitCallback();

        initViews();
        initEvents();
    }

    @Override
    public void beforeOnCreate() {
    }

    @Override
    public void afterOnCreate(Bundle savedInstanceState) {
        setStatusBarDarkFont(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (shouldSupportMultiLanguage()) {
            Context context = LanguageUtils.attachBaseContext(newBase);
            final Configuration configuration = context.getResources().getConfiguration();
            // 此处的ContextThemeWrapper是androidx.appcompat.view包下的
            // 你也可以使用android.view.ContextThemeWrapper，但是使用该对象最低只兼容到API 17
            // 所以使用 androidx.appcompat.view.ContextThemeWrapper省心
            final ContextThemeWrapper wrappedContext = new ContextThemeWrapper(context,
                    R.style.Theme_AppCompat_Empty) {
                @Override
                public void applyOverrideConfiguration(Configuration overrideConfiguration) {
                    if (overrideConfiguration != null) {
                        overrideConfiguration.setTo(configuration);
                    }
                    super.applyOverrideConfiguration(overrideConfiguration);
                }
            };
            super.attachBaseContext(wrappedContext);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    protected boolean shouldSupportMultiLanguage() {
        return true;
    }

    /**
     * 设置状态栏字体颜色
     *
     * @param isBlack true.黑色 false.白色
     */
    public void setStatusBarDarkFont(boolean isBlack) {
        ImmersionBar.with(this)
                .statusBarDarkFont(isBlack)
                .transparentBar()
                .keyboardEnable(false)
                .init();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);

            int left = l[0];
            int top = l[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();

            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                ((EditText) v).setCursorVisible(true);
                return false;
            } else {
                ((EditText) v).setCursorVisible(false);
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }


    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    public void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void initViews() {
        mMiniLoadingDialog = WidgetUtils.getMiniLoadingDialog(this);
    }

    @Override
    public void initEvents() {
    }

    @SuppressLint("CheckResult")
    public void requestPermissions(String... permissions) {
        XXPermissions.with(this)
                .permission(permissions)
                // 设置权限请求拦截器（局部设置）
//                .interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                .unchecked()
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            toast("获取部分权限成功，但部分权限未正常授予" + new Gson().toJson(permissions));
                            return;
                        }
//                        toast("获取录音和日历权限成功");
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            new MaterialDialog.Builder(BaseAc.this)
                                    .content("被永久拒绝授权，请手动授予")
                                    .positiveText("去授予")
                                    .negativeText("拒绝")
                                    .onPositive((dialog, which) -> {
                                        XXPermissions.startPermissionActivity(BaseAc.this, permissions);

                                    })
                                    .show();
                        } else {
                            toast("获取权限失败");
                        }
                    }
                });
    }

    public void getPermissionSuccess() {
        LogUtils.v("Base--->getPermissionSuccess");
    }

    public void getPermissionFailured() {
        LogUtils.v("Base--->getPermissionFail");
    }

    /**
     * 注册首页的退出回调
     */
    private void registerExitCallback() {
        // 退出逻辑：关闭所有Activity
        ExitAppUtils.registerExitCallback(
                getOnBackPressedDispatcher(),
                this,
                ActivityUtils::finishAllActivities
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputUtils.hideInputMethod(this);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    /**
     * EventBus事件传递
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(MessageEvent event) {
        if (event.getSender() == this) return;
        String text = event.getMsg().toString();
        LogUtils.v("收到消息：" + event.getMsg().toString());
        EventBus.getDefault().removeStickyEvent(event);
    }

    public void requestLoadingDialogShow() {
        if (mMiniLoadingDialog.isLoading() || mMiniLoadingDialog.isShowing()) return;
        mMiniLoadingDialog.show();
    }

    public void loadingDialogDismiss() {
        mMiniLoadingDialog.dismiss();
    }

    /**
     * 调整布局margin，适配状态栏高度
     */
    public void adjustLayoutForStatusBar(View view) {
        // 获取状态栏高度
        int statusBarHeight = NavigationStatusBarUtils.getActualStatusBarHeight(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, statusBarHeight, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);
    }

    public void adjustLayoutForNavigationBar(View view) {
        // 获取导航栏高度
        int navigationBarHeight = NavigationStatusBarUtils.getActualNavigationBarHeight(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, navigationBarHeight);
        view.setLayoutParams(params);
    }

    // ========== 通用UI方法（Base层实现，业务层可重写） ==========

    /**
     * 显示加载弹窗（Base层默认实现，业务层可重写）
     */
    @Override
    public void showLoadingDialog() {
        // 示例：显示通用加载弹窗
        requestLoadingDialogShow();
    }

    /**
     * 关闭加载弹窗（Base层默认实现，业务层可重写）
     */
    @Override
    public void dismissLoadingDialog() {
        loadingDialogDismiss();
    }

    /**
     * 显示默认错误提示（Base层默认实现，业务层可重写）
     */
    @Override
    public void showDefaultErrorTip(Throwable throwable) {
        String errorMsg = Exceptions.getErrorMsg(throwable);
        XToastUtils.error("请求失败：" + (!TextUtils.isEmpty(errorMsg) ? errorMsg : "未知错误"));
    }

    /**
     * 显示请求取消提示（Base层默认实现）
     */
    @Override
    public void showRequestCancelledTip() {
        XToastUtils.info("请求已取消");
    }

    // ========== 业务层需实现/重写的抽象方法（差异化处理） ==========

    /**
     * 请求成功（业务层实现具体逻辑）
     */
    @Override
    public void onRequestSuccess(Object data) {

    }

    /**
     * 请求失败（业务层可扩展差异化处理）
     */
    @Override
    public void onRequestFailed(Throwable throwable) {
    }

    /**
     * 请求取消（业务层可扩展）
     */
    @Override
    public void onRequestCancelled() {
    }

    /**
     * 请求结束（业务层可扩展）
     */
    @Override
    public void onRequestCompleted() {
    }
}
