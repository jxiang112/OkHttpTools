package com.wyx.libs.httplib.response;

import com.google.gson.annotations.SerializedName;
import com.wyx.libs.httplib.HttpTools;

import java.io.Serializable;

/**
 * http返回的数据格式
 * {
 * "meta": {
 * "code": 0,
 * "msg": "",
 * "error": "",
 * "request_uri": ""
 * },
 * "response": {
 * }
 * }
 *
 * Http 请求返回的数据模型
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 13:56
 * @since: 1.0.0
 */
public class HttpRespModel<T> implements Serializable {

    /*@SerializedName("code")
    private int mCode;
    @SerializedName("msg")
    private String mMessage;*/

    @SerializedName("code")
    protected int code;
    @SerializedName("msg")
    protected String message;
    @SerializedName("time")
    protected long time;

    /**
     * http返回的实体
     */
    @SerializedName("data")
    private T data;

    private String originResponseStr;

    public int getCode() {
        return code;
    }

    public void setCode(int pCode) {
        this.code = pCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String pMessage) {
        this.message = pMessage;
    }

    public T getData() {
        return data;
    }

    public long getTime() {
        return 0;
    }

    public void setTime(long time){
        this.time = time;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getOriginResponseStr() {
        return originResponseStr;
    }

    public void setOriginResponseStr(String originResponseStr) {
        this.originResponseStr = originResponseStr;
    }

    public boolean isSuccess() {
        if (code == HttpTools.getSuccessCode()) {
            return true;
        } else {
            return false;
        }
    }
}


