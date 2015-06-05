package com.SillyApps.SimpleTimer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    private long[] mVibratePattern = { 0, 200, 200, 300 };

    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;


    public void onReceive(Context context, Intent intent) {
        long duration = intent.getLongExtra("duration", 0);
        Log.i("Alert", "Notified: " + duration);

        Intent mAlertUI = new Intent(context, TimerAlert.class );
        mAlertUI.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        mAlertUI.putExtra("duration", duration);
        context.startActivity(mAlertUI);
    }
}
