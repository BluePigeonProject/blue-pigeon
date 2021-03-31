package com.csg.bluepigeon.ui.foreground;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.csg.bluepigeon.ui.MainActivity;
import com.csg.bluepigeon.R;
import com.csg.bluepigeon.util.BlueChirp;
import com.csg.bluepigeon.util.Config;

import static com.csg.bluepigeon.ui.foreground.NotificationApp.CHANNEL_ID;

/**********************************************
 * This class is a service that is responsible in building the foreground notification
 **********************************************/
public class ForegroundBuilder extends Service {

    private final String TAG = "BP-Foreground";
    private BlueChirp blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        blueChirp.log("Invoked foreground notification", "info");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Battery Optimization is On")
                .setContentText("Battery optimization is running in the background. ")
                .setSmallIcon(R.drawable.ic_baseline_child_care_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    // not useful but required to implement
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
