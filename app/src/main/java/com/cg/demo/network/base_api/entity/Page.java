package com.cg.demo.network.base_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Page<T> {


    @JsonProperty("current")
    private Integer current;
    @JsonProperty("pages")
    private Integer pages;
    @JsonProperty("records")
    private List<T> records;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("total")
    private Integer total;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }


}
