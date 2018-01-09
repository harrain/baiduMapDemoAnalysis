/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo.remould.overlay;

import android.app.Activity;
import android.os.Bundle;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 * 轨迹运行demo展示
 */
public class TrackShowDemo extends Activity {

    private List<LatLng> polylines;
    private String tag = "TrackShowDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackshow);

        polylines = new ArrayList<>();
        for (int index = 0; index < latlngs.length; index++) {
            polylines.add(latlngs[index]);
        }
        polylines.add(latlngs[0]);

        initMarkerMoveSmoothlyFragment();

    }

    private void initMarkerMoveSmoothlyFragment() {
        MarkerMoveSmoothlyFragment markerMoveSmoothlyFragment = MarkerMoveSmoothlyFragment.create(polylines);
        getFragmentManager().beginTransaction().add(R.id.track_container,markerMoveSmoothlyFragment).commit();
    }


    private static final LatLng[] latlngs = new LatLng[]{
            new LatLng(40.055826, 116.307917),
            new LatLng(40.055916, 116.308455),
            new LatLng(40.055967, 116.308549),
            new LatLng(40.056014, 116.308574),
            new LatLng(40.056440, 116.308485),
            new LatLng(40.056816, 116.308352),
            new LatLng(40.057997, 116.307725),
            new LatLng(40.058022, 116.307693),
            new LatLng(40.058029, 116.307590),
            new LatLng(40.057913, 116.307119),
            new LatLng(40.057850, 116.306945),
            new LatLng(40.057756, 116.306915),
            new LatLng(40.057225, 116.307164),
            new LatLng(40.056134, 116.307546),
            new LatLng(40.055879, 116.307636),
            new LatLng(40.055826, 116.307697),
    };

}

