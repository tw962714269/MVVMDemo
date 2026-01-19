package com.cg.demo.network.base_api.module;

import com.cg.demo.bean.LoginBean;
import com.cg.demo.bean.ReleaseAppVersionDTO;
import com.cg.demo.network.base_api.entity.Response;

import java.util.List;

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
     * App版本更新
     */
    public ObservableCall<List<ReleaseAppVersionDTO>> getReleaseAppVersion() {
        return RxHttp.get("/auth/version/open/v1/getReleaseAppVersion").add("typeName", 1).toObservableResponseList(ReleaseAppVersionDTO.class);
    }

    /**
     * 登录
     */
    public ObservableCall<Response<LoginBean.LoginVO>> login(LoginBean.LoginDTO loginDTO) {
        return RxHttp.postBody("/auth/open/v1/login").setBody(loginDTO).toObservableData(LoginBean.LoginVO.class);
    }
}
