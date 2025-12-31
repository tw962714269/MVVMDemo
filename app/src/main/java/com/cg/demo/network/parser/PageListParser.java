package com.cg.demo.network.parser;


import com.cg.demo.network.base_api.entity.PageList;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.TypeParser;
import rxhttp.wrapper.utils.Converter;

@Parser(name = "PageList", wrappers = {PageList.class})
public class PageListParser<T> extends TypeParser<PageList<T>> {

    //该构造方法是必须的
    protected PageListParser() { super(); }
    //如果依赖了RxJava，该构造方法也是必须的
    public PageListParser(Type type) { super(type); }

    @Override
    public PageList<T> onParse(Response response) throws IOException {
        //将okhttp3.Response转换为自定义的Response对象
        PageList<T> data = Converter.convertTo(response, PageList.class, types);  //这里的types就是自定义Response<T>里面的泛型类型
//        T t = (T) data.getRows(); //获取data字段
        if (data.getMeta().getCode() != 0) {//这里假设code不等于200，代表数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getMeta().getCode()), data.getMeta().getMessage(), response);
        }
        return data;


//        Response<T> data = Converter.convertTo(response, Response.class, types);
//        T t = data.getData(); //获取data字段
//        LogUtils.d(new Gson().toJson(data)+"-------"+new Gson().toJson(t));
//
//        if (t == null && types[0] == String.class) {
//            /*
//             * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
//             * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
//             * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
//             */
//            t = (T) data.getMsg();
//        }
//        if (data.getMeta().getCode() != 0 || t == null) {//code不等于0，说明数据不正确，抛出异常
//            throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
//        }
//        return t;
    }
}
