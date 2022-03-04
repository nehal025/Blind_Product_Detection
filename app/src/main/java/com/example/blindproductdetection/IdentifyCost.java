package com.example.blindproductdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class IdentifyCost extends AppCompatActivity implements RecognitionListener {

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_cost);

        resetSpeechRecognizer();
        setRecogniserIntent();
        welcomeSpeech();

    }

   public void welcomeSpeech(){

       textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

           String text = "Say any product name for cost identification ,or say exit for going back to home screen";

           textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);

       });


       final Handler h =new Handler();
       Runnable r = new Runnable() {

           public void run() {

               if (!textToSpeech.isSpeaking()) {

                   speech.startListening(recognizerIntent);

                   return;
               }

               h.postDelayed(this, 500);
           }
       };

       h.postDelayed(r, 500);
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
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (!matches.contains("exit")) {

            Intent myIntent = new Intent(IdentifyCost.this, DisplayCost.class);
            myIntent.putExtra("name", matches.get(0));
            IdentifyCost.this.startActivity(myIntent);

        }

        finish();

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
}