package biz.corestats.locator;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SmsService extends Service {

    private final String TAG = this.getClass().getSimpleName();


    private DataBroadcastReceiver dataBroadcastReceiver;
    private IntentFilter dataIntentFilter;

    private SMSReceiver mSMSreceiver;
    private IntentFilter mIntentFilter;

    private String messageBody;
    private String msgOriginalAddress;

    private LocationManager mLocationManager;
    private double longitude;
    private double latitude;
    private String cityName;
    private Location myLastLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        // foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "receiver")
                    .setContentText("running")
                    .setContentTitle("Locator")
                    .setSmallIcon(R.drawable.ic_android_black_24dp);

            startForeground(101, builder.build());
        }

        Log.i(TAG, "sms event receiver started");
        //SMS event receiver
        mSMSreceiver = new SMSReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        mIntentFilter.setPriority(2147483647);
        registerReceiver(mSMSreceiver, mIntentFilter);

        Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
        List<ResolveInfo> infos = getPackageManager().queryBroadcastReceivers(intent, 0);
        for (ResolveInfo info : infos) {
            Log.i(TAG, "Receiver name:" + info.activityInfo.name + "; priority=" + info.priority);
        }

        // register broadcast from smsReceiver to Service
        dataBroadcastReceiver = new DataBroadcastReceiver();
        dataIntentFilter = new IntentFilter("ACT_DATA_TRANSFER");
        registerReceiver(dataBroadcastReceiver, dataIntentFilter);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (longitude == 0.0 && latitude == 0.0) {
            myLastLocation = getLastKnownLocation();
            System.out.println(myLastLocation);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the SMS receiver
        unregisterReceiver(mSMSreceiver);
        unregisterReceiver(dataBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class SMSReceiver extends BroadcastReceiver {

        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            String strMsgBody = "";
            String strMessage = ""; //TODO remove not used string
            String strMsgSrc = "";


            if ( extras != null ) {
                Object[] smsextras = (Object[]) extras.get( "pdus" );

                for ( int i = 0; i < smsextras.length; i++ ) {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);

                    strMsgBody = smsmsg.getMessageBody().toString();

                    strMsgSrc = smsmsg.getOriginatingAddress();

                    strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;
                }


            }

            Intent i = new Intent("ACT_DATA_TRANSFER");
            i.putExtra("messageBody",strMsgBody);
            i.putExtra("sender",strMsgSrc);
            Log.d(TAG,"sendBroadcast() message intent : " + strMsgBody + " to Service ");
            sendBroadcast(i);

        }

    }

    public class DataBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("received intent from SmsReceiver");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(Objects.equals(intent.getAction(), "ACT_DATA_TRANSFER")){
                    messageBody = intent.getStringExtra("messageBody");
                    msgOriginalAddress = intent.getStringExtra("sender");
                    System.out.println("Broadcast receiver values: " + messageBody);
                    analyseMessage(messageBody);
                }
            }
        }
    }

    private void analyseMessage(String strMsgBody) {
        if(strMsgBody.contains("Track my phone")){
            getLocation();
            sendSms(msgOriginalAddress,"http://maps.google.com/?q=" + latitude + "," + longitude);
        }
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if(longitude == 0.0 && latitude == 0.0){
            longitude = myLastLocation.getLongitude();
            latitude = myLastLocation.getLatitude();
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                5, new android.location.LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {


                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.v(TAG, "Location retrieved."+ " Latitude: " + latitude + " Longitude: " + longitude);

                /*------- To get city name from coordinates -------- */
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        cityName = addresses.get(0).getLocality();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        });

    }

    public void sendSms(String address, String message){

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(address,null,message,null,null);
        System.out.println("Sms sent to: "+ msgOriginalAddress + " message:  " + message);

    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }


}