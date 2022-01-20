package com.app.notepad.client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static Retrofit retrofit=null;

    public static String URL="https://3xs79s0rob.execute-api.us-west-1.amazonaws.com/";
    public static Retrofit getRetrofit() {
        if(retrofit==null){
            retrofit=new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(URL)
                    .build();
        }
        return retrofit;
    }
}
