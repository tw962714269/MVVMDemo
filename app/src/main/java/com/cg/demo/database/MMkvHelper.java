package com.cg.demo.database;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.cg.demo.app.BaseApp;
import com.cg.demo.constant.C;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FileName: MMkvHelper
 * Created by zlx on 2020/9/21 14:40
 * Email: 1170762202@qq.com
 * Description:
 */
public class MMkvHelper {
    private final Gson mGson = new Gson();

    private static MMKV mmkv;

    private MMkvHelper() {
        String rootDir = MMKV.initialize(BaseApp.getInstance());

        mmkv = MMKV.defaultMMKV();
    }

    public static MMkvHelper getInstance() {
        return MMkvHolder.INSTANCE;
    }

    private static class MMkvHolder {
        private static final MMkvHelper INSTANCE = new MMkvHelper();
    }

    public void saveLanguage(Locale locale) {
        mmkv.encode(C.LANGUAGE, JSON.toJSONString(locale));
    }

    public Locale getLanguage() {
        String s = mmkv.decodeString(C.LANGUAGE);
        if (TextUtils.isEmpty(s)){
            return null;
        }
        try {
            Locale locale = JSON.parseObject(s, Locale.class);
            return locale;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public <T> void saveProjectTabs(List<T> dataList) {
        saveList(C.PROJECT_TABS, dataList);
    }

    public <T> List<T> getProjectTabs(Class<T> cls) {
        return getTList(C.PROJECT_TABS, cls);
    }

    /**
     * 保存list
     */
    private <T> void saveList(String tag, List<T> dataList) {
        if (null == dataList || dataList.size() <= 0)
            return;
        //转换成json数据，再保存
        String strJson = mGson.toJson(dataList);
        mmkv.encode(tag, strJson);
    }

    private <T> List<T> getTList(String tag, Class<T> cls) {
        List<T> dataList = new ArrayList<>();
        String strJson = mmkv.decodeString(tag, null);
        if (null == strJson) {
            return dataList;
        }
        try {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                dataList.add(gson.fromJson(jsonElement, cls));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }
}
