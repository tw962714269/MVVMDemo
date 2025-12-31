package com.cg.demo.network.base_api.entity;

import java.util.List;

public class ResponseListNest<T> {


    private Meta meta;
    private List<List<T>> data;





    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<List<T>> getData() {
        return data;
    }

    public void setData(List<List<T>> data) {
        this.data = data;
    }
}
