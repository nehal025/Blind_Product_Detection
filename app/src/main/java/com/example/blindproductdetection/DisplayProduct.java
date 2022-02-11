package com.example.blindproductdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.blindproductdetection.model.Cost;
import com.example.blindproductdetection.model.Product;
import com.example.blindproductdetection.utils.Utils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayProduct extends AppCompatActivity implements RecognitionListener {
    TextView mLocation,mConfidence;
    String location,confidence;
    TextToSpeech t;
    LottieAnimationView lottie;
    CoordinatorLayout displayProduct;
    ImageView img;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_product);
        lottie=findViewById(R.id.lottie);
        displayProduct=findViewById(R.id.displayProduct);
        displayProduct.setVisibility(View.INVISIBLE);
        img=findViewById(R.id.product_img);

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
        resetSpeechRecognizer();
        setRecogniserIntent();
        welcomeSpeech();





    }

    private void getRetrofit(String productName) {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        String baseURL = "https://blind-product-detection.herokuapp.com/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitArrayApi service = retrofit.create(RetrofitArrayApi.class);
        Call<List<Product>> call = service.getProduct(productName);


        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {

                if(!response.body().isEmpty()){
                    lottie.cancelAnimation();
                    lottie.setVisibility(View.GONE);
                    displayProduct.setVisibility(View.VISIBLE);


//                    RequestOptions requestOptions = new RequestOptions();
//                    requestOptions.placeholder(Utils.getRandomDrawbleColor());
//                    requestOptions.error(Utils.getRandomDrawbleColor());
//                    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
//                    requestOptions.centerCrop();
//
//                    Glide.with(getApplicationContext())
//                            .load(response.body().get(0).getImg())
//                            .apply(requestOptions)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
////                                    holder.progressBar.setVisibility(View.GONE);
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                                    holder.progressBar.setVisibility(View.GONE);
//                                    return false;
//                                }
//                            })
//                            .transition(DrawableTransitionOptions.withCrossFade())
//                            .into(img);

                    t = new TextToSpeech(getApplicationContext(), i -> {

                        String text =response.body().get(0).getTitle()+"detected And,"+response.body().get(0).getInfo() +",that's about it "+",And for navigation ,Say exit for going back to home screen and,say capture for identifying another product";


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            t.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }

                    });



                   exitSpeech();




                }else{


                }
            }




            @Override
            public void onFailure(Call <List<Product>> call, Throwable t) {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "This is a message displayed in a Toast",
                        Toast.LENGTH_SHORT);

                toast.show();

            }
        });


    }


    public void welcomeSpeech(){
        t = new TextToSpeech(getApplicationContext(), i -> {

            String text = "Fetching product information";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
            } else {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

        });

        final Handler h =new Handler();
        Runnable r = new Runnable() {

            public void run() {

                if (!t.isSpeaking()) {

//                    speech.startListening(recognizerIntent);
//                        Toast.makeText(getBaseContext(), "TTS Completed", Toast.LENGTH_SHORT).show();
                    getRetrofit(location);


                    return;
                }

                h.postDelayed(this, 500);
            }
        };

        h.postDelayed(r, 500);
    }

    public void exitSpeech(){


        final Handler h =new Handler();
        Runnable r = new Runnable() {

            public void run() {

                if (!t.isSpeaking()) {

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
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speech.setRecognitionListener(this);
        } else {
            finish();
        }
    }

    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
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

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//        String text = "";
//
//        for (String result : matches)
//
//            text += result + "\n";

        if (matches.contains("exit")) {
            finish();
        }
        if (matches.contains("capture")) {
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
        t.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
        t.stop();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}