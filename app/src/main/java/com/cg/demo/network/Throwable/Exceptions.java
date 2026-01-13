package com.cg.demo.network.Throwable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.cg.demo.network.Tip;
import com.cg.demo.utils.ApplicationUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.TimeoutCancellationException;
import rxhttp.wrapper.exception.HttpStatusCodeException;
import rxhttp.wrapper.exception.ParseException;

public class Exceptions extends Throwable {
    @NotNull
    public static final String getErrorMsg(@NotNull Throwable $this$errorMsg) {
        Intrinsics.checkNotNullParameter($this$errorMsg, "$this$errorMsg");
        String var1;
        LogUtils.d(new Gson().toJson($this$errorMsg));
        LogUtils.d(new Gson().toJson($this$errorMsg));

        if ("888".equals(JsonUtils.getString(new Gson().toJson($this$errorMsg), "errorCode"))) {
            Tip.logout();
            return "登录过期。请重新登录";
        }
        if ($this$errorMsg instanceof UnknownHostException) {
            Intrinsics.checkNotNullExpressionValue(ApplicationUtils.getApp(), "AppHolder.getInstance()");
            var1 = !isNetworkConnected(ApplicationUtils.getApp()) ? "当前无网络，请检查你的网络设置" : "网络连接不可用，请稍后重试！";
        } else {
            if (!($this$errorMsg instanceof SocketTimeoutException) && !($this$errorMsg instanceof TimeoutException) && !($this$errorMsg instanceof TimeoutCancellationException)) {
                if ($this$errorMsg instanceof ConnectException) {
                    var1 = "网络不给力，请稍候重试！";
                } else if ($this$errorMsg instanceof HttpStatusCodeException) {
                    var1 = "Http状态码异常 " + $this$errorMsg.getMessage();
                } else if ($this$errorMsg instanceof JsonSyntaxException) {
                    var1 = "数据解析失败,请检查数据是否正确";
                } else if ($this$errorMsg instanceof ParseException) {
                    var1 = $this$errorMsg.getMessage();
                    if (var1 == null) {
                        var1 = ((ParseException) $this$errorMsg).getErrorCode();
                    }
                } else {
                    var1 = $this$errorMsg.getMessage();
                    if (var1 == null) {
                        var1 = $this$errorMsg.toString();
                    }
                }
            } else {
                var1 = "网络开小差了,请稍后再试";
            }

            Intrinsics.checkNotNullExpressionValue(var1, "if (\n            this is…this.toString()\n        }");
        }

        return var1;
    }

    private static boolean isNetworkConnected(Context context) {
        Object var10000 = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (var10000 == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.net.ConnectivityManager");
        } else {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) var10000;
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null ? mNetworkInfo.isAvailable() : false;
        }
    }
}
