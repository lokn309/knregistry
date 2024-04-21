package cn.lokn.knregistry.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @description: OKHttp invoker
 * @author: lokn
 * @date: 2024/03/23 00:10
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public String post(String reqeustString, String url) {
        log.debug(" ===>>> post url = {}, reqJson = {}", url, reqeustString);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqeustString, JSON_TYPE))
                .build();

        try {
            final String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===>>> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug(" ===>>> get url = {}", url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            final String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===> respJson = {}", respJson);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
