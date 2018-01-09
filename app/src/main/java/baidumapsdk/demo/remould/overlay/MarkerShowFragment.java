package baidumapsdk.demo.remould.overlay;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.Constants;
import baidumapsdk.demo.remould.listener.FragmentLifeCycleListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarkerShowFragment extends Fragment {


    private FrameLayout mMapViewFL;

    private BaiduMap mBaiduMap;
    private Polyline mPolyline;

    private List<LatLng> mPolylines;
    private PolylineOptions polylineOptions;
    private boolean isDefaultMapStatus = true;
    private boolean isDrawedStart = true;
    private boolean isDrawedEnd = true;

    private BitmapDescriptor qw;
    private BitmapDescriptor qx;
    private final String tag = "TrackShowDemo";

    private Context mContext;


    List<OverlayOptions> markeroptions;
    List<Marker> markers;


    volatile boolean mapMatch = true;
    private Marker mClickMarker = null;

    private Marker mMarkerS;
    private Marker mMarkerE;
    private FragmentLifeCycleListener fragmentLifeCycleListener;
    private BaiduMapFragment baiduMapFragment;
    private MapView mapView;

    public MarkerShowFragment() {
    }

    public static MarkerShowFragment create(List<LatLng> points) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        latLngs.addAll(points);
        MarkerShowFragment fragment = new MarkerShowFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("points", latLngs);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_show, container, false);
        init(view);

        return view;
    }

    private void init(View view) {

        mContext = getActivity();


        mPolylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        markeroptions = new ArrayList<>();
        markers = new ArrayList<>();
        initMap(view);
//        initRadioGroup(view);

    }

    private void initMap(View view) {

        mMapViewFL = (FrameLayout) view.findViewById(R.id.bmapView);
        baiduMapFragment = new BaiduMapFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.bmapViewfl, baiduMapFragment).commit();
        baiduMapFragment.setFragmentLifeCycleListener(new FragmentLifeCycleListener() {
            @Override
            public void onCreateView() {

                drawOverlay();
            }
        });

//        tracesFileNames = new Link<>();

    }

    private void drawOverlay(){
        mBaiduMap = baiduMapFragment.getBaiduMap();
        mapView = baiduMapFragment.getMapView();
        getPointsFromArgument();
        getPointsFromConstants();
        if (fragmentLifeCycleListener!=null) fragmentLifeCycleListener.onCreateView();
    }

    private void initRadioGroup(View view) {
        RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroup);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.raw_rb) {
                    mapMatch = false;
                    mBaiduMap.clear();
                    isDrawedStart = false;
//                    obtainLatlngFromEagleEye();
                }
                if (checkedId == R.id.match_rb) {
                    mapMatch = true;
                    mBaiduMap.clear();
//                    obtainLatlngFromEagleEye();
                }
            }
        });
    }


    private void getPointsFromArgument() {
        if (getArguments() == null) return;
        ArrayList<LatLng> points = getArguments().getParcelableArrayList("points");
        if (points == null && points.size() == 0) {
            return;
        }
        mPolylines.clear();
        mPolylines.addAll(points);

        try {
            invalidateMapAndTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPointsFromConstants() {
        if (Constants.points.size() == 0) {
            return;
        }
        mPolylines.clear();
        mPolylines.addAll(Constants.points);
        try {
            invalidateMapAndTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDataAndRefresh(List<LatLng> points) {
        if (points == null && points.size() == 0) {
            if (mPolylines != null) mPolylines.clear();
            if (mPolylines != null) mPolylines.addAll(points);
        }
        try {
            mBaiduMap.clear();
            invalidateMapAndTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void invalidateMapAndTrace() throws Exception {
        if (mPolylines == null || mPolylines.size() == 0) {
//            ToastUtil.showShortToast("无坐标点");
            return;
        }
        Log.d(tag, "invalidateMapAndTrace: " + mPolylines.size());
        if (isDefaultMapStatus) {
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(mPolylines.get(mPolylines.size() - 1));
            builder.zoom(16.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

        try {

            if (isDrawedStart) {
                drawStart();
            }
            if (isDrawedEnd) {
                drawEnd();
            }

            drawPolyLine();
            drawPoints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawStart() {
        qw = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_st);
        MarkerOptions ooA = new MarkerOptions().position(mPolylines.get(0)).icon(qw)
                .zIndex(1);
        mMarkerS = (Marker) (mBaiduMap.addOverlay(ooA));

        isDrawedStart = true;
    }

    private void drawEnd() {
        qx = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_en);
        MarkerOptions ooB = new MarkerOptions().position(mPolylines.get(mPolylines.size() - 1)).icon(qx)
                .zIndex(2);
        mMarkerE = (Marker) (mBaiduMap.addOverlay(ooB));
        isDrawedEnd = false;
    }

    private void drawPolyLine() throws Exception {

        polylineOptions.points(mPolylines).width(10).color(Color.RED);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        mPolyline.setDottedLine(true);
    }

    private void drawPoints() {
        BitmapDescriptor point = BitmapDescriptorFactory.fromResource(R.drawable.reddot);
        for (LatLng latLng : mPolylines) {

            MarkerOptions m = new MarkerOptions().position(latLng).icon(point);
            markeroptions.add(m);
            Marker marker = (Marker) mBaiduMap.addOverlay(m);
        }
        Log.d(tag, "drawPoints: markeroptions size is " + markeroptions.size());
//        mBaiduMap.addOverlays(markeroptions);
    }


    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public void setDrawedStart(boolean drawedStart) {
        isDrawedStart = drawedStart;
    }

    public void setDrawedEnd(boolean drawedEnd) {
        isDrawedEnd = drawedEnd;
    }

    public void setDefaultMapStatus(boolean defaultMapStatus) {
        isDefaultMapStatus = defaultMapStatus;
    }

    public void setFragmentLifeCycleListener(FragmentLifeCycleListener listener){
        fragmentLifeCycleListener = listener;
    }

    public BaiduMap getBaiduMap() {
        return mBaiduMap;
    }

    public Polyline getPolyline() {
        return mPolyline;
    }

    public MapView getMapView(){
        return mapView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            clearOverlay();
//            TraceControl.getInstance().resetTrackResultListener();

        } catch (Exception e) {
//            LogUtils.e(tag, e.getMessage());
        }

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

    private void clearOverlay() {
        mMarkerS = null;
        mMarkerE = null;
        qw.recycle();
        qx.recycle();
    }


}
