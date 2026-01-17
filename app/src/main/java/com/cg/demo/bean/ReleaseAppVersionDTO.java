package com.cg.demo.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ReleaseAppVersionDTO {

    @JsonProperty("notes")
    private String notes;
    @JsonProperty("createTime")
    private Long createTime;
    @JsonProperty("versionNum")
    private String versionNum;
    @JsonProperty("typeName")
    private Integer typeName;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("upStatus")
    private Integer upStatus;
    @JsonProperty("packageName")
    private String packageName;
    @JsonProperty("versionName")
    private String versionName;
    @JsonProperty("downAddress")
    private String downAddress;
    @JsonProperty("status")
    private Integer status;

    private Long fileSize = 125552153L;
}
