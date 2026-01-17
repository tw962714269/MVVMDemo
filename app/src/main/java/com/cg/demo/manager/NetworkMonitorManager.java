package com.cg.demo.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局网络监听
 */
public class NetworkMonitorManager {
    // 单例实例
    private static volatile NetworkMonitorManager sInstance;
    // 上下文（使用Application Context避免内存泄漏）
    private Context mContext;
    // 网络监听回调列表（线程安全）
    private final List<NetworkStateListener> mListeners = new CopyOnWriteArrayList<>();
    // 网络连接管理器
    private ConnectivityManager mConnectivityManager;
    // 网络回调
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    // 当前网络状态
    private NetworkType mCurrentNetworkType = NetworkType.NONE;
    // 是否已初始化
    private boolean isInitialized = false;

    // 私有构造方法
    private NetworkMonitorManager() {
    }

    /**
     * 获取单例实例
     */
    public static NetworkMonitorManager getInstance() {
        if (sInstance == null) {
            synchronized (NetworkMonitorManager.class) {
                if (sInstance == null) {
                    sInstance = new NetworkMonitorManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化网络监听（建议在Application中调用）
     *
     * @param context Application Context
     */
    public void init(@NonNull Context context) {
        if (isInitialized) {
            return;
        }

        // 使用Application Context防止内存泄漏
        mContext = context.getApplicationContext();
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // 初始化时先获取一次当前网络状态
        mCurrentNetworkType = getCurrentNetworkType();

        registerNetworkCallback();

        isInitialized = true;
    }

    /**
     * NetworkCallback
     */
    private void registerNetworkCallback() {
        if (mNetworkCallback == null) {
            mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    updateNetworkState();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    updateNetworkState();
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    updateNetworkState();
                }
            };

            // 注册网络回调
            try {
                mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新网络状态并通知所有监听者
     */
    private void updateNetworkState() {
        NetworkType newType = getCurrentNetworkType();
        boolean isAvailable = newType != NetworkType.NONE;

        // 只有状态发生变化时才通知
        if (newType != mCurrentNetworkType) {
            mCurrentNetworkType = newType;
            notifyAllListeners(isAvailable, newType);
        }
    }

    /**
     * 获取当前网络类型
     */
    public NetworkType getCurrentNetworkType() {
        if (mConnectivityManager == null) {
            return NetworkType.NONE;
        }

        // 高版本API（23+）
        Network network = mConnectivityManager.getActiveNetwork();
        if (network == null) {
            return NetworkType.NONE;
        }

        NetworkCapabilities capabilities = mConnectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return NetworkType.NONE;
        }

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.WIFI;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.MOBILE;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkType.OTHER;
        } else {
            return NetworkType.NONE;
        }
    }

    /**
     * 通知所有监听者网络状态变化
     */
    private void notifyAllListeners(boolean isAvailable, NetworkType networkType) {
        for (NetworkStateListener listener : mListeners) {
            try {
                listener.onNetworkStateChanged(isAvailable, networkType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加网络状态监听
     */
    public void addNetworkStateListener(NetworkStateListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
            // 立即回调当前网络状态
            listener.onNetworkStateChanged(
                    mCurrentNetworkType != NetworkType.NONE,
                    mCurrentNetworkType);
        }
    }

    /**
     * 移除网络状态监听
     */
    public void removeNetworkStateListener(NetworkStateListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    /**
     * 判断网络是否可用
     */
    public boolean isNetworkAvailable() {
        return mCurrentNetworkType != NetworkType.NONE;
    }

    /**
     * 释放资源（建议在Application退出时调用）
     */
    public void release() {
        if (mContext == null) {
            return;
        }

        // 清空监听列表
        mListeners.clear();

        // 注销回调
        if (mNetworkCallback != null) {
            try {
                mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mNetworkCallback = null;
        }

        // 重置状态
        mContext = null;
        mConnectivityManager = null;
        mCurrentNetworkType = NetworkType.NONE;
        isInitialized = false;
    }
}