package com.example.blindproductdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindproductdetection.api.RetrofitArrayApi;
import com.example.blindproductdetection.model.Cost;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayProduct extends AppCompatActivity {
    TextView mLocation,mConfidence;
    String location,confidence;
    TextToSpeech t;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_product);

        mLocation=findViewById(R.id.location);
        mConfidence=findViewById(R.id.des);

        location= getIntent().getExtras().getString("location");
        confidence= getIntent().getExtras().getString("confidence");

        mLocation.setText(location);
        mConfidence.setText(confidence);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        CollapsingToolbarLayout collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(location);
        t = new TextToSpeech(getApplicationContext(), i -> {

            String text = location+",detected";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
//            getRetrofit(location);

        });



    }

    private void getRetrofit(String productName) {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        String baseURL = "http://192.168.1.201:3000//";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
        Call<List<Cost>> call = service.getCost(productName);


        call.enqueue(new Callback<List<Cost>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cost>> call, @NonNull Response<List<Cost>> response) {

                if(!response.body().isEmpty()){
                    progressBar.setVisibility(View.GONE);
                    t = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int i) {

                            String text ="The Minimum cost product is "+ response.body().get(0).getTitle()+"for rupees "+response.body().get(0).getCash()+" And  The Maximum cost product is"+ response.body().get(1).getTitle()+"for rupees "+response.body().get(1).getCash();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                t.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                            } else {
                                t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });


                }else{


                }
            }




            @Override
            public void onFailure(Call <List<Cost>> call, Throwable t) {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "This is a message displayed in a Toast",
                        Toast.LENGTH_SHORT);

                toast.show();

            }
        });


    }

}