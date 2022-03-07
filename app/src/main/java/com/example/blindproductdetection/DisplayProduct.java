package com.example.blindproductdetection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.blindproductdetection.api.RetrofitArrayApi;
import com.example.blindproductdetection.model.Product;
import com.example.blindproductdetection.utils.Utils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayProduct extends AppCompatActivity implements RecognitionListener {

    TextView displayProduct, displayDes, displayPrice;
    String product, confidence;
    TextToSpeech textToSpeech;
    LottieAnimationView lottie;
    ImageView displayImg;
    CoordinatorLayout hideLayout;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_product);
        lottie = findViewById(R.id.lottie);
        displayProduct = findViewById(R.id.product);
        displayImg = findViewById(R.id.product_img);
        displayDes = findViewById(R.id.product_des);
        displayPrice = findViewById(R.id.product_cost);
        hideLayout = findViewById(R.id.hide_layout);
        hideLayout.setVisibility(View.INVISIBLE);


        product = getIntent().getExtras().getString("product");
        confidence = getIntent().getExtras().getString("confidence");

        displayProduct.setText(product);
//        mConfidence.setText(confidence);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(product);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);


        resetSpeechRecognizer();
        setRecogniserIntent();
        welcomeSpeech();
        getRetrofit(product);


    }

    private void getRetrofit(String productName) {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        String baseURL = getResources().getString(R.string.link);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
        Call<List<Product>> call = service.getProduct(productName);


        call.enqueue(new Callback<List<Product>>() {
            @SuppressLint("CheckResult")
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {

                if (!response.body().isEmpty()) {
                    lottie.cancelAnimation();
                    lottie.setVisibility(View.GONE);
                    hideLayout.setVisibility(View.VISIBLE);


                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(Utils.getRandomDrawbleColor());
                    requestOptions.error(Utils.getRandomDrawbleColor());
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                    requestOptions.centerCrop();

                    Glide.with(getApplicationContext())
                            .load(response.body().get(0).getImg())
                            .apply(requestOptions)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    holder.progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    holder.progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(displayImg);
                    displayDes.setText(response.body().get(0).getInfo());
                    displayPrice.setText(response.body().get(0).getPrice());

                    textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

                        String text = response.body().get(0).getTitle() + ", detected And," + response.body().get(0).getInfo() + " ,It is for" + response.body().get(0).getPrice() + "rupees ,that's about it " + ",And for navigation ,Say exit for going back to home screen ";

                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

                    });

                    exitSpeech();

                }
            }


            @Override
            public void onFailure(@NonNull Call<List<Product>> call, Throwable t) {


            }
        });


    }


    public void welcomeSpeech() {

        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

            String text = "Fetching product information";

            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

        });

    }

    public void exitSpeech() {

        final Handler h = new Handler();
        Runnable r = new Runnable() {

            public void run() {

                if (!textToSpeech.isSpeaking()) {

                    speech.startListening(recognizerIntent);

                    return;
                }

                h.postDelayed(this, 100);
            }
        };

        h.postDelayed(r, 100);
    }

    private void resetSpeechRecognizer() {

        if (speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speech.setRecognitionListener(this);
        } else {
            finish();
        }
    }

    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (matches.contains("exit")) {
            finish();
        }

        speech.startListening(recognizerIntent);

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onResume() {
        super.onResume();
        resetSpeechRecognizer();
        welcomeSpeech();
    }

    @Override
    protected void onPause() {
        super.onPause();
        speech.stopListening();
        textToSpeech.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
        textToSpeech.stop();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}