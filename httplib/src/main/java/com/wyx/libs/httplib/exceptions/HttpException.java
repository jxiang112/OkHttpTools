package com.wyx.libs.httplib.exceptions;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/15 14:11
 * @since: 1.2.0
 */
public class HttpException extends Throwable {
    private int errCode = -1;
    private String errMsg;
    private long time;

    public  HttpException(){
        time = System.currentTimeMillis();
    }

    public HttpException(int pErrCode, String pErrMsg){
        this(pErrCode, pErrMsg, 0);
    }

    public HttpException(int pErrCode, String pErrMsg, long pTime){
        errCode = pErrCode;
        errMsg = pErrMsg;
        time = pTime;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "HttpException{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", time=" + time +
                '}';
    }
}
