package com.cg.demo.network.interceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * @author:lee
 * @Date:2025/12/30 9:06
 * @Describe:OkHttp 防重复请求拦截器
 * 重复请求时，取消前一次未完成请求，只保留最新请求
 */
public class CancelRepeatRequestInterceptor implements Interceptor {
    // 存储正在执行的请求：KEY=请求标识，VALUE=请求对象(用于取消)
    private final ConcurrentHashMap<String, Call> requestMap = new ConcurrentHashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String requestKey = generateRequestKey(request);

        // 判断是否有重复请求在执行，有则取消前一次
        if (requestMap.containsKey(requestKey)) {
            Call oldCall = requestMap.get(requestKey);
            if (oldCall != null && !oldCall.isCanceled()) {
                oldCall.cancel();
            }
        }

        // 存入当前请求对象
        requestMap.put(requestKey, chain.call());

        try {
            Response response = chain.proceed(request);
            return response;
        } finally {
            // 请求完成后移除标记
            requestMap.remove(requestKey);
        }
    }

    // 复用上面的【生成唯一请求标识】方法，一模一样
    private String generateRequestKey(Request request) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(request.url()).append(request.method());
        if ("POST".equals(request.method()) || "PUT".equals(request.method())) {
            RequestBody body = request.body();
            if (body != null) {
                Buffer buffer = new Buffer();
                body.writeTo(buffer);
                sb.append(buffer.readUtf8());
            }
        }
        return sb.toString();
    }
}