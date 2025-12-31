package com.cg.demo.network.base_api.module;

import com.cg.demo.bean.LoginBean;
import com.cg.demo.network.base_api.entity.Response;

import rxhttp.ObservableCall;
import rxhttp.RxHttp;

public class BaseApi {

    private static BaseApi instance;

    public static BaseApi getInstance() {
        if (instance == null) {
            synchronized (BaseApi.class) {
                instance = new BaseApi();
            }
        }
        return instance;
    }

    private BaseApi() {
    }

    /**
     * 登录
     */
    public ObservableCall<Response<LoginBean.LoginVO>> login(LoginBean.LoginDTO loginDTO) {
        return RxHttp.postBody("/auth/open/v1/login").setBody(loginDTO).toObservableData(LoginBean.LoginVO.class);
    }
}
