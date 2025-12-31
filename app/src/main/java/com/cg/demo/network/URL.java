package com.cg.demo.network;

import rxhttp.wrapper.annotation.DefaultDomain;
import rxhttp.wrapper.annotation.Domain;

public class URL {
    @DefaultDomain //设置为默认域名
//    public static String BASE_URL = "http://116.205.121.142/";
    public static String BASE_URL = "https://cgpt.fenjiu.com.cn/";

    //上传文件
    @Domain(name = "Upload")
    public static String UPLOAD = "http://116.205.121.142/";
}
