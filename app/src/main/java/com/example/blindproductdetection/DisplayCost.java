package com.example.blindproductdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blindproductdetection.adapter.CostAdapter;
import com.example.blindproductdetection.api.RetrofitArrayApi;
import com.example.blindproductdetection.model.Cost;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayCost extends AppCompatActivity {
    TextToSpeech t;
    ProgressBar progressBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<Cost> productCost = new ArrayList<>();
    CostAdapter costAdapter;
    RelativeLayout errorLayout;
    ImageView errorImage;
    TextView errorTitle, errorMessage;
    Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cost);
        Intent intent = getIntent();
        TextView tv=findViewById(R.id.productName);
        progressBar=findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.recyclerView_restaurant);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRetry);



        String name = intent.getStringExtra("name");
        tv.setText(name);
        t = new TextToSpeech(getApplicationContext(), i -> {

            String text = "searching "+name+", in amazon ";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
            getRetrofit(name);

        });

    }

    private void getRetrofit(String productName) {
        errorLayout.setVisibility(View.GONE);

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
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Cost>> call, @NonNull Response<List<Cost>> response) {

                assert response.body() != null;
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


                    if (!productCost.isEmpty()){
                        productCost.clear();
                    }

                    for (int i=0; i<response.body().size();i++){

                        productCost.add( new Cost(response.body().get(i).getTitle(),
                                response.body().get(i).getImg(),
                                "Rs"+response.body().get(i).getCash(),response.body().get(i).getBookNow()

                        ));
                    }


                    costAdapter = new CostAdapter(productCost, getApplicationContext());
                    recyclerView.setAdapter(costAdapter);
                    costAdapter.notifyDataSetChanged();
                    initListener();




                }else{

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_result,
                            "No Result",
                            "Please Try Again!\n"+
                                    errorCode);

                }
            }




            @Override
            public void onFailure(Call <List<Cost>> call, Throwable t) {

                showErrorMessage(R.drawable.no_result, "Oops..",

                        t.toString());

            }
        });


    }
    private void showErrorMessage(int imageView, String title, String message){

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        });
    }

    private void initListener(){


        costAdapter.setOnItemClickListener(new CostAdapter.OnItemClickListener() {


            @Override
            public void onClickBookNow(View view, int position) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(productCost.get(position).getBookNow())));

            }

            @Override
            public void onItemClick(int position) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(productCost.get(position).getBookNow())));

            }
        });

    }

}