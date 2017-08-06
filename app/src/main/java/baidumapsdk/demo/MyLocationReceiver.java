package baidumapsdk.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import baidumapsdk.demo.map.MyLocationService;

public class MyLocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("baiduMap.MyLocationService")){
            Log.i("MyLocationReceiver","get broadcast");
            context.startService(new Intent(context, MyLocationService.class));
        }
    }
}
