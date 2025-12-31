package com.cg.demo.bean;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author:lee
 * @Date:2025/12/30 17:33
 * @Describe:登录实体类
 */
public class LoginBean {

    //登录请求参数
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LoginDTO {
        //手机号
        public String loginName;
        //验证码/密码
        public String loginCode;
        //登录类型（登录方式：0：游客登录；1：一键登录；2：密码登录；3：验证码登录, 4为多身份登录）
        public Integer loginType;
    }

    //登录响应结果
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LoginVO {
        @SerializedName("userId")
        private String userId;
        @SerializedName("token")
        private String token;
        @SerializedName("appToken")
        private String appToken;
        @SerializedName("secretSign")
        private Object secretSign;
        @SerializedName("hasPsw")
        private Boolean hasPsw;
        @SerializedName("hasTourist")
        private Boolean hasTourist;
        @SerializedName("hasNewUser")
        private Boolean hasNewUser;
        @SerializedName("isDefaultPwd")
        private Boolean isDefaultPwd;
        @SerializedName("isWeChatLogin")
        private Boolean isWeChatLogin;
        @SerializedName("isAppleLogin")
        private Boolean isAppleLogin;
        @SerializedName("isGrayPub")
        private Object isGrayPub;
        @SerializedName("isCall")
        private Object isCall;
        @SerializedName("isMessage")
        private Object isMessage;
        @SerializedName("userName")
        private Object userName;
        @SerializedName("phone")
        private Object phone;
        @SerializedName("sidList")
        private Object sidList;
    }
}
