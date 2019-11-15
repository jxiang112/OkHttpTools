package com.wyx.libs.httplib.utils;

import android.util.Base64;

import com.wyx.libs.httplib.HttpTools;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 网络请求签名工具类，从应用商店移过来
 * @author: yongxiang.wei
 * @version: 1.0.0, 2018/5/9 18:52
 * @since: 1.0.0
 */
public class HmacSHA1Encryption {
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    private static final String TAG = "HmacSHA1Encryption";


    /**
     * 使用 HMAC-SHA1 签名方法对对 encryptText 进行签名
     *
     * @param encryptText 被签名的字符串
     * @return 返回被加密后的字符串
     */
    public static String HmacSHA1Encrypt(String encryptText) {

        try {
//            Log.v(TAG, "Constant.ENCRYPT_KEY  : "+ HttpConst.ENCRYPT_KEY );
            byte[] data = new byte[0];
            data = HttpTools.getEncryptKey().getBytes(ENCODING);
            SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
            Mac mac = Mac.getInstance(MAC_NAME);
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(ENCODING);
            byte[] digest = mac.doFinal(text);
            byte[] encode = Base64.encode(digest, Base64.NO_WRAP);
            return new String(encode);// 将digest进行base64 encode后返回
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 使用 HMAC-SHA1 签名方法对对 encryptText 进行签名
     *
     * @param encryptData 被签名的字符串
     * @return 返回被加密后的字符串
     */
    public static String HmacSHA1Encrypt(byte[] encryptData) throws Exception {
        byte[] data = HttpTools.getEncryptKey().getBytes(ENCODING);
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        Mac mac = Mac.getInstance(MAC_NAME);
        mac.init(secretKey);
        byte[] digest = mac.doFinal(encryptData);
        byte[] encode = Base64.encode(digest, Base64.NO_WRAP);
        return new String(encode);
    }

}
