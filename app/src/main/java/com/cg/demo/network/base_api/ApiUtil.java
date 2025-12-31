package com.cg.demo.network.base_api;

import com.cg.demo.network.base_api.module.BaseApi;

public class ApiUtil {

    public static BaseApi getApi() {
        return BaseApi.getInstance();
    }

}
