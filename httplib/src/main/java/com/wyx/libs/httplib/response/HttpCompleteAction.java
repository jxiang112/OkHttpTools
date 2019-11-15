package com.wyx.libs.httplib.response;

import com.wyx.libs.httplib.utils.SafeObject;

import io.reactivex.functions.Action;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 18:15
 * @since: 1.2.0
 */
public abstract class HttpCompleteAction<T> extends SafeObject<T> implements Action {

    public HttpCompleteAction(T pObject) {
        super(pObject);
    }

    @Override
    public void run() throws Exception {
        if(isDestroyed()){
            return;
        }
        onComplete();
    }

    public abstract void onComplete();
}
