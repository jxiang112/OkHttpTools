package com.wyx.libs.httplib.converter;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.wyx.libs.httplib.utils.HmacSHA1Encryption;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/**
 * 网络请求体JSON转换器
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 13:56
 * @since: 1.0.0
 */
public class CustomRequestConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");


    private final Gson gson;
    private final Type type;

    CustomRequestConverter(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    /**
     * 将网络请求体转成JSON格式对象
     * @param value 网络请求体
     * @return 返回JSON格式对象
     * @throws IOException 异常
     */
    @Override
    public RequestBody convert(T value) throws IOException {
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        try {
            HashMap<String, Object> paramMap = sign(value);
            gson.toJson(paramMap, HashMap.class, writer);
            writer.flush();
        } catch (Exception e) {
            throw new AssertionError(e); // Writing to Buffer does no I/O.
        }
//        return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        return RequestBody.create(buffer.readByteString(), MEDIA_TYPE);
    }

    public static HashMap<String, Object> sign(Object obj) throws IllegalArgumentException,
            IllegalAccessException {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        Class clazz = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();
        do {
            clazzs.add(clazz);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));

        for (Class iClazz : clazzs) {
            Field[] fields = iClazz.getDeclaredFields();
            for (Field field : fields) {
                Object objVal = null;
                field.setAccessible(true);
                if(field.getName().equals("serialVersionUID")){
                    continue;
                }else {
                    objVal = field.get(obj);
                    if (objVal != null)//如果为null值 则不计入 md5摘要 ，因为 http传输层 不会把null值送出去 modified by janyo at 2015-5-7
                        hashMap.put(field.getName(), objVal);
                }
            }
        }
        LinkedList<KeyValue> paramList = sortParam(hashMap);
        StringBuilder sb = new StringBuilder();

        for (KeyValue pair : paramList) {
            sb.append(pair.getKey()).append("=").append(pair.getValue()).append("&");
        }
        if(paramList.size() > 0){
            sb.deleteCharAt(sb.length() - 1);
        }
        String sign = HmacSHA1Encryption.HmacSHA1Encrypt(sb.toString());
        //签名，算法参考1.签名算法;算法的secret为: pkm#yuns8$
        if(!TextUtils.isEmpty(sign)){
            hashMap.put("Game-Sign", sign);
        }
        return hashMap;
    }

    public static LinkedList<KeyValue> sortParam(Map<String, Object> params) {
        LinkedList<KeyValue> pairs = new LinkedList<KeyValue>();
        ArrayList<String> temp = new ArrayList<String>();
        for (String key : params.keySet()) {
            temp.add(key);
        }
        temp.trimToSize();
        Collections.sort(temp);
        Gson gson = new Gson();
        for (String string : temp) {
            Object valueObj = params.get(string);
            String value = "";
            if (valueObj != null) {
                if (valueObj instanceof Integer || valueObj instanceof String || valueObj instanceof Double || valueObj instanceof Float
                        || valueObj instanceof Long || valueObj instanceof Boolean || valueObj instanceof Byte || valueObj instanceof Character) {
                    value = valueObj.toString();
                } else {
                    value = gson.toJson(valueObj);
                }
            }
            pairs.add(new KeyValue(string, value));
        }
        return pairs;
    }

    public static class KeyValue {
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}