package biz.corestats.locator;


import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

@RequiresApi(api = Build.VERSION_CODES.P)
public class MainActivity extends FlutterActivity {
    
    private final String TAG = this.getClass().getSimpleName();

    private static final String SERVICE_CHANNEL = "biz.corestats.locator/foregroundService";

    private String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS
    };


    private Intent forService;

    private PermissionUtility permissionUtility;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionUtility = new PermissionUtility(this, PERMISSIONS);
        if (permissionUtility.arePermissionsEnabled()) {
            Log.d(TAG, "All permissions are already granted");
        } else {
            permissionUtility.requestMultiplePermissions();
        }

        forService = new Intent(MainActivity.this, SmsService.class);

        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), SERVICE_CHANNEL).setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                if (call.method.equals("startService")) {
                    startService();
                    result.success("Service started");
                }
            }
        });
        

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissionUtility.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "All permissions granted");
        }
    }

    private void startService(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // run service
            startForegroundService(forService);
        } else {
            startService(forService);
        }
    }


}
