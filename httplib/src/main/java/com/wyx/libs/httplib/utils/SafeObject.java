package com.wyx.libs.httplib.utils;

import java.lang.ref.WeakReference;

/**
 * 线程安全的对象
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 11:41
 * @since: 1.0.0
 */
public class SafeObject<T> {
    private WeakReference<T> mReference;

    public SafeObject(T pObject){
        mReference = new WeakReference<>(pObject);
    }

    public T getReferenceTarget(){
        T t = mReference == null ? null : mReference.get();
        return t;
    }

    public void updateReference(T pReference){
        mReference = new WeakReference<>(pReference);
    }

    public boolean isDestroyed(){
        T obj = getReferenceTarget();
        if(HttpUtils.isDestroyed(obj)){
            return true;
        }
        return false;
    }

    public void destroy(){
        mReference = null;
    }
}
