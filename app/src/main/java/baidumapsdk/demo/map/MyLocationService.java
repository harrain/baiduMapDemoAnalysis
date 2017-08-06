package baidumapsdk.demo.map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyLocationService extends Service {

    private static String TAG = "MyLocationService";
    private LocationRequest lr;

    public MyLocationService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        lr = new LocationRequest(getApplicationContext());
        lr.startLocate(new LocationRequest.LocationListener() {
            @Override
            public void onLocate(String latitude, String longtitude) {
                Log.i(TAG,"经"+longtitude+"纬"+latitude);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG,"onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i(TAG,"onTrimMemory---"+level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG,"onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lr.releaseLocate();
        getApplication().sendBroadcast(new Intent("baiduMap.MyLocationService"));
    }
}
