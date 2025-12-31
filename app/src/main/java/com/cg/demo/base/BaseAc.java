package com.cg.demo.base;

import static com.xuexiang.xui.utils.XToastUtils.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.cg.demo.loadsir.EmptyCallback;
import com.cg.demo.loadsir.LoadingCallback;
import com.cg.demo.utils.DoubleClickExitDetector;
import com.cg.demo.utils.InputUtils;
import com.cg.demo.utils.LanguageUtils;
import com.google.gson.Gson;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
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


    protected TextView tvTitle;

    protected ImageView ivLeft;
    protected ImageView ivRight;

    private LoadService loadService;

    protected MiniLoadingDialog mMiniLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        beforeOnCreate(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        afterOnCreate(savedInstanceState);
        setTheme(getMTheme());
        setSuspension();
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        initImmersionBar(fullScreen());
        initEvents();
        initViews();
        doubleClickExitDetector =
                new DoubleClickExitDetector(this, "再按一次退出", 2000);
    }

    @Override
    public void beforeOnCreate(int requestedOrientation) {
        setRequestedOrientation(requestedOrientation);//竖屏
    }

    @Override
    public void afterOnCreate(Bundle savedInstanceState) {

    }

    @Override
    public void initEvents() {
        mMiniLoadingDialog = WidgetUtils.getMiniLoadingDialog(this);

        tvTitle = findViewById(R.id.tvTitle);
        ivLeft = findViewById(R.id.ivLeft);
        ivRight = findViewById(R.id.ivRight);
        if (ivLeft != null) {
            ivLeft.setOnClickListener(view -> finish());
        }
    }

    protected void setOnRightImgClickListener(View.OnClickListener listener) {
        if (ivRight != null) {
            ivRight.setOnClickListener(listener);
        }
    }

    @Override
    public void showLoading() {
        if (loadService == null) {
            loadService = LoadSir.getDefault().register(this, v -> onRetryBtnClick());
        }
        loadService.showCallback(LoadingCallback.class);
    }

    @Override
    public void showLoading(View view) {
        if (loadService == null) {
            loadService = LoadSir.getDefault().register(view, v -> onRetryBtnClick());
        }
        loadService.showCallback(LoadingCallback.class);
    }

    @Override
    public void showEmpty() {
        if (loadService == null) {
            loadService = LoadSir.getDefault().register(this, v -> onRetryBtnClick());
        }
        loadService.showCallback(EmptyCallback.class);
    }

    @Override
    public void showSuccess() {
        if (loadService == null) {
            loadService = LoadSir.getDefault().register(this, v -> onRetryBtnClick());
        }
        loadService.showSuccess();
    }

    @Override
    public void onRetryBtnClick() {

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

    protected void setRightImg(int bg) {
        if (ivRight != null) {
            if (bg <= 0) {
                ivRight.setVisibility(View.GONE);
            } else {
                ivRight.setVisibility(View.VISIBLE);
                ivRight.setImageResource(bg);
            }
        }

    }

    protected void setLeftImg(int bg) {
        if (ivLeft != null) {
            if (bg <= 0) {
                ivLeft.setVisibility(View.GONE);
            } else {
                ivLeft.setVisibility(View.VISIBLE);
                ivLeft.setImageResource(bg);
            }
        }
    }

    @Override
    public void initImmersionBar(boolean fullScreen) {
        if (fullScreen) {
            ImmersionBar.with(this)
                    .fullScreen(true)
                    .keyboardEnable(true)
                    .hideBar(BarHide.FLAG_HIDE_BAR)
                    .init();
            return;
        }
        ImmersionBar.with(this)
                .statusBarView(R.id.statusBarView)
                .statusBarDarkFont(true)
                .transparentBar()
                .keyboardEnable(true)
                .hideBar(BarHide.FLAG_SHOW_BAR)
                .init();
    }

    public void resetImmersionBar(boolean fullScreen, boolean isBlack) {
        if (fullScreen) {
            ImmersionBar.with(this)
                    .reset()
                    .fullScreen(true)
                    .keyboardEnable(true)
                    .hideBar(BarHide.FLAG_HIDE_BAR)
                    .init();
            return;
        }
        ImmersionBar.with(this)
                .reset()
                .statusBarView(R.id.statusBarView)
                .statusBarDarkFont(isBlack)
                .transparentBar()
                .keyboardEnable(true)
                .init();
    }

    /**
     * 是否全屏
     *
     * @return true全屏
     */
    protected boolean fullScreen() {
        return false;
    }

    protected void setAcTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchHideSoft()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    hideKeyboard(v.getWindowToken());
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 是否触摸edittext以外的隐藏软键盘
     *
     * @return
     */
    protected boolean touchHideSoft() {
        return true;
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

    /**
     * 悬浮窗设置
     */
    private void setSuspension() {
        WindowManager.LayoutParams mParams = getWindow().getAttributes();
        mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    @Override
    public void initViews() {

    }

    protected int getLayoutId() {
        return 0;
    }

    protected int getMTheme() {
        return R.style.Theme_FullScreen;
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


    private DoubleClickExitDetector doubleClickExitDetector;

    public boolean isDoubleClickExit() {
        return ActivityUtils.getActivityList().size() == 1;
    }

    @Override
    public void onBackPressed() {
        if (isDoubleClickExit()) {
            boolean isExit = doubleClickExitDetector.click();
            if (isExit) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
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
//        RxJavaUtils.delay(30, aLong -> mMiniLoadingDialog.dismiss());
    }

    public void loadingDialogDismiss() {
        mMiniLoadingDialog.dismiss();
    }


    /**
     * 调整布局margin，适配状态栏高度
     */
    public void adjustLayoutForStatusBar(View view) {
        // 获取状态栏高度
        int statusBarHeight = ImmersionBar.getStatusBarHeight(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin + statusBarHeight, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);
    }

    /**
     * 调整布局margin，适配导航栏高度
     */
    public void adjustLayoutFoNavigationBar(View view) {
        // 获取导航栏高度
        int navigationBarHeight = ImmersionBar.getNavigationBarHeight(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin + navigationBarHeight);
        view.setLayoutParams(params);
    }
}
