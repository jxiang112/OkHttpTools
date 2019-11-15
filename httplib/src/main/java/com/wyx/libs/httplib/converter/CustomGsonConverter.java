package com.wyx.libs.httplib.converter;

import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


/**
 * 网络请求体、响应JSON转换器
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 13:56
 * @since: 1.0.0
 */
public class CustomGsonConverter extends Converter.Factory {

    private final Gson gson;

    public static CustomGsonConverter create() {
        return create(new Gson());
    }

    public static CustomGsonConverter create(Gson gson) {
        return new CustomGsonConverter(gson);
    }

    private CustomGsonConverter(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    /**
     * 将网络请求响应体转换成JSON格式对象
     * @param type 目标JSON格式对象类型
     * @param annotations 所有注解信息
     * @param retrofit retrofit对象
     * @return 返回经过转换的JSON格式对象
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new CustomResponseConverter<ResponseBody>(gson, type);
    }

    /**
     * 将网络请求体转换成JSON格式对象
     * @param type 目标JSON格式对象类型
     * @param parameterAnnotations 所有参数的注解信息
     * @param methodAnnotations 所有函数的注解信息
     * @param retrofit retrofit对象
     * @return 返回经过转换的JSON格式对象
     */
    /*@Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new CustomRequestConverter<ResponseBody>(gson, type);
    }*/


}
