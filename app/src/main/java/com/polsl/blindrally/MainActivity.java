package com.polsl.blindrally;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.polsl.blindrally.models.RankPosition;
import com.polsl.blindrally.models.RankingList;
import com.polsl.blindrally.models.Track;
import com.polsl.blindrally.models.Turn;
import com.polsl.blindrally.utils.GenerateUtils;
import com.polsl.blindrally.utils.TrackBank;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int MIN_DISTANCE = 300;

    private boolean eventFlag = true;
    private boolean firstTurnFlag = true;
    private boolean isInTurn = true;
    private boolean isInGame = false;

    private float x1, y1, diff = 0; // position variables

    private int turnCount = 0;
    private int trackNo = 0;
    private int inGamePoints = 0;
    private int perfect = 0, vgood = 0, good = 0, bad = 0, vbad = 0;
    private int turnSize;
    private Instant startTurn;
    private String trackName = "";

    private List<Track> tracks = new ArrayList<>();
    private TextToSpeech mTTS;
    private ImageView imageView;
    private final TrackBank trackBank = new TrackBank();
    private final Ranking ranking = new Ranking();
    private Track inGameTrack = new Track();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tracks = trackBank.getTracks(MainActivity.this);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        imageView = findViewById(R.id.imageView);
        mTTS = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTTS.setLanguage(Locale.ENGLISH);
                mTTS.setSpeechRate((float) 0.8);

                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    imageView.setEnabled(true);
                }
                speak("Welcome to Blind Rally. Swipe left to choose tracks. Swipe up to read ranking for chosen track. Double tap to start game after choosing track.");
            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
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

            private final GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    isInGame = true;
                    speak("The game will start in 3... 2... 1... ");
                    eventFlag = false;
                    generateTrack();

                    return false;
                }
            });
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, " X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

        if (isInGame) {
            if (firstTurnFlag) {
                diff = timeToNextTurn();
                firstTurnFlag = !firstTurnFlag;
                startTurn = Instant.now();
            }
            if ((Duration.between(startTurn, Instant.now()).toMillis() / 1000) > diff) {
                gameAlgorithm(event);
                if (isInGame)
                    diff = timeToNextTurn();
                startTurn = Instant.now();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void onTouchResponse(MotionEvent event) {

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

                    if (Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE && !trackName.equals("")) {
                        if (trackName.equals("generatedTrack")) {
                            speak("There is no raking for autogenerated track.");
                        } else {
                            speak("Players ranking");
                            RankingList rankingList = ranking.showRanking(MainActivity.this, trackName);
                            speak(rankingList.getTrackName());
                            speakRanking(rankingList);
                        }
                    }

                    if (-deltaX < MIN_DISTANCE && Math.abs(deltaY) > MIN_DISTANCE) {
                        trackNo++;
                        if (trackNo > tracks.size()) {
                            trackNo = 0;
                        } else if (trackNo < 0) {
                            trackNo = tracks.size();
                        }
                        if (trackNo != tracks.size()) {
                            trackName = tracks.get(trackNo).getTrackName();
                            speak(trackName);
                        } else {
                            trackName = "generatedTrack";
                            speak("Generated 10 turn track.");
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
            mTTS.speak((i + 1) + "." + ranks.get(i).getName() + ranks.get(i).getScore() + " points.", TextToSpeech.QUEUE_ADD, null);
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

    private void gameAlgorithm(SensorEvent event) {
        speak(" now! ");
        inGamePoints += setPoints(event, inGameTrack.getTurnList().get(turnCount).getAngle() * (-1));

        if (turnCount == turnSize - 1)
            gameSummary(inGamePoints, inGameTrack.getTurnList().size());
        else
            turnCount++;
    }

    private float timeToNextTurn() {
        int speed = 50;
        if (trackNo == tracks.size()) {
            turnSize = 10;
        } else {
            inGameTrack = tracks.get(trackNo);
            turnSize = inGameTrack.getTurnList().size();
        }
        ImageView iw = findViewById(R.id.imageView);
        InputStream ims;
        Drawable drawable;
        Turn nextTurn = inGameTrack.getTurnList().get(turnCount);

        float diff = nextTurn.getDistance() / speed / 2;

        try {
            if (nextTurn.getAngle() > 45) {
                ims = getAssets().open("sharpLeft.png");
                drawable = Drawable.createFromStream(ims, null);
                iw.setImageDrawable(drawable);
            } else if (nextTurn.getAngle() > 0 && nextTurn.getAngle() <= 45) {
                ims = getAssets().open("slightLeft.png");
                drawable = Drawable.createFromStream(ims, null);
                iw.setImageDrawable(drawable);
            } else if (nextTurn.getAngle() < -45) {
                ims = getAssets().open("sharpRight.png");
                drawable = Drawable.createFromStream(ims, null);
                iw.setImageDrawable(drawable);
            } else if (nextTurn.getAngle() < 0 && nextTurn.getAngle() >= -45) {
                ims = getAssets().open("slightRight.png");
                drawable = Drawable.createFromStream(ims, null);
                iw.setImageDrawable(drawable);
            } else {
                ims = getAssets().open("straight.png");
                drawable = Drawable.createFromStream(ims, null);
                iw.setImageDrawable(drawable);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String temp;
        if (inGameTrack.getTurnList().get(turnCount).getAngle() > 0) {
            temp = " left";
        } else {
            temp = " right";
        }

        speak(Math.abs(inGameTrack.getTurnList().get(turnCount).getAngle()) + temp + ". " + diff + " seconds. ");

        return diff;
    }

    private int setPoints(SensorEvent sensorEvent, int angle) {
        float turnDiff = Math.abs(sensorEvent.values[1] * 9 - angle);
        if (turnDiff < 10) {
            perfect++;
            return 10;
        } else if (turnDiff < 15 && turnDiff >= 10) {
            vgood++;
            return 8;
        } else if (turnDiff < 20 && turnDiff >= 15) {
            good++;
            return 6;
        } else if (turnDiff < 25 && turnDiff >= 20) {
            bad++;
            return 4;
        } else if (turnDiff < 30 && turnDiff >= 25) {
            vbad++;
            return 2;
        } else {
            return 0;
        }
    }

    private void generateTrack() {
        List<Turn> turns = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Turn turn = new Turn();
            int rnd = new Random().nextInt(GenerateUtils.turnArray.length);
            turn.setAngle(GenerateUtils.turnArray[rnd]);
            int dist = new Random().nextInt(GenerateUtils.distanceArray.length);
            turn.setDistance(GenerateUtils.distanceArray[dist]);
            turns.add(turn);
        }
        inGameTrack = new Track();
        inGameTrack.setTurnList(turns);
    }

    private void gameSummary(int points, int size) {
        speak("Possible points: " + size * 10);
        speak("Points collected: " + points);
        speak("Perfect turns: " + perfect);
        speak("Very good turns: " + vgood);
        speak("Good turns: " + good);
        speak("Bad turns: " + bad);
        speak("Very bad turns: " + vbad);

        perfect = 0;
        good = 0;
        bad = 0;
        vbad = 0;

        isInGame = false;
        inGamePoints = 0;
        turnCount = 0;

        ImageView iw = findViewById(R.id.imageView);
        iw.setImageDrawable(null);
    }
}