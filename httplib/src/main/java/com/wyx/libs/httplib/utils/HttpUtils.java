package com.wyx.libs.httplib.utils;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;

import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 14:50
 * @since: 1.2.0
 */
public class HttpUtils {

    public static RequestBody mapToJsonBody(Map<String, String> pParams){
        return RequestBody.create(new JSONObject(pParams).toString(), MediaType.parse("Content-Type, application/json"));
    }

    /**
     * 对象生命周期是否已结束
     * @param pObject 对象
     * @return
     */
    public static boolean isDestroyed(Object pObject){
        if(pObject == null){
            return true;
        }
        if(pObject instanceof Activity){
            return ((Activity) pObject).isDestroyed() || ((Activity) pObject).isFinishing();
        }

        if(pObject instanceof Fragment){
            return ((Fragment) pObject).isDetached() || isDestroyed(((Fragment) pObject).getActivity());
        }

        if(pObject instanceof android.app.Fragment){
            return ((android.app.Fragment) pObject).isDetached() || isDestroyed(((android.app.Fragment) pObject).getActivity());
        }

        if(pObject instanceof View){
            return isDestroyed(((View) pObject).getContext());
        }

        if(pObject instanceof WeakReference){
            Object targetObj = ((WeakReference) pObject).get();
            return isDestroyed(targetObj);
        }

        if(pObject instanceof SoftReference){
            Object targetObj = ((SoftReference) pObject).get();
            return isDestroyed(targetObj);
        }

        return false;
    }
}
