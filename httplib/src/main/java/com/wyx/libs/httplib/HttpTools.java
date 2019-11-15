package com.wyx.libs.httplib;

import android.content.Context;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.wyx.libs.httplib.converter.CustomGsonConverter;
import com.wyx.libs.httplib.cookie.CookieJarImpl;
import com.wyx.libs.httplib.cookie.PersistentCookieStore;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author: yongxiang.wei
 * @version: 1.2.0, 2019/11/14 16:22
 * @since: 1.2.0
 */
public class HttpTools {
    static final String TAG = "HttpTools";
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;
    private static final int WRITE_TIMEOUT = 10000;

    private static final int MIN_CACHE_SIZE = 5 * 1024 * 1024; // 5M
    private static final int DEFAULT_CACHE_SIZE = 10 * 1024 * 1024; // 10M
    private static final int MAX_CACHE_SIZE = 30 * 1024 * 1024; // 30M

    static CookieJar sDefaultCookieJar;

    static final String CACHE_FOLDER_NAME = "http_caches";
    static Cache sDefaultCache;

    static Converter.Factory sDefaultConverterFactory;
    static CallAdapter.Factory sDefaultCallAdapter;
    static Interceptor sDefaultInterceptor;

    static HttpLoggingInterceptor sLogInterceptor;
//    static Interceptor sDefaultInterceptor;

    static String sEncryptKey = "?wyx_pwd=@";

    static String sDefaultBaseUrl;
    static Context sContext;

    private static final ArrayMap<String, HttpTools> sHttpToolCache = new ArrayMap<>();
    private static final ArrayMap<Class, WeakReference<Object>> sApiCache = new ArrayMap<>();

    private Retrofit mRetrofit;

    static int sSuccessCode = 1;
    private static String sResponseCodeKey = "code";
    private static String sResponseMsgKey = "msg";
    private static String sResponseTimeKey = "time";
    private static String sResponseDataKey = "data";


    static {
        sDefaultConverterFactory = CustomGsonConverter.create(new GsonBuilder()
                .disableHtmlEscaping()
                .create());

        sDefaultCallAdapter = RxJava2CallAdapterFactory.create();
        sLogInterceptor = new HttpLoggingInterceptor();
        sLogInterceptor.level(HttpLoggingInterceptor.Level.BODY);


    }

