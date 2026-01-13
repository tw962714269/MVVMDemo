package com.cg.demo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.xuexiang.xui.utils.XToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 再按一次退出应用工具类（使用XToastUtils提示，适配OnBackPressedDispatcher）
 */
public class ExitAppUtils {
    // 默认退出间隔（2000毫秒）
    private static final long DEFAULT_EXIT_INTERVAL = 2000;
    // 按页面存储最后点击时间（key：Activity类名，value：最后点击时间）
    private static final Map<String, Long> PAGE_LAST_CLICK_MAP = new HashMap<>();
    // 主线程Handler（保证XToastUtils在主线程调用）
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * 安全转换Context为LifecycleOwner
     *
     * @param context 上下文
     * @return 转换后的LifecycleOwner，不支持则返回null
     */
    @Nullable
    private static LifecycleOwner safeConvertToLifecycleOwner(Context context) {
        if (context instanceof LifecycleOwner) {
            return (LifecycleOwner) context;
        }
        return null;
    }

    /**
     * 注册退出回调（默认提示语+默认间隔）
     *
     * @param dispatcher   Activity的OnBackPressedDispatcher
     * @param context      Activity上下文
     * @param exitRunnable 确认退出时的执行逻辑
     */
    public static void registerExitCallback(
            @NonNull OnBackPressedDispatcher dispatcher,
            @NonNull Context context,
            @NonNull Runnable exitRunnable) {
        registerExitCallback(dispatcher, context, "再按一次退出应用", DEFAULT_EXIT_INTERVAL, exitRunnable);
    }

    /**
     * 注册退出回调（自定义提示语+默认间隔）
     *
     * @param dispatcher   Activity的OnBackPressedDispatcher
     * @param context      Activity上下文
     * @param tipText      提示语
     * @param exitRunnable 确认退出时的执行逻辑
     */
    public static void registerExitCallback(
            @NonNull OnBackPressedDispatcher dispatcher,
            @NonNull Context context,
            @NonNull String tipText,
            @NonNull Runnable exitRunnable) {
        registerExitCallback(dispatcher, context, tipText, DEFAULT_EXIT_INTERVAL, exitRunnable);
    }

    /**
     * 注册退出回调（完全自定义）
     *
     * @param dispatcher   Activity的OnBackPressedDispatcher
     * @param context      Activity上下文
     * @param tipText      提示语
     * @param exitInterval 退出间隔（毫秒）
     * @param exitRunnable 确认退出时的执行逻辑
     */
    public static void registerExitCallback(
            @NonNull OnBackPressedDispatcher dispatcher,
            @NonNull Context context,
            @NonNull String tipText,
            long exitInterval,
            @NonNull Runnable exitRunnable) {
        // 防止空指针
        if (dispatcher == null || context == null || tipText == null || exitRunnable == null) {
            return;
        }

        String pageKey = context.getClass().getName();
        LifecycleOwner lifecycleOwner = safeConvertToLifecycleOwner(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                long lastClickTime = PAGE_LAST_CLICK_MAP.getOrDefault(pageKey, 0L);

                if (currentTime - lastClickTime > exitInterval) {
                    // 第一次点击：使用XToastUtils显示提示语（保证主线程调用）
                    showTip(tipText);
                    PAGE_LAST_CLICK_MAP.put(pageKey, currentTime);
                } else {
                    // 第二次点击：执行退出逻辑，禁用回调
                    PAGE_LAST_CLICK_MAP.remove(pageKey);
                    exitRunnable.run();
                    setEnabled(false);
                }
            }
        };

        // 绑定生命周期（支持则绑定，不支持则直接注册）
        if (lifecycleOwner != null) {
            dispatcher.addCallback(lifecycleOwner, callback);
        } else {
            dispatcher.addCallback(callback);
        }
    }

    /**
     * 清理页面状态（页面销毁时调用）
     *
     * @param context Activity上下文
     */
    public static void clearPageState(Context context) {
        if (context == null) return;
        String pageKey = context.getClass().getName();
        PAGE_LAST_CLICK_MAP.remove(pageKey);
    }

    /**
     * 使用XToastUtils显示提示（确保在主线程执行）
     *
     * @param text 提示文本
     */
    private static void showTip(String text) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 当前已是主线程，直接调用
            XToastUtils.info(text);
        } else {
            // 子线程，切换到主线程调用
            MAIN_HANDLER.post(() -> XToastUtils.info(text));
        }
    }
}