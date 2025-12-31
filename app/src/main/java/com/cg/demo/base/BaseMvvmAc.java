package com.cg.demo.base;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseMvvmAc<V extends ViewDataBinding, VM extends BaseViewModel> extends BaseAc {

    protected VM viewModel;
    protected V binding;


    @Override
    public void afterOnCreate(Bundle savedInstanceState) {
        super.afterOnCreate(savedInstanceState);
        initViewDataBinding(savedInstanceState);
        getLifecycle().addObserver(viewModel);

    }

    private void initViewDataBinding(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, initContentView(savedInstanceState));

        if (viewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[1];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = BaseViewModel.class;
            }
            viewModel = (VM) new ViewModelProvider(this,
                    (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                    .get(modelClass);
        }
        if (initVariableId() > 0) {
            binding.setVariable(initVariableId(), viewModel);
        }

        viewModel.uiChangeLiveData().onBackPressedEvent().observe(this, o -> {
            onBackPressed();
        });
    }


    /**
     * 初始化根布局
     *
     * @param savedInstanceState
     * @return 布局layout的id
     */
    protected abstract int initContentView(Bundle savedInstanceState);

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    protected abstract int initVariableId();
}
