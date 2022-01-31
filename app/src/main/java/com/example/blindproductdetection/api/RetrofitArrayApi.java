package com.example.blindproductdetection.api;

import com.example.blindproductdetection.model.Cost;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitArrayApi {

    @GET("/cost/")
    Call<List<Cost>> getCost(@Query("name")String name);



    @GET("/products/")
    Call<List<Cost>> getProduct(@Query("name")String name);
}
