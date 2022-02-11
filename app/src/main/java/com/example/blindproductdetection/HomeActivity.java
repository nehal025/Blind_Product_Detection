package com.example.blindproductdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity implements RecognitionListener {

    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    TextToSpeech t;
    Boolean flag=true;

    Button cost,product;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cost=findViewById(R.id.cost);
        product=findViewById(R.id.Product);

        product.setOnClickListener(v -> {
            Intent myIntent = new Intent(HomeActivity.this, Camera.class);
            HomeActivity.this.startActivity(myIntent);
        });

        cost.setOnClickListener(v -> {
            Intent myIntent = new Intent(HomeActivity.this, IdentifyCost.class);
            HomeActivity.this.startActivity(myIntent);
        });

        resetSpeechRecognizer();
        setRecogniserIntent();


        welcomeSpeech();


    }

    public void welcomeSpeech() {

        t = new TextToSpeech(getApplicationContext(), i -> {

            String text = "Welcome to blind product detection say cost for cost identification  , say product for product identification ,and say exit for closing app ";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

        });



        final Handler h = new Handler();
        Runnable r = new Runnable() {

            public void run() {

                if (!t.isSpeaking()) {
                    if (allPermissionsGranted()) {

                        speech.startListening(recognizerIntent);
//                        Toast.makeText(getBaseContext(), "TTS Completed", Toast.LENGTH_SHORT).show();

                    } else {



                        if(flag){
                            ActivityCompat.requestPermissions(HomeActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

                        }
                        flag=false;
                        t.speak("you are starting app first time so, please take anyone's help to allow ,all  Permissions ", TextToSpeech.QUEUE_FLUSH, null, null);

                    }
                    return;
                }

                h.postDelayed(this, 100);
            }
        };

        h.postDelayed(r, 100);



    }


    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
        speech.stopListening();
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
        if (matches.contains("cost")) {

            Intent myIntent = new Intent(HomeActivity.this, IdentifyCost.class);
            HomeActivity.this.startActivity(myIntent);

        }
        if (matches.contains("product")) {

            Intent myIntent = new Intent(HomeActivity.this, Camera.class);
            HomeActivity.this.startActivity(myIntent);

        }
        if (matches.contains("exit")) {

            finish();

        }
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setRecogniserIntent();
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }


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


}