    public static void init(Context pContext, String pBaseUrl){
        if(pContext == null){
            return;
        }
        sContext = pContext.getApplicationContext();
        sDefaultBaseUrl = pBaseUrl;

        if (sDefaultCache == null) {
            synchronized (Builder.class) {
                if (sDefaultCache == null) {
                    File cacheFolder = new File(sContext.getApplicationContext().getCacheDir(), CACHE_FOLDER_NAME);
                    sDefaultCache = new Cache(cacheFolder, calcCanUseCacheSize(cacheFolder));
                }
            }
        }

        if (sDefaultCookieJar == null) {
            synchronized (Builder.class) {
                if (sDefaultCookieJar == null) {
                    sDefaultCookieJar = new CookieJarImpl(new PersistentCookieStore(sContext.getApplicationContext()));
                }
            }
        }

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                throwable = throwable.getCause();
            }
            if ((throwable instanceof IOException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (throwable instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((throwable instanceof NullPointerException) || (throwable instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
                return;
            }
            if (throwable instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
                return;
            }
            Log.d(TAG,"RxJava setErrorHandler Undeliverable exception");
        });
    }

    public static void configResponseKey(
            String pCodeKey,
            String pMsgKey,
            String pTimeKey,
            String pDataKey){
        sResponseCodeKey = pCodeKey;
        sResponseMsgKey = pMsgKey;
        sResponseTimeKey = pTimeKey;
        sResponseDataKey = pDataKey;
    }

    private HttpTools(Retrofit pRetrofit){
        mRetrofit = pRetrofit;
    }

    public static HttpTools newRequest(){
        return newRequest(null);
    }

    public static HttpTools newRequest(HttpTools.Builder pBuilder){
        if(pBuilder == null){
            pBuilder = new Builder();
        }
        String key = pBuilder.generateKey();
        HttpTools httpTools = sHttpToolCache.get(key);
        if(httpTools == null){
            httpTools = pBuilder.build();
            sHttpToolCache.put(key, httpTools);
        }
        cleanupHttpToolCaches();
        cleanupApiCaches();
        return httpTools;
    }

    public <T> T getApi(Class<T> pClass){
        WeakReference<Object> referenceApi = sApiCache.get(pClass);
        T api = referenceApi == null ? null : (T) referenceApi.get();
        if(api == null){
            api = mRetrofit.create(pClass);
            referenceApi = new WeakReference<>(api);
            sApiCache.put(pClass, referenceApi);
        }
        return api;
    }

    public Retrofit getRetrofit(){
        return mRetrofit;
    }

    public static void setEncryptKey(String pEncryptKey){
        sEncryptKey = pEncryptKey;
    }

    public static String getEncryptKey(){
        return sEncryptKey == null ? "" : sEncryptKey;
    }

    public static int getSuccessCode() {
        return sSuccessCode;
    }

    public static void setSuccessCode(int successCode) {
        HttpTools.sSuccessCode = successCode;
    }

    public static String getDefaultBaseUrl() {
        return sDefaultBaseUrl;
    }

    public static void setDefaultBaseUrl(String sBaseUrl) {
        HttpTools.sDefaultBaseUrl = sBaseUrl;
    }

    public static String getResponseCodeKey() {
        return sResponseCodeKey;
    }

    public static void setResponseCodeKey(String sResponseCodeKey) {
        HttpTools.sResponseCodeKey = sResponseCodeKey;
    }

    public static String getResponseMsgKey() {
        return sResponseMsgKey;
    }

    public static void setResponseMsgKey(String sResponseMsgKey) {
        HttpTools.sResponseMsgKey = sResponseMsgKey;
    }

    public static String getResponseTimeKey() {
        return sResponseTimeKey;
    }

    public static void setResponseTimeKey(String sResponseTimeKey) {
        HttpTools.sResponseTimeKey = sResponseTimeKey;
    }

    public static String getResponseDataKey() {
        return sResponseDataKey;
    }

    public static void setResponseDataKey(String sResponseDataKey) {
        HttpTools.sResponseDataKey = sResponseDataKey;
    }

    public static class Builder {
        private long connectTimeout = CONNECT_TIMEOUT;
        private long readTimeout = READ_TIMEOUT;
        private long writeTimeout = WRITE_TIMEOUT;
        private List<Interceptor> interceptors;
        private List<Interceptor> netInterceptors;
        private List<Converter.Factory> converterFactorys;
        private List<CallAdapter.Factory> callAdapters;
        private String baseUrl;
        private CookieJar cookieJar;
        private Cache cache;

        private boolean enableDefaultInterceptor = true;
        private boolean enableDefaultConverter = true;
        private boolean enableDefaultCallAdapter = true;

        private boolean enableCookieJar = true;
        private boolean enableCache = true;

        private boolean retryOnConnectFail = true;

        private boolean enableLog;

        public CookieJar getCookieJar() {
            return cookieJar;
        }

        public Builder cookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        public Cache getCache() {
            return cache;
        }

        public Builder cache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public boolean isEnableDefaultInterceptor() {
            return enableDefaultInterceptor;
        }

        public Builder enableDefaultInterceptor(boolean defaultInterceptor) {
            this.enableDefaultInterceptor = defaultInterceptor;
            return this;
        }

        public boolean isEnableDefaultConverter() {
            return enableDefaultConverter;
        }

        public Builder enableDefaultConverter(boolean defaultConverter) {
            this.enableDefaultConverter = defaultConverter;
            return this;
        }

        public boolean isEnableDefaultCallAdapter() {
            return enableDefaultCallAdapter;
        }

        public Builder enableDefaultCallAdapter(boolean defaultAdapter) {
            this.enableDefaultCallAdapter = defaultAdapter;
            return this;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public Builder enableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public boolean isEnableRetryOnConnectFail() {
            return retryOnConnectFail;
        }

        public Builder enableRetryOnConnectFail(boolean retryOnConnectFail) {
            this.retryOnConnectFail = retryOnConnectFail;
            return this;
        }

        public boolean isEnableLog() {
            return enableLog;
        }

        public Builder enableLog(boolean enableLog) {
            this.enableLog = enableLog;
            return this;
        }

        public long getConnectTimeout() {
            return connectTimeout;
        }

        public Builder connectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public long getReadTimeout() {
            return readTimeout;
        }

        public Builder readTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public long getWriteTimeout() {
            return writeTimeout;
        }

        public Builder writeTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public List<Interceptor> getInterceptors() {
            return interceptors;
        }

        public Builder interceptors(List<Interceptor> interceptors) {
            if (interceptors == null) {
                return this;
            }
            this.interceptors = interceptors;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                return this;
            }
            this.interceptors.add(interceptor);
            return this;
        }

        public List<Interceptor> getNetInterceptors() {
            return netInterceptors;
        }

        public Builder netInterceptors(List<Interceptor> netInterceptors) {
            if (netInterceptors == null) {
                return this;
            }
            this.netInterceptors = netInterceptors;
            return this;
        }

        public Builder addNetInterceptor(Interceptor netInterceptor) {
            if (netInterceptor == null) {
                return this;
            }
            this.netInterceptors.add(netInterceptor);
            return this;
        }

        public List<Converter.Factory> getConverterFactorys() {
            return converterFactorys;
        }

        public Builder converterFactorys(List<Converter.Factory> converterFactorys) {
            if (converterFactorys == null) {
                return this;
            }
            this.converterFactorys = converterFactorys;
            return this;
        }

        public Builder addConverterFactorys(Converter.Factory converterFactory) {
            if (converterFactory == null) {
                return this;
            }
            this.converterFactorys.add(converterFactory);
            return this;
        }

        public List<CallAdapter.Factory> getCallAdapters() {
            return callAdapters;
        }

        public Builder callAdapters(List<CallAdapter.Factory> callAdapters) {
            if (callAdapters == null) {
                return this;
            }
            this.callAdapters = callAdapters;
            return this;
        }

        public Builder addCallAdapter(CallAdapter.Factory callAdapter) {
            if (callAdapter == null) {
                return this;
            }
            this.callAdapters.add(callAdapter);
            return this;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }


        public boolean isEnableCookieJar() {
            return enableCookieJar;
        }

        public Builder enableCookieJar(boolean enableCookieJar) {
            this.enableCookieJar = enableCookieJar;
            return this;
        }

        public Builder() {
            /*if(sContext == null){
                Log.d(TAG, "occur error on new Builder because context is null, you need call init function first");
                return;
            }*/
            interceptors = new ArrayList<>();
            netInterceptors = new ArrayList<>();
            converterFactorys = new ArrayList<>();
            callAdapters = new ArrayList<>();

        }

        public HttpTools build() {
            return build(null);
        }

        public HttpTools build(OkHttpClient pOkHttpClient) {
            if (converterFactorys == null) {
                converterFactorys = new ArrayList<>();
            }
            if (enableDefaultConverter && sDefaultConverterFactory != null) {
                converterFactorys.add(sDefaultConverterFactory);
            }

            if (callAdapters == null) {
                callAdapters = new ArrayList<>();
            }

            if (enableDefaultCallAdapter && sDefaultCallAdapter != null) {
                callAdapters.add(sDefaultCallAdapter);
            }

            if (interceptors == null) {
                interceptors = new ArrayList<>();
            }

            if (sDefaultInterceptor != null) {
                interceptors.add(sDefaultInterceptor);
            }

            OkHttpClient.Builder okBuilder = null;
            if (pOkHttpClient == null) {
                okBuilder = new OkHttpClient.Builder();
            } else {
                okBuilder = pOkHttpClient.newBuilder();
            }

            okBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
            okBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
            okBuilder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
            if (enableCache) {
                if (cache == null) {
                    okBuilder.cache(sDefaultCache);
                } else {
                    okBuilder.cache(cache);
                }
            }
            okBuilder.retryOnConnectionFailure(retryOnConnectFail);

            if (enableCookieJar) {
                okBuilder.cookieJar(cookieJar == null ? sDefaultCookieJar : cookieJar);
            }

            if (interceptors != null) {
                for (Interceptor item : interceptors) {
                    if(item != null) {
                        okBuilder.addInterceptor(item);
                    }
                }
            }

            if (netInterceptors != null) {
                for (Interceptor item : netInterceptors) {
                    if(item != null) {
                        okBuilder.addNetworkInterceptor(item);
                    }
                }
            }

            if (enableLog) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
                okBuilder.addInterceptor(loggingInterceptor);
            }

            Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
            if (!TextUtils.isEmpty(baseUrl)) {
                retrofitBuilder.baseUrl(baseUrl);
            }else if(!TextUtils.isEmpty(sDefaultBaseUrl)){
                retrofitBuilder.baseUrl(sDefaultBaseUrl);
            }
            if (converterFactorys != null) {
                for (Converter.Factory item : converterFactorys) {
                    if (item != null) {
                        retrofitBuilder.addConverterFactory(item);
                    }
                }
            }

            if (callAdapters != null) {
                for (CallAdapter.Factory item : callAdapters) {
                    if (item != null) {
                        retrofitBuilder.addCallAdapterFactory(item);
                    }
                }
            }

            retrofitBuilder.client(okBuilder.build());

            return new HttpTools(retrofitBuilder.build());
        }


        public String generateKey() {
            StringBuilder sb = new StringBuilder();

            sb.append("connectTimeout=").append(connectTimeout).append(",")
                    .append("&readTimeout=").append(readTimeout).append(",")
                    .append("&writeTimeout=").append(writeTimeout).append(",")
                    .append("&baseUrl=").append(baseUrl).append(",")
                    .append("&enableDefaultInterceptor=").append(enableDefaultInterceptor).append(",")
                    .append("&enableDefaultConverter=").append(enableDefaultConverter).append(",")
                    .append("&enableDefaultCallAdapter=").append(enableDefaultCallAdapter).append(",")
                    .append("&enableCookieJar=").append(enableCookieJar).append(",")
                    .append("&enableCache=").append(enableCache).append(",")
                    .append("&retryOnConnectFail=").append(retryOnConnectFail);
            return sb.toString();
        }
    }

