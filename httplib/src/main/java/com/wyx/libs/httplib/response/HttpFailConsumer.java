package com.wyx.libs.httplib.response;

import com.wyx.libs.httplib.exceptions.HttpException;
import com.wyx.libs.httplib.utils.SafeObject;

import io.reactivex.functions.Consumer;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 18:09
 * @since: 1.2.0
 */
public abstract class HttpFailConsumer<T, K extends Throwable> extends SafeObject<T> implements Consumer<K> {



    public HttpFailConsumer(T pObject) {
        super(pObject);
    }


    @Override
    public void accept(K k) throws Exception {
        if(isDestroyed()){
            return;
        }
        HttpException httpException = null;
        if(k == null){
            httpException = new HttpException();
            httpException.setErrMsg("occur unknow error");
        }else if(k instanceof HttpException){
            httpException = (HttpException) k;
        }else {
            httpException = new HttpException();
            httpException.setErrMsg(k.toString());
        }
        onFail(httpException);
    }

    public abstract void onFail(HttpException httpException);
}
