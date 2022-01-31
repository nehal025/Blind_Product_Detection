package com.example.blindproductdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blindproductdetection.ml.Model;
import com.example.blindproductdetection.utils.Global;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ProductClassifier extends AppCompatActivity  implements RecognitionListener {

    ImageView imageView;
    ImageView classify;
    ImageView retake;
    Bitmap bitmap = Global.img;
    int imageSize = 224;
    List<String> labels = new ArrayList<>();


    TextToSpeech t;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_classifier);
        imageView = findViewById(R.id.photo);
        classify = findViewById(R.id.classify);
        retake = findViewById(R.id.retake);

        int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
        imageView.setImageBitmap(bitmap);

        classify.setOnClickListener(v -> {
            bitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false);
            classifyImage(bitmap);

        });

        retake.setOnClickListener(v -> {
            Intent myIntent = new Intent(ProductClassifier.this, Camera.class);
            ProductClassifier.this.startActivity(myIntent);
            finish();
        });

        try {
            loadLabels();
        } catch (IOException e) {
            e.printStackTrace();
        }

        resetSpeechRecognizer();
        setRecogniserIntent();
        t = new TextToSpeech(getApplicationContext(), i -> {

            String text = "Image successfully captured,Say classify for product identification  and ,Say retake for retaking image ";

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

                    speech.startListening(recognizerIntent);
//                        Toast.makeText(getBaseContext(), "TTS Completed", Toast.LENGTH_SHORT).show();


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
    @SuppressLint("DefaultLocale")
    public void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;
            StringBuilder confidence = new StringBuilder();


            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            for (int i = 0; i < labels.size(); i++) {

                confidence.append(String.format("%s: %.1f%%\n", labels.get(i), confidences[i] * 100));
            }

            try {
                Log.i("String", confidence.toString());
            } catch (Exception ignored) {

            }

            Intent myIntent = new Intent(ProductClassifier.this, DisplayProduct.class);
            myIntent.putExtra("location", labels.get(maxPos));
            myIntent.putExtra("confidence", confidence.toString());
            ProductClassifier.this.startActivity(myIntent);
            model.close();
            finish();

        } catch (IOException ignored) {

        }
    }

    public void loadLabels() throws IOException {
        AssetManager manager;
        String line;
        manager = getAssets();
        InputStream is = manager.open("labels.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br  = new BufferedReader(isr);
        try {
            while ((line = br.readLine()) != null) {
                labels.add(line) ;
            }
        }catch (IOException e1) {
            Toast.makeText(getBaseContext(), "Problem!", Toast.LENGTH_SHORT).show();
        }finally{
            br.close();
            isr.close();
            is.close();
        }
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
        String text = "";

        for (String result : matches)

            text += result + "\n";
        if (text.contains("classify")) {

            classify.performClick();

        }
        if (text.contains("retake")) {
            retake.performClick();


        }
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}