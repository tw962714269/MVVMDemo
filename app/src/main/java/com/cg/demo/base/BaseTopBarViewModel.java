package com.cg.demo.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

public class BaseTopBarViewModel<M extends BaseModel> extends BaseViewModel<M> {
    /**
     * 兼容databinding，去泛型化
     */
    public BaseTopBarViewModel toolbarViewModel;
    public ObservableField<String> titleText = new ObservableField<>("999");

    public BaseTopBarViewModel(@NonNull Application application) {
        super(application);
        toolbarViewModel = this;
    }

    public void setTitleText(String title) {
        titleText.set(title);
    }
}
