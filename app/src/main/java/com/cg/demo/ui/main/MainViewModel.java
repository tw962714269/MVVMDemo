package com.cg.demo.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;

import com.cg.demo.base.BaseViewModel;

/**
 * @author:lee
 * @Date:2025/12/30 17:19
 * @Describe:
 */
public class MainViewModel extends BaseViewModel<MainModel> {
    public MainViewModel(@NonNull Application application) {
        super(application);
    }
}
