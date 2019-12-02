package com.eso.realtime.Retrofit;


import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    public static Retrofit getClient(String baseURL){
        if (retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(baseURL).
                    addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
