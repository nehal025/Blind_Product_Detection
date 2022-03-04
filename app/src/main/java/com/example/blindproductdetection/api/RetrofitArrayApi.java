package com.example.blindproductdetection.api;

import com.example.blindproductdetection.model.Cost;
import com.example.blindproductdetection.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitArrayApi {

    @GET("/cost/")
    Call<List<Cost>> getCost(@Query("name")String name);


    @GET("/products/")
    Call<List<Product>> getProduct(@Query("title")String name);


}
