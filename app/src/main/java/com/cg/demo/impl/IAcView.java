package com.cg.demo.impl;

import android.os.Bundle;

/**
 * Created by zlx on 2020/9/22 13:58
 * Email: 1170762202@qq.com
 * Description:
 */
public interface IAcView {
    void initViews();
    void initEvents();
    void beforeOnCreate(int requestedOrientation);
    void afterOnCreate(Bundle savedInstanceState);
    void initImmersionBar(boolean fullScreen);
}
