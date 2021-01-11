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
import com.polsl.blindrally.models.RankingList;
import com.polsl.blindrally.models.Track;
import com.polsl.blindrally.utils.TrackBank;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int MIN_DISTANCE = 300;

    private boolean eventFlag = true;
    private boolean isInTurn = true;
    private boolean isInGame = false;

    private float x1, y1; // position variables
    private float beforeTurnPos, afterTurnPos;

    int trackNo = 0, exTime, clickedTime;
    String trackName = "";

    private List<Track> tracks = new ArrayList<>();
    private TextToSpeech mTTS;
    private SensorEvent sensorEvent;
    private Button mButtonSpeak;
    private RankingList rankingList;
    private final TrackBank trackBank = new TrackBank();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tracks = trackBank.getTracks(MainActivity.this);

        Log.d(TAG, "onCreate: Initializing Sensor Services");

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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
                    speak("Welcome to Blind Rally. Swipe left to choose tracks. Swipe up to read ranking for chosen track. Double tap to start game after choosing track.");
                }
            }
        });
        mButtonSpeak.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isInTurn = !isInTurn;
                if (!isInGame) {
                    eventFlag = true;
                    gestureDetector.onTouchEvent(event);
                    onTouchResponse(event);
                }

                return true;
            }

            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    isInGame = true;
                    speak("The game will start in 3... 2... 1...");
                    eventFlag = false;
                    gameAlgorithm();

                    return false;
                }
            });
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, " X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
        sensorEvent = event;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void gameAlgorithm() {
        Object lock = new Object();
        synchronized (lock) {
            Track inGameTrack = tracks.get(trackNo);
            int speed = 50;
            int i = 0;
            while (i < inGameTrack.getTurnList().size()) {
                float time = inGameTrack.getTurnList().get(i).getDistance() / speed * 500;
                if (inGameTrack.getTurnList().get(i).getAngle() > 0)
                    speak(Float.toString(time) + "." + Math.abs(inGameTrack.getTurnList().get(i).getAngle()) + "  degrees left.");
                else
                    speak(Float.toString(time) + "." + Math.abs(inGameTrack.getTurnList().get(i).getAngle()) + "  degrees right.");

                try {
                    lock.wait((long) time);
                    speak("... now");
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                speak(Integer.toString(Math.round(sensorEvent.values[0] * 100)) + " degrees.");

                i++;
            }
        }
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

                    if (Math.abs(deltaX) < MIN_DISTANCE && Math.abs(deltaY) > MIN_DISTANCE) {
                        speak("Players ranking");
                        rankingList = ranking.showRanking(MainActivity.this, trackName);
                        speak(rankingList.getTrackName());
                        speakRanking(rankingList);
                    }

                    if (-deltaX > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE) {
                        trackName = tracks.get(trackNo).getTrackName();
                        speak(trackName);

                        trackNo++;
                        if (trackNo > tracks.size() - 1) {
                            trackNo = 0;
                        } else if (trackNo < 0) {
                            trackNo = tracks.size() - 1;
                        }
                    }
                    break;
            }
        }
    }

    private void speak(String text) {
        mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    private void speakRanking(RankingList ranking) {
        List<RankPosition> ranks = ranking.getRanks();
        for (int i = 0; i < ranks.size(); i++) {
            mTTS.speak(String.valueOf(ranks.get(i).getPosition()) + ranks.get(i).getName() + ranks.get(i).getTime(), TextToSpeech.QUEUE_ADD, null);
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