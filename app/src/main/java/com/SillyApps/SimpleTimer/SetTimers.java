package com.SillyApps.SimpleTimer;

import android.app.Activity;
import android.app.AlarmManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
//import android.widget.Toast;

public class SetTimers extends Activity implements GestureDetector.OnGestureListener {

    private AlarmManager mAlarmManager;
    private CountDownTimer timer;

    private static final long ONE_MINUTE_IN_MS = 1 * 60 * 1000L;
    private static final long ONE_SECOND_IN_MS = 1000L;
    private static final long INITIAL_DURATION_IN_MINS = 5;

    private static final String KEY_DURATION = "duration";
    private static final String KEY_ENDTIME = "endtime";
    private static final String KEY_TIMERSTARTED = "timerstarted";

    private long mDurationInMinutes = INITIAL_DURATION_IN_MINS;
    private long startTime = 0;
    private long endTime = 0;

    private Button btnTimerStart;
    private TextView tvMinutesLabel;
    private Boolean timerStarted = false;

    private Intent receiverIntent;
    private PendingIntent receiverPendingIndent;

    private GestureDetector gestureDetector;

    private SharedPreferences mPrefs;

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(KEY_DURATION, mDurationInMinutes);

        if(timerStarted) {

            outState.putLong(KEY_ENDTIME, endTime);
            outState.putBoolean(KEY_TIMERSTARTED, timerStarted);

            if(timer != null) {
                Log.i("Alert", "Timer cancelled");
                timer.cancel();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mPrefs = getSharedPreferences("TIMER", MODE_PRIVATE);

        btnTimerStart = (Button) this.findViewById(R.id.btnduration);
        tvMinutesLabel = (TextView) this.findViewById(R.id.tvMinsLabel);

        btnTimerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Alert", "Set Timer for : " + mDurationInMinutes);
                Log.i("Alert", view.toString());
                if(timerStarted == true) {
                    resetAlarm();
                }
                else{
                    setAlarm();
                }
            }
        });

        gestureDetector = new GestureDetector(this,this);

        //if activity was restarted, check whether we have running timer
        if (savedInstanceState != null) {
            mDurationInMinutes = savedInstanceState.getLong(KEY_DURATION);
            timerStarted = savedInstanceState.getBoolean(KEY_TIMERSTARTED);

            updateDuration();
            if(timerStarted) {
                endTime = savedInstanceState.getLong(KEY_ENDTIME);
                if(endTime > System.currentTimeMillis()) {
                    restartTimer();
                }
            }
        } else {
            //if app restarted, check whether timer running when the app was closed?
            if(mPrefs.contains(KEY_DURATION)) {
                mDurationInMinutes = mPrefs.getLong(KEY_DURATION, INITIAL_DURATION_IN_MINS);
            }
            if(mPrefs.contains(KEY_TIMERSTARTED)) {
                timerStarted = mPrefs.getBoolean(KEY_TIMERSTARTED, false);
            }

            if(timerStarted) {
                if(mPrefs.contains(KEY_ENDTIME)) {
                    endTime = mPrefs.getLong(KEY_ENDTIME, 0);
                }

                if(endTime > System.currentTimeMillis()) {
                    restartTimer();
                }
                else {
                    timerStarted = false;
                    updateDuration();
                }

            } else {
                updateDuration();
            }
        }

        updateUIState();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();

        //save last selected duration preference
        ed.putLong(KEY_DURATION, mDurationInMinutes);

        //if timer is running, save state
        if(timerStarted) {
            ed.putLong(KEY_ENDTIME, endTime);
                ed.putBoolean(KEY_TIMERSTARTED, timerStarted);

            if(timer != null) {
                Log.i("Alert", "Timer cancelled");
                timer.cancel();
            }
        } else {
            ed.remove(KEY_TIMERSTARTED);
            ed.remove(KEY_ENDTIME);
        }

        ed.commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(!timerStarted) {
            this.gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void updateDuration() {
//        if(mDurationInMinutes == 1)
//            btnTimerStart.setText(String.format("%d min", mDurationInMinutes));
//        } else {
//            btnTimerStart.setText(String.format("%d mins", mDurationInMinutes));
//        }
        btnTimerStart.setText(String.format("%d", mDurationInMinutes));
        btnTimerStart.setBackgroundColor(Color.rgb(0,150,0));
    }

    public void updateUIState() {
        if(timerStarted) {
            btnTimerStart.setBackgroundColor(Color.rgb(150,0,0));
            tvMinutesLabel.setVisibility(View.INVISIBLE);
        } else {
            btnTimerStart.setBackgroundColor(Color.rgb(0,150,0));
            tvMinutesLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setTimerText() {
        long millis = endTime -  System.currentTimeMillis();
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        btnTimerStart.setText(String.format("%d:%02d", minutes, seconds));
        btnTimerStart.setBackgroundColor(Color.rgb(150,0,0));
    }

    private void resetAlarm() {
        timerStarted = false;

        if(receiverPendingIndent != null) {
            mAlarmManager.cancel(receiverPendingIndent);
            Log.i("Alert", "Alarm cancelled");
        } else {
            Log.i("Alert","NULL: receiver pending indent is null!!!!!");

            receiverIntent = new Intent(SetTimers.this, AlarmNotificationReceiver.class);
            receiverIntent.putExtra("duration", mDurationInMinutes);
            receiverPendingIndent = PendingIntent.getBroadcast(SetTimers.this, 0, receiverIntent, 0);
            Log.i("Alert","new indent created");
            mAlarmManager.cancel(receiverPendingIndent);
            Log.i("Alert","Alarm cancelled");
        }

        if(timer != null) {
            Log.i("Alert", "Timer cancelled");
            timer.cancel();
        }

        updateDuration();
        updateUIState();

//        Toast.makeText(getApplicationContext(),
//                "Timer stopped",
//                Toast.LENGTH_LONG).show();

        Log.i("Alert","Alert Reset");
    }

    private void restartTimer() {
        long durationInMs = mDurationInMinutes * ONE_MINUTE_IN_MS;

        startTime = endTime - durationInMs;

        durationInMs = endTime - System.currentTimeMillis();
        //endTime = startTime + durationInMs;

        setTimerText();

        timer = new CountDownTimer(durationInMs, ONE_SECOND_IN_MS) {
            public void onTick(long millisUntilFinished) {
                Log.i("Alert","Timer tick");
                setTimerText();
            }

            public void onFinish() {
                Log.i("Alert","Timer finished");
                timerStarted = false;
                updateDuration();
            }
        };

        timerStarted = true;
        timer.start();

        updateUIState();

        Log.i("Alert","Timer restarted");
    }

    private void setAlarm() {
        setAlarm(false);
    }

    private void setAlarm(Boolean ignoreAlarm) {
        receiverIntent = new Intent(SetTimers.this, AlarmNotificationReceiver.class);
        receiverIntent.putExtra("duration", mDurationInMinutes);

        receiverPendingIndent = PendingIntent.getBroadcast(SetTimers.this, 0, receiverIntent, 0);

        long durationInMs = mDurationInMinutes * ONE_MINUTE_IN_MS;
        startTime = System.currentTimeMillis() ;
        endTime = startTime + durationInMs;

        if(!ignoreAlarm) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                    endTime,
                    receiverPendingIndent);
        }

        setTimerText();

        timer = new CountDownTimer(durationInMs, ONE_SECOND_IN_MS) {
            public void onTick(long millisUntilFinished) {
                setTimerText();
            }

            public void onFinish() {
                Log.i("Alert","Timer finished");
                timerStarted = false;
                updateDuration();
            }
        };

//        Toast.makeText(getApplicationContext(),
//                "Timer set for " + mDurationInMinutes + " minute(s)" ,
//                Toast.LENGTH_LONG).show();

        timerStarted = true;
        timer.start();

        updateUIState();

        Log.i("Alert","Alert set");
    }

    private long valueOnScrollStart;
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        valueOnScrollStart = mDurationInMinutes;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        return;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        float diff = motionEvent.getY() - motionEvent2.getY();
        int i = Math.abs((int) diff) / 40;

        if(diff < 0) {
            i = -i;
        }

        mDurationInMinutes = valueOnScrollStart + i;
        if(mDurationInMinutes < 1) {
            mDurationInMinutes = 1;
        } else if(mDurationInMinutes > 50) {
            mDurationInMinutes = 50;
        }

        updateDuration();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        return;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }
}
