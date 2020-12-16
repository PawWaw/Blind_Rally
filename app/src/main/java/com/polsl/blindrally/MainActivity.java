package com.polsl.blindrally;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.polsl.blindrally.models.RankPosition;
import com.polsl.blindrally.models.Turn;
import com.polsl.blindrally.utils.TrackBank;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int MIN_DISTANCE = 300;

    private boolean eventFlag = true;
    float x1;
    float y1;

    private TextToSpeech mTTS;
    private Button mButtonSpeak;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TrackBank trackBank = new TrackBank();
//        List<List<Turn>> tracks = trackBank.getTracks(MainActivity.this);

        Log.d(TAG, "onCreate: Initializing Sensor Services");

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Registered accelerometer listener");

        mButtonSpeak = findViewById(R.id.button_speak);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    mTTS.setSpeechRate((float) 0.8);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                    speak("Welcome to Blind Rally. Swipe right to choose tracks. Swipe up to read ranking for chosen track. Double tap to start game");
                }
            }
        });
        mButtonSpeak.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eventFlag = true;
                gestureDetector.onTouchEvent(event);
                onTouchResponse(event);
                return true;
            }

            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    speak("The game will start in 3... 2... 1...");
                    eventFlag = false;
                    return super.onDoubleTap(e);
                }
            });
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, " X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void onTouchResponse(MotionEvent event) {
        Ranking ranking = new Ranking();

        if (eventFlag) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float x2 = event.getX();
                    float y2 = event.getY();

                    float deltaX = x2 - x1;
                    float deltaY = y2 - y1;

                    if (!(deltaX > MIN_DISTANCE && -deltaY > MIN_DISTANCE)) {
                        speak("Players ranking");
                        speakRanking(ranking.showRanking(MainActivity.this));
                    }

                    if (deltaX > MIN_DISTANCE && deltaY < MIN_DISTANCE) {
                        speak("Track one");
                    }
                    break;
            }
        }
    }

    private void speak(String text) {
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void speakRanking(List<RankPosition> ranking) {
        for (int i = 0; i < ranking.size(); i++) {
            mTTS.speak(String.valueOf(ranking.get(i).getPosition()) + ranking.get(i).getName() + ranking.get(i).getTime(), TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}