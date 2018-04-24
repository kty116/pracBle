package com.deltaworks.pracble.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2018-03-15.
 */

public class RetrofitLib {

    public static final String TAG = RetrofitLib.class.getSimpleName();
    private boolean isSuccess = false;



    public RetrofitService getRetrofit() {

//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        isSuccess = false;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitService.url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        return retrofitService;
    }

}
