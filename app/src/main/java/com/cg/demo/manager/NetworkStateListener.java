package com.cg.demo.manager;

public interface NetworkStateListener {
    /**
     * 网络状态变化回调
     * @param isAvailable 网络是否可用
     * @param networkType 网络类型
     */
    void onNetworkStateChanged(boolean isAvailable, NetworkType networkType);
}