package com.wyx.libs.okhttptools;

import com.google.gson.JsonObject;
import com.wyx.libs.httplib.response.HttpRespModel;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 13:38
 * @since: 1.2.0
 */
public interface TestApi {

    @POST("/account/testLogin")
    Flowable<HttpRespModel<String>> login(
            @Body RequestBody pInvideCod
            );

    @GET("/account/userInfo")
    Flowable<HttpRespModel<JsonObject>> testGet(
            @Query("userId") String userId
    );

    @FormUrlEncoded
    @POST("/account/userInfo")
    Flowable<HttpRespModel<TestUser>> testNoCacheLogin(
            @Field("userId") String userId
    );

    @FormUrlEncoded
    @POST("/account/userInfo")
    Flowable<HttpRespModel<JsonObject>> testConnectTime(
            @Field("userId") String userId
    );
}
