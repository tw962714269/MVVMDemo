package com.cg.demo.base;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.LogUtils;

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

        subscribeRequestState();
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

    /**
     * 统一订阅请求状态（Base层处理通用逻辑，暴露抽象方法给业务层）
     */
    private void subscribeRequestState() {
        viewModel.getRequestStateEvent().observe(this, o -> {
            if (o == null) return;
            BaseRequestState<?> state = (BaseRequestState<?>) o;
            switch (state.getState()) {
                case LOADING:
                    // 通用加载中逻辑：显示加载弹窗
                    showLoadingDialog();
                    LogUtils.v("通用加载中逻辑：显示加载弹窗");
                    break;
                case SUCCESS:
                    // 通用成功逻辑：业务层处理具体数据
                    onRequestSuccess(state.getData());
                    LogUtils.v("通用成功逻辑：业务层处理具体数据");
                    break;
                case ERROR:
                    // 通用失败逻辑：显示默认错误提示，业务层可扩展
                    showDefaultErrorTip(state.getError());
                    onRequestFailed(state.getError());
                    LogUtils.v("通用失败逻辑：显示默认错误提示，业务层可扩展");
                    break;
                case CANCELLED:
                    // 通用取消逻辑：关闭加载弹窗，提示请求取消
                    dismissLoadingDialog();
                    showRequestCancelledTip();
                    onRequestCancelled();
                    LogUtils.v("通用取消逻辑：关闭加载弹窗，提示请求取消");
                    break;
                case COMPLETED:
                    // 通用结束逻辑：关闭加载弹窗，可做埋点、统计等
                    dismissLoadingDialog();
                    onRequestCompleted();
                    LogUtils.v("通用结束逻辑：关闭加载弹窗，可做埋点、统计等");
                    break;
            }
        });
    }

    /**
     * 页面销毁时，取消所有请求
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            viewModel.cancelAllRequests();
        }
    }
}
