package com.cg.demo.network.base_api.entity;

public class Meta {
    private int code;
    private String message;
    private Long systemTime;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(Long systemTime) {
        this.systemTime = systemTime;
    }
}
