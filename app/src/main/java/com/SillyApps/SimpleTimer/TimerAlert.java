package com.SillyApps.SimpleTimer;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class TimerAlert extends Activity {
    private Ringtone mCurrentRingtone;
    private Uri mRingtoneUri;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timeralert);

        //code to unlock the screen and bring the activity on top
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final TextView tvDuration = (TextView) this.findViewById(R.id.tvDuration);
        final Button btnOk = (Button) this.findViewById(R.id.btnok);

        long duration = getIntent().getLongExtra("duration", 0);

        String d = duration + " minutes";
        tvDuration.setText(d);

        mRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Shut off current Ringtone and play new one
    private void playRingtone(Ringtone newRingtone) {
        if (null != mCurrentRingtone && mCurrentRingtone.isPlaying())
            mCurrentRingtone.stop();

        mCurrentRingtone = newRingtone;

        if (null != newRingtone) {
            mCurrentRingtone.play();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        playRingtone(RingtoneManager.getRingtone(
                getApplicationContext(), mRingtoneUri));

    }

    @Override
    protected void onPause() {
        playRingtone(null);
        super.onPause();
    }

}