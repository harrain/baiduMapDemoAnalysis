package baidumapsdk.demo.remould.locate;

import android.app.Activity;
import android.os.Bundle;

import baidumapsdk.demo.R;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 */
public class LocationDemo extends Activity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_framelayout);

        LocateFragment locateFragment = new LocateFragment();
        getFragmentManager().beginTransaction().add(R.id.simple_fragment_container,locateFragment).commit();

    }




}
