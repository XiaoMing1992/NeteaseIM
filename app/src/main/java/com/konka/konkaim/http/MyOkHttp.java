package com.konka.konkaim.http;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by HP on 2018-5-7.
 */

public class MyOkHttp {

    private static final long DEFAULT_TIME_OUT = 5;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient getOkHttpClient() {
        // 创建 OKHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//连接超时时间
        builder.writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//写操作 超时时间
        builder.readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//读操作超时时间

/*        // 添加公共参数拦截器
        BasicParamsInterceptor basicParamsInterceptor = new BasicParamsInterceptor.Builder()
                .addHeaderParam("userName", "")//添加公共参数
                .addHeaderParam("device", "")
                .build();

        builder.addInterceptor(basicParamsInterceptor);*/

        return builder.build();
    }

    /**
     * 通过post方式来提交json数据
     * @param url
     * @param data
     * @return
     */
    public static String post(String url, Map<String, String>data) {
        //RequestBody body = RequestBody.create(JSON, json);
        FormBody.Builder builder = new FormBody.Builder();

        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.out.println("key=" + key + " value=" + value);
            builder.add(key, value);
        }

/*        RequestBody formBody = new FormBody.Builder()
                .add("mobile", "13631257723")
                .build();*/
        RequestBody formBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

/*        final String res = null;
        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().toString();
            }
        });*/

        try {
            Response response = getOkHttpClient().newCall(request).execute();
            System.out.println("code="+response.code());

            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过get方式来提交json数据
     * @param url
     * @return
     */
    public static String get(String url) {
         Request request = new Request.Builder().url(url).build();
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            System.out.println("code="+response.code());

            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
