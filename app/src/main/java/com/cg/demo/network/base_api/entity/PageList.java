package com.cg.demo.network.base_api.entity;


/**
 * 列表数据
 */
public class PageList<T> {

    private Meta meta;
    private Page<T> data;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Page<T> getData() {
        return data;
    }

    public void setData(Page<T> data) {
        this.data = data;
    }
}
