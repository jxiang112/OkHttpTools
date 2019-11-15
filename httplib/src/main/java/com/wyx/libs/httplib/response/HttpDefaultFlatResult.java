package com.wyx.libs.httplib.response;

import android.text.TextUtils;

import com.wyx.libs.httplib.exceptions.HttpException;
import com.wyx.libs.httplib.HttpTools;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 18:29
 * @since: 1.2.0
 */
public class HttpDefaultFlatResult {
    public static <T> Flowable<T> flatMap(HttpRespModel<T> httpRespModel){
        return Flowable.create(subscriber -> {
            HttpException httpException = null;
            int code = -1;
            String msg = null;
            long time = 0;
            if (httpRespModel == null) {
                msg = "reponse info is null";
            }else{
                code = httpRespModel.getCode();
                msg = httpRespModel.getMessage();
                time = httpRespModel.getTime();
            }
            if(time == 0){
                time = System.currentTimeMillis();
            }
            if(code == HttpTools.getSuccessCode()){
                try {
                    subscriber.onNext(httpRespModel.getData());
                    subscriber.onComplete();
                    return;
                }catch (Throwable e){
                    e.printStackTrace();
                    code = -1;
                    msg = "occurr error while do success step";
                }
            }
            if(TextUtils.isEmpty(msg)){
                msg = "occurr unknow error";
            }
            httpException = new HttpException(code, msg, time);
            subscriber.onError(httpException);
        }, BackpressureStrategy.BUFFER);
    }
}
