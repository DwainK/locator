package biz.corestats.locator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import io.flutter.app.FlutterApplication;

public class Locator extends FlutterApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // create channel
            NotificationChannel channel = new NotificationChannel("receiver", "Receiver", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            // register the channel
            manager.createNotificationChannel(channel);
        }

    }
}
