package com.wyx.libs.httplib.response;

import com.wyx.libs.httplib.utils.SafeObject;

import io.reactivex.functions.Consumer;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 18:01
 * @since: 1.2.0
 */
public abstract class HttpSuccessConsumer<T, K> extends SafeObject<T> implements Consumer<K> {

    public HttpSuccessConsumer(T pObject) {
        super(pObject);
    }

    /**
     * 对网络请求成功返回的数据信息的处理逻辑
     * @param pResponseData 网络请求成功返回的数据
     */
    @Override
    public void accept(K pResponseData) throws Exception {
        if(isDestroyed()){
            return;
        }
        onSuccess(pResponseData);
    }

    /**
     * 最终呈现给用户的网络请求成功返回的数据信息
     * @param k 用户期望的网络请求成功返回的数据信息
     */
    public abstract void onSuccess(K k);
}
