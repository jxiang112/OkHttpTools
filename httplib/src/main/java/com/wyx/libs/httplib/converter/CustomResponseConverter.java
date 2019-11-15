package com.wyx.libs.httplib.converter;

import com.google.gson.Gson;
import com.wyx.libs.httplib.HttpTools;
import com.wyx.libs.httplib.response.HttpRespModel;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;


/**
 * 网络体向应体JSON转换器
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 13:56
 * @since: 1.0.0
 */
public class CustomResponseConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    public CustomResponseConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    /**
     * 将网络请求响应体转换成JSON格式对象
     * @param value 网络请求响应体
     * @return 返回转换之后的JSON格式对象
     * @throws IOException 异常
     */
    @Override
    public T convert(ResponseBody value) throws IOException {
        /*boolean flag = false;
        if(flag) {
            String result = value.string();
            Lg.d("httpResponseRaw", result);
        }
        String result = value.string();
        Reader reader = value.charStream();
        if(String.class.equals(type)){
            try {
                String valueStr = new String(value.bytes());
                valueStr = valueStr.replaceAll("\r\n", "");
                return (T) valueStr;
            }catch (Exception e){
                return (T)"";
            }
        }else if(ResponseBody.class.equals(type)){
            return (T) value;
        }
        try {
            return gson.fromJson(reader, type);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }*/
//        String result = value.string();
        Reader reader = value.charStream();
        String valueStr = null;
        try {
            valueStr = new String(value.bytes());
            valueStr = valueStr.replaceAll("\r\n", "");
        }catch (Exception e){
            e.printStackTrace();
        }
//        Lg.d("HttpResponse", valueStr);
        System.out.println("HttpResponse = " + valueStr);
        if(String.class.equals(type)){
            return (T) (valueStr == null ? "" : valueStr);
        }else if(ResponseBody.class.equals(type)){
            return (T) value;
        }

//        JSONObject dataJsonObj = null;
        try{
            JSONObject jsonObject = new JSONObject(valueStr);
            int code = jsonObject.getInt(HttpTools.getResponseCodeKey());
            String msg = jsonObject.optString(HttpTools.getResponseMsgKey(), "");
            long time = jsonObject.optLong(HttpTools.getResponseTimeKey(), 0);
            if(code != HttpTools.getSuccessCode()){
                HttpRespModel<T> respModel = new HttpRespModel<>();
                respModel.setCode(code);
                respModel.setMessage(msg);
                respModel.setTime(time);
                respModel.setOriginResponseStr(valueStr);
                return (T) respModel;
            }
//            dataJsonObj = jsonObject.getJSONObject(HttpTools.getResponseDataKey());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("HttpResponse Exception = " + e.getMessage());
        }
        try {
            T obj = gson.fromJson(valueStr, type);
            if(obj instanceof HttpRespModel){
                ((HttpRespModel) obj).setOriginResponseStr(valueStr);
//                ((HttpRespModel) obj).setDataJsonObj(dataJsonObj);
            }
            return obj;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
