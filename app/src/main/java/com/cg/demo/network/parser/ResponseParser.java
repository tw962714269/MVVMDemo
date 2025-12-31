package com.cg.demo.network.parser;

import com.cg.demo.network.base_api.entity.Response;

import java.io.IOException;
import java.lang.reflect.Type;

import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.TypeParser;
import rxhttp.wrapper.utils.Converter;

@Parser(name = "Response", wrappers = {Response.class})
public class ResponseParser<T> extends TypeParser<T> {

    //该构造方法是必须的
    protected ResponseParser() {
        super();
    }

    //如果依赖了RxJava，该构造方法也是必须的
    public ResponseParser(Type type) {
        super(type);
    }

    @Override
    public T onParse(okhttp3.Response response) throws IOException {
        //将okhttp3.Response转换为自定义的Response对象
        Response<T> data = Converter.convertTo(response, Response.class, types);  //这里的types就是自定义Response<T>里面的泛型类型
        T t = data.getData(); //获取data字段

        if (data.getMeta().getCode() != 0 || t == null) {//这里假设code不等于200，代表数据不正确，抛出异常
            throw new ParseException(String.valueOf(data.getMeta().getCode()), data.getMeta().getMessage(), response);
        }
        return t;
    }
}
