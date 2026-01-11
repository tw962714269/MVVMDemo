package com.cg.demo.base;

/**
 * 全局请求状态封装类（Base层通用）
 * 覆盖：加载中、成功、失败、取消、结束 所有状态
 * @param <T> 成功返回的数据类型
 */
public class BaseRequestState<T> {
    // 状态类型
    public enum State {
        LOADING,   // 加载中
        SUCCESS,   // 成功
        ERROR,     // 失败
        CANCELLED, // 取消
        COMPLETED  // 结束（通用）
    }

    private final State state;
    private T data;         // 成功数据
    private Throwable error;// 失败异常

    // 私有构造，通过静态方法创建
    private BaseRequestState(State state) {
        this.state = state;
    }

    // 加载中
    public static <T> BaseRequestState<T> loading() {
        return new BaseRequestState<>(State.LOADING);
    }

    // 成功
    public static <T> BaseRequestState<T> success(T data) {
        BaseRequestState<T> state = new BaseRequestState<>(State.SUCCESS);
        state.data = data;
        return state;
    }

    // 失败
    public static <T> BaseRequestState<T> error(Throwable error) {
        BaseRequestState<T> state = new BaseRequestState<>(State.ERROR);
        state.error = error;
        return state;
    }

    // 取消
    public static <T> BaseRequestState<T> cancelled() {
        return new BaseRequestState<>(State.CANCELLED);
    }

    // 结束
    public static <T> BaseRequestState<T> completed() {
        return new BaseRequestState<>(State.COMPLETED);
    }

    // Getter
    public State getState() { return state; }
    public T getData() { return data; }
    public Throwable getError() { return error; }
}