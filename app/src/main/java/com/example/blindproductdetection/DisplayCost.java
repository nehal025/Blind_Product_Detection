package com.example.blindproductdetection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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

public class DisplayCost extends AppCompatActivity implements RecognitionListener {

    TextToSpeech textToSpeech;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<Cost> productCost = new ArrayList<>();
    CostAdapter costAdapter;
    RelativeLayout errorLayout;
    ImageView errorImage;
    TextView errorTitle, errorMessage;
    Button btnRetry;
    LottieAnimationView lottie;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cost);
        TextView tv = findViewById(R.id.productName);
        recyclerView = findViewById(R.id.recyclerView_restaurant);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        lottie = findViewById(R.id.lottie);
        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRetry);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        tv.setText(name);

        resetSpeechRecognizer();
        setRecogniserIntent();

        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

            String text = "searching " + name + ", in amazon ";

            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            getRetrofit(name);

        });

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


    private void getRetrofit(String productName) {
        errorLayout.setVisibility(View.GONE);

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
        Call<List<Cost>> call = service.getCost(productName);


        call.enqueue(new Callback<List<Cost>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Cost>> call, @NonNull Response<List<Cost>> response) {

                assert response.body() != null;
                if (!response.body().isEmpty()) {
                    lottie.cancelAnimation();
                    lottie.setVisibility(View.GONE);

                    textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

                        String text = "The Minimum cost product is " + response.body().get(0).getTitle() + "for rupees " + response.body().get(0).getCash() + " And  The Maximum cost product is" + response.body().get(1).getTitle() + "for rupees " + response.body().get(1).getCash()+ "And for navigation say exit" ;
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    });

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

                    if (!productCost.isEmpty()) {
                        productCost.clear();
                    }

                    for (int i = 0; i < response.body().size(); i++) {

                        productCost.add(new Cost(response.body().get(i).getTitle(), response.body().get(i).getImg(), "Rs" + response.body().get(i).getCash(), response.body().get(i).getBookNow()

                        ));
                    }


                    costAdapter = new CostAdapter(productCost, getApplicationContext());
                    recyclerView.setAdapter(costAdapter);
                    costAdapter.notifyDataSetChanged();
                    initListener();


                } else {

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

                    showErrorMessage(R.drawable.no_result, "No Result", "Please Try Again!\n" + errorCode);

                }
            }


            @Override
            public void onFailure(@NonNull Call<List<Cost>> call, Throwable t) {

                showErrorMessage(R.drawable.no_result, "Oops..", t.toString());

            }
        });


    }


    private void showErrorMessage(int imageView, String title, String message) {

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


    private void initListener() {


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

    }

    @Override
    protected void onPause() {
        super.onPause();
        textToSpeech.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();

        textToSpeech.stop();

    }
}