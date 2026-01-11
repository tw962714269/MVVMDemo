package com.cg.demo.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

/**
 * 状态栏/导航栏高度工具类（终极稳定版）
 * 核心改进：
 * 1. 移除所有Android 11+高风险反射（navigationBars字段/InsetsController方法）
 * 2. 仅保留“屏幕可视区域判断”（核心）+“Window标记判断”（基础）+“品牌适配”（兜底）
 * 3. 全版本无异常，适配所有机型（包括定制ROM）
 */
public class NavigationStatusBarUtil {

    // ==================== 核心常量（品牌适配） ====================
    private static final String BRAND_XIAOMI = "xiaomi";
    private static final String BRAND_HUAWEI = "huawei";
    private static final String BRAND_OPPO = "oppo";
    private static final String BRAND_VIVO = "vivo";

    // ==================== 状态栏高度（全版本兼容） ====================
    public static int getDefaultStatusBarHeight(Context context) {
        if (context == null) return 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    public static int getActualStatusBarHeight(Activity activity) {
        if (activity == null) return 0;
        if (isStatusBarHidden(activity.getWindow())) return 0;

        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int actualHeight = rect.top;
        return actualHeight > 0 ? actualHeight : getDefaultStatusBarHeight(activity);
    }

    private static boolean isStatusBarHidden(Window window) {
        if (window == null) return false;
        WindowManager.LayoutParams attrs = window.getAttributes();
        return (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0
                || (attrs.flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0;
    }

    // ==================== 导航栏高度（核心稳定版） ====================
    /**
     * 获取实际显示的导航栏高度（隐藏=0，显示=默认高度）
     * 核心逻辑：屏幕可视区域高度 ≈ 真实高度 → 导航栏隐藏
     */
    public static int getActualNavigationBarHeight(Activity activity) {
        if (activity == null) return 0;

        // 1. 无虚拟导航栏 → 返回0
        if (!hasVirtualNavigationBar(activity)) {
            return 0;
        }

        // 2. 导航栏已隐藏 → 返回0（仅保留最稳定的判断逻辑）
        if (isNavigationBarHiddenStable(activity)) {
            return 0;
        }

        // 3. 显示状态 → 返回默认高度
        return getDefaultNavigationBarHeight(activity);
    }

    /**
     * 获取导航栏默认高度（仅设备参数，不判断显示状态）
     */
    public static int getDefaultNavigationBarHeight(Context context) {
        if (context == null || !hasVirtualNavigationBar(context)) return 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier(
                isLandscape(context) ? "navigation_bar_width" : "navigation_bar_height",
                "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    // ==================== 核心：稳定的导航栏隐藏判断（移除所有高风险反射） ====================
    private static boolean isNavigationBarHiddenStable(Activity activity) {
        Window window = activity.getWindow();

        // 维度1：基础Window标记判断（全版本兼容，无异常）
        if (isNavigationBarHiddenByBasicFlags(window)) {
            return true;
        }

        // 维度2：屏幕可视区域判断（核心，最精准，全版本兼容）
        if (isNavigationBarHiddenByDisplayArea(activity)) {
            return true;
        }

        // 维度3：品牌适配（仅保留低风险反射，失败则跳过）
        if (isNavigationBarHiddenByBrandSafe(activity)) {
            return true;
        }

        return false;
    }

    /**
     * 维度1：基础Window标记判断（无任何反射，全版本稳定）
     */
    private static boolean isNavigationBarHiddenByBasicFlags(Window window) {
        if (window == null) return false;
        WindowManager.LayoutParams attrs = window.getAttributes();
        // 覆盖所有隐藏导航栏的基础标记
        return (attrs.flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0
                || (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0);
    }

    /**
     * 维度2：屏幕可视区域判断（核心，无API依赖，99%机型精准）
     * 逻辑：可视区域高度 = 屏幕真实高度 - 状态栏高度 → 导航栏隐藏
     */
    private static boolean isNavigationBarHiddenByDisplayArea(Activity activity) {
        try {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics realMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(realMetrics);
            // 真实屏幕高度（竖屏）/宽度（横屏）
            int realScreenSize = isLandscape(activity) ? realMetrics.widthPixels : realMetrics.heightPixels;

            // 获取当前页面可视区域（不含导航栏/状态栏）
            Rect visibleRect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleRect);
            int visibleHeight = visibleRect.bottom - visibleRect.top;

            // 获取状态栏高度（可视区域top即为状态栏高度）
            int statusBarHeight = visibleRect.top;
            // 真实可用高度 = 真实屏幕高度 - 状态栏高度
            int realUsableHeight = realScreenSize - statusBarHeight;

            // 可视高度 ≥ 真实可用高度 - 10px（容错）→ 导航栏隐藏
            return visibleHeight >= (realUsableHeight - 10);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 维度3：品牌适配（低风险反射，失败则返回false，不影响主逻辑）
     */
    private static boolean isNavigationBarHiddenByBrandSafe(Activity activity) {
        String brand = Build.BRAND.toLowerCase();
        Window window = activity.getWindow();
        try {
            switch (brand) {
                case BRAND_XIAOMI:
                    // MIUI：仅判断SYSTEM_UI_FLAG，不反射高风险字段
                    return (window.getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
                case BRAND_HUAWEI:
                    // EMUI：低风险反射（仅作兜底，失败则跳过）
                    return isEmuiNavigationBarHiddenSafe(activity);
                case BRAND_OPPO:
                case BRAND_VIVO:
                    // OPPO/VIVO：仅依赖基础标记，不反射
                    return isNavigationBarHiddenByBasicFlags(window);
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * EMUI导航栏判断（低风险反射，失败则返回false）
     */
    private static boolean isEmuiNavigationBarHiddenSafe(Activity activity) {
        try {
            Class<?> cls = Class.forName("com.huawei.android.view.HwDecorView");
            View decorView = activity.getWindow().getDecorView();
            // 先判断是否为HwDecorView实例，再调用方法
            if (cls.isInstance(decorView)) {
                return (boolean) cls.getMethod("isNavigationBarHide").invoke(decorView);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 辅助：判断是否有虚拟导航栏（全版本稳定） ====================
    private static boolean hasVirtualNavigationBar(Context context) {
        if (context == null) return false;

        // 有物理菜单键 → 无虚拟导航栏
        if (ViewConfiguration.get(context).hasPermanentMenuKey()) {
            return false;
        }

        // 系统资源判断（优先）
        Resources res = context.getResources();
        int resId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resId > 0) {
            return res.getBoolean(resId);
        }

        // 屏幕高度差值判断（兜底，无API依赖）
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics realMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(realMetrics);
            int realHeight = realMetrics.heightPixels;
            int realWidth = realMetrics.widthPixels;

            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            int displayHeight = metrics.heightPixels;
            int displayWidth = metrics.widthPixels;

            // 差值>20px → 有虚拟导航栏（容错，避免状态栏干扰）
            return (realHeight - displayHeight) > 20 || (realWidth - displayWidth) > 20;
        } catch (Exception e) {
            // 兜底：全面屏默认有虚拟导航栏
            return true;
        }
    }

    // ==================== 工具方法（全版本稳定） ====================
    private static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}