    private static long calcCanUseCacheSize(File pFolder){
        long cacheSize = DEFAULT_CACHE_SIZE;
        try{
            StatFs statFs = new StatFs(pFolder.getAbsolutePath());
            long totalSize = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            if(totalSize < MIN_CACHE_SIZE){
                return totalSize / 2;
            }
            cacheSize = (long) (totalSize * 0.02f);
        }catch (Throwable e){
            e.printStackTrace();
        }
        cacheSize = Math.max(Math.min(cacheSize, MIN_CACHE_SIZE), MAX_CACHE_SIZE);
        return cacheSize;
    }

    public static void clearCache(){
        if(sDefaultCache != null){
            try {
                sDefaultCache.evictAll();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeHttpToolByRetrofit(Retrofit pRetrofit){
        if(pRetrofit == null){
            return;
        }
        try {
            if (sHttpToolCache != null && sHttpToolCache.size() > 0) {
                Iterator<Map.Entry<String, HttpTools>> it = sHttpToolCache.entrySet().iterator();
                Map.Entry<String, HttpTools> entity = null;
                HttpTools httpTools = null;
                while(it.hasNext()){
                    entity = it.next();
                    if(entity == null){
                        it.remove();
                        continue;
                    }
                    httpTools = entity.getValue();
                    if(httpTools == null){
                        it.remove();
                        continue;
                    }
                    if(httpTools.mRetrofit == pRetrofit){
                        it.remove();
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void cleanupHttpToolCaches(){
        try {
            if (sHttpToolCache != null && sHttpToolCache.size() > 0) {
                Iterator<Map.Entry<String, HttpTools>> it = sHttpToolCache.entrySet().iterator();
                Map.Entry<String, HttpTools> entity = null;
                HttpTools httpTools = null;
                while(it.hasNext()){
                    entity = it.next();
                    if(entity == null){
                        it.remove();
                        continue;
                    }
                    httpTools = entity.getValue();
                    if(httpTools == null){
                        it.remove();
                        continue;
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void cleanupApiCaches(){
        try {
            if (sApiCache != null && sApiCache.size() > 0) {
                Iterator<Map.Entry<Class, WeakReference<Object>>> it = sApiCache.entrySet().iterator();
                Map.Entry<Class, WeakReference<Object>> entity = null;
                WeakReference<Object> reference = null;
                while(it.hasNext()){
                    entity = it.next();
                    if(entity == null){
                        it.remove();
                        continue;
                    }
                    reference = entity.getValue();
                    if(reference == null || reference.get() == null){
                        it.remove();
                        continue;
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
