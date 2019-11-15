package com.wyx.libs.okhttptools;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;
import com.wyx.libs.httplib.HttpTools;
import com.wyx.libs.httplib.exceptions.HttpException;
import com.wyx.libs.httplib.response.HttpCompleteAction;
import com.wyx.libs.httplib.response.HttpDefaultFlatResult;
import com.wyx.libs.httplib.response.HttpFailConsumer;
import com.wyx.libs.httplib.response.HttpRespModel;
import com.wyx.libs.httplib.response.HttpSuccessConsumer;
import com.wyx.libs.httplib.utils.HttpUtils;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                testApi();
            }
        });

        findViewById(R.id.testNoCacheLogin).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                testNoCacheLogin();
            }
        });

        findViewById(R.id.testConnectTime).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                testConnectTime();
            }
        });

        findViewById(R.id.testGet).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                testGet();
            }
        });

        HttpTools.init(this, "http://172.28.36.12:8001");
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PERMISSION_GRANTED;
    }

    private void testApi() {
        if (hasPermission()) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1000);
            return;
        }

        testApiInner();
    }

    private void testApiInner() {

        ArrayMap<String, String> params = new ArrayMap<>(1);
        params.put("inviate_code", "666");
        HttpTools.newRequest()
                .getApi(TestApi.class)
                .login(HttpUtils.mapToJsonBody(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(HttpDefaultFlatResult::flatMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSuccessConsumer<MainActivity, String>(this) {
                    @Override
                    public void onSuccess(String testUser) {
                        Log.d("testApiInner", "onSuccess" + testUser);
                    }
                }, new HttpFailConsumer<MainActivity, Throwable>(this) {
                    @Override
                    public void onFail(HttpException httpException) {
                        Log.d("testApiInner", "onFail" + httpException.toString());
                    }
                }, new HttpCompleteAction<MainActivity>(this) {
                    @Override
                    public void onComplete() {
                        Log.d("testApiInner", "onComplete");
                    }
                });
    }

    private void testNoCacheLogin(){
        HttpTools.Builder builder = new HttpTools.Builder();
        builder.enableCache(false);
        HttpTools.newRequest(builder)
                .getApi(TestApi.class)
                .testNoCacheLogin("07a8d8285ba140ffabd29fb500b2e4fb")
                .subscribeOn(Schedulers.io())
                .flatMap(HttpDefaultFlatResult::flatMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSuccessConsumer<MainActivity, TestUser>(this) {
                    @Override
                    public void onSuccess(TestUser testUser) {
                        Log.d("testNoCacheLogin", "onSuccess" + testUser);
                    }
                }, new HttpFailConsumer<MainActivity, Throwable>(this) {
                    @Override
                    public void onFail(HttpException httpException) {
                        Log.d("testNoCacheLogin", "onFail" + httpException.toString());
                    }
                }, new HttpCompleteAction<MainActivity>(this) {
                    @Override
                    public void onComplete() {
                        Log.d("testNoCacheLogin", "onComplete");
                    }
                });
    }

    private void testConnectTime() {
        HttpTools.Builder builder = new HttpTools.Builder();
        builder.connectTimeout(5000);
        HttpTools.newRequest(builder)
                .getApi(TestApi.class)
                .testConnectTime("07a8d8285ba140ffabd29fb500b2e4fb")
                .subscribeOn(Schedulers.io())
                .flatMap(HttpDefaultFlatResult::flatMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSuccessConsumer<MainActivity, JsonObject>(this) {
                    @Override
                    public void onSuccess(JsonObject testUser) {
                        Log.d("testConnectTime", "onSuccess" + testUser);
                    }
                }, new HttpFailConsumer<MainActivity, Throwable>(this) {
                    @Override
                    public void onFail(HttpException httpException) {
                        Log.d("testConnectTime", "onFail" + httpException.toString());
                    }
                }, new HttpCompleteAction<MainActivity>(this) {
                    @Override
                    public void onComplete() {
                        Log.d("testConnectTime", "onComplete");
                    }
                });
    }


    private void testGet() {

        HttpTools.newRequest()
                .getApi(TestApi.class)
                .testGet("07a8d8285ba140ffabd29fb500b2e4fb")
                .subscribeOn(Schedulers.io())
                .flatMap(HttpDefaultFlatResult::flatMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSuccessConsumer<MainActivity, JsonObject>(this) {
                    @Override
                    public void onSuccess(JsonObject testUser) {
                        Log.d("testGet", "onSuccess" + testUser);
                    }
                }, new HttpFailConsumer<MainActivity, Throwable>(this) {
                    @Override
                    public void onFail(HttpException httpException) {
                        Log.d("testGet", "onFail" + httpException.toString());
                    }
                }, new HttpCompleteAction<MainActivity>(this) {
                    @Override
                    public void onComplete() {
                        Log.d("testGet", "onComplete");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (hasPermission()) {
                testApiInner();
            }
        }
    }
}
