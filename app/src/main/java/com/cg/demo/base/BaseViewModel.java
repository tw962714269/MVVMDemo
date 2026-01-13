package com.cg.demo.base;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.cg.demo.livedata.SingleLiveEvent;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import io.reactivex.functions.Consumer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


public class BaseViewModel<M extends BaseModel> extends AndroidViewModel implements IBaseViewModel, Consumer<Disposable> {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    protected M model;

    // 全局请求状态分发（SingleLiveEvent避免粘性事件）
    protected final SingleLiveEvent<BaseRequestState<?>> mRequestStateEvent = new SingleLiveEvent<>();
    // 主线程Handler，确保所有回调在主线程执行
    protected final Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 获取请求状态事件（给View层订阅）
     */
    public SingleLiveEvent<BaseRequestState<?>> getRequestStateEvent() {
        return mRequestStateEvent;
    }

    /**
     * 分发请求状态（确保主线程）
     */
    protected void postRequestState(BaseRequestState<?> state) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mRequestStateEvent.setValue(state);
        } else {
            mMainHandler.post(() -> mRequestStateEvent.setValue(state));
        }
    }

    /**
     * 取消所有请求
     */
    public void cancelAllRequests() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
            postRequestState(BaseRequestState.cancelled());
        }
    }


    public BaseViewModel(@NonNull Application application) {
        super(application);
        model = createModel();
    }

    private M createModel() {
        try {
            Type superClass = getClass().getGenericSuperclass();
            Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            Class<?> clazz = getRawType(type);
            return (M) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // type不能直接实例化对象，通过type获取class的类型，然后实例化对象
    private Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            return (Class) rawType;
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        } else if (type instanceof TypeVariable) {
            return Object.class;
        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    protected void addDisposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed())
            mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelAllRequests();
        mMainHandler.removeCallbacksAndMessages(null);

        if (model != null) {
            model.onCleared();
        }
    }

    @Override
    public void onAny(LifecycleOwner owner, Lifecycle.Event event) {
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }


    @Override
    public void accept(Disposable disposable) throws Exception {
        addDisposable(disposable);
    }


    // ========== 通用请求生命周期封装（业务层直接调用） ==========
    /**
     * 执行网络请求的通用封装
     * @param requestAction 具体的请求逻辑（由业务层实现）
     * @param <T> 请求成功数据类型
     */
    protected <T> void executeRequest(RequestAction<T> requestAction) {
        // 1. 分发加载中状态
        postRequestState(BaseRequestState.loading());

        try {
            // 2. 执行具体请求（业务层实现）
            requestAction.execute(disposable -> {
                // 请求开始：添加Disposable，分发加载中（已提前分发，可扩展额外逻辑）
                addDisposable(disposable);
            }, data -> {
                // 3. 请求成功：分发成功状态
                postRequestState(BaseRequestState.success(data));
                // 4. 分发结束状态
                postRequestState(BaseRequestState.completed());
            }, throwable -> {
                // 3. 请求失败：分发失败状态
                postRequestState(BaseRequestState.error(throwable));
                // 4. 分发结束状态
                postRequestState(BaseRequestState.completed());
            });
        } catch (Exception e) {
            postRequestState(BaseRequestState.error(e));
            postRequestState(BaseRequestState.completed());
        }
    }

    /**
     * 请求行为接口（业务层实现具体请求逻辑）
     */
    public interface RequestAction<T> {
        /**
         * 执行具体请求
         * @param onStart 请求开始回调（传递Disposable）
         * @param onSuccess 请求成功回调
         * @param onError 请求失败回调
         */
        void execute(OnRequestStart onStart, OnRequestSuccess<T> onSuccess, OnRequestError onError);
    }

    // 请求开始回调
    public interface OnRequestStart {
        void onStart(Disposable disposable);
    }

    // 请求成功回调
    public interface OnRequestSuccess<T> {
        void onSuccess(T data);
    }

    // 请求失败回调
    public interface OnRequestError {
        void onError(Throwable throwable);
    }
}
