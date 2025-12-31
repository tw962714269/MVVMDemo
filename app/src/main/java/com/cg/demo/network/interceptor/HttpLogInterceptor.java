package com.cg.demo.network.interceptor;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpLogInterceptor implements Interceptor {
    private static final String TAG = "HttpLogInterceptor";

    //token刷新时间
    private static volatile long SESSION_KEY_REFRESH_TIME = 0;

    public HttpLogInterceptor() {
    }


    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //添加到责任链中
        Request request = chain.request();
//        logForRequest(request);
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return logForResponse(chain, response, request);
    }

    /**
     * 打印响应日志
     *
     * @param response
     * @return
     */
    private Response logForResponse(Chain chain, Response response, Request request) {
//        Log.e(TAG, "\n********响应开始********");
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
//        Log.d(TAG, "url:" + clone.request().url());
//        Log.d(TAG, "code:" + clone.code());
//        if (!TextUtils.isEmpty(clone.message())) {
//            Log.d(TAG, "message:" + clone.message());
//        }

        Headers responseHeaders = response.request().headers();
//        Log.e(TAG, "\n请求头 ----> \n");
//        Log.d(TAG, responseHeaders.toString());

        ResponseBody body = clone.body();
        if (body != null) {
            MediaType mediaType = body.contentType();
            if (mediaType != null) {
                if (isText(mediaType)) {
                    String resp = null;
                    try {
                        resp = body.string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String result = resp;
                    int segmentSize = 3 * 1024;
                    Log.e(TAG, "\n响应结果: ---->\n");
                    if (result.length() <= segmentSize) {// 长度小于等于限制直接打印
                        Log.d(TAG, result);
                    } else {

                        while (result.length() > segmentSize) {// 循环分段打印日志
                            String logContent = result.substring(0, segmentSize);
                            result = result.replace(logContent, "");
                            Log.d(TAG, logContent);
                        }
                        Log.d(TAG, result);
                    }

//                    Log.e(TAG, "********响应结束********\n");
                    body = ResponseBody.create(mediaType, resp);
                    return response.newBuilder().body(body).build();
                } else {
                    Log.e(TAG, "响应内容 : " + "发生错误-非文本类型");
                }
            }
        }
        Log.e(TAG, "********响应结束********");
        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && (mediaType.type().equals("text") || mediaType.type().equals("application/json"))) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json")
                    || mediaType.subtype().equals("xml")
                    || mediaType.subtype().equals("html")
                    || mediaType.subtype().equals("webviewhtml")
                    || mediaType.subtype().equals("x-www-form-urlencoded")) {
                return true;
            }
        }
        return false;
    }
//
//    /**
//     * 打印请求日志
//     *
//     * @param request
//     */
//    private void logForRequest(Request request) {
//        String url = request.url().toString();
//        Log.e(TAG, "\n========请求开始=======");
//        Log.d(TAG, "请求方式 : " + request.method());
//        Log.d(TAG, "url : " + url);
//        RequestBody requestBody = request.body();
//        if (requestBody != null) {
//            MediaType mediaType = requestBody.contentType();
//            if (mediaType != null) {
//                Log.d(TAG, "请求内容类别 : " + mediaType.toString());
//                if (isText(mediaType)) {
//                    Log.d(TAG, "请求内容 : " + bodyToString(request));
//                } else {
//                    Log.d(TAG, "请求内容 : " + " 无法识别。");
//                }
//            }
//        }
//        Log.e(TAG, "========请求结束=======\n");
//    }
//
//    private String bodyToString11(Request request) {
//        Request req = request.newBuilder().build();
//        String urlSub = null;
//        Buffer buffer = new Buffer();
//        try {
//            req.body().writeTo(buffer);
//            String message = buffer.readUtf8();
//            urlSub = URLDecoder.decode(message, "utf-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "在解析请求内容时候发生了异常-非字符串";
//        }
//        return urlSub;
//    }


    // TODO: 2025/8/20  token
//    //处理token失效问题
//    private Response handleTokenInvalid(Chain chain, Request request) throws IOException {
//        HashMap<String, String> mapParam = new HashMap<>();
//        RequestBody body = request.body();
//        if (body instanceof FormBody) {
//            FormBody formBody = (FormBody) body;
//            for (int i = 0; i < formBody.size(); i++) {
//                mapParam.put(formBody.name(i), formBody.value(i));  //2、保存参数
//            }
//        }
//        //同步刷新token
//        String requestTime = String.valueOf(System.currentTimeMillis());  //3、发请求前需要add("request_time",System.currentTimeMillis())
//        boolean success = refreshToken(requestTime);
//        Request newRequest;
//        LogUtils.d("------" + success);
//
//        if (success) { //刷新成功，重新签名
//            mapParam.put("Authorization", SPFullUtils.getInstance().getUserDTO().getToken()); //4、拿到最新的token,重新发起请求
//            newRequest = RxHttp.postForm(request.url().toString())
//                    .addAll(mapParam) //添加参数
//                    .buildRequest();
//        } else {
//            newRequest = request;
//        }
//        return chain.proceed(newRequest);
//    }
//
//    //刷新token
//    private boolean refreshToken(Object value) {
//        long requestTime = 10;
////        try {
////            requestTime = Integer.parseInt(value.toString());
////        } catch (Exception ignore) {
////        }
//        //请求时间小于token刷新时间，说明token已经刷新，则无需再次刷新
//        if (requestTime <= SESSION_KEY_REFRESH_TIME) return true;
//        synchronized (this) {
//            //再次判断是否已经刷新
//            if (requestTime <= SESSION_KEY_REFRESH_TIME) return true;
//            try {
//                //获取到最新的token，这里需要同步请求token,千万不能异步  5、根据自己的业务修改
//                String token = RxHttp.postBody("/login").setBody(SPFullUtils.getInstance().getUserDTO())
//                        .executeString();
////                SESSION_KEY_REFRESH_TIME = System.currentTimeMillis() / 1000;
//                UserMsgDTO userDTO = new UserMsgDTO();
//                UserMsgDTO user = SPFullUtils.getInstance().getUserDTO();
//                userDTO.setToken(JsonUtils.getString(token, "token"));
//                //保存最新的token
//                SPFullUtils.getInstance().putUserToken(userDTO.getToken());
//                //更新最新的用户信息
//                SPFullUtils.getInstance().putUserDTO(userDTO);
//                return true;
//            } catch (IOException e) {
//                return false;
//            }
//        }
//    }
}