package baidumapsdk.demo.remould.overlay;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.Constants;
import baidumapsdk.demo.remould.track.TrackAnalysisInfoLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarkShowFragment extends Fragment implements OnGetGeoCoderResultListener {


    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;

    private List<LatLng> mPolylines;
    private PolylineOptions polylineOptions;
    private boolean isDrawed = false;
    private boolean isDrawedStart = false;
    private boolean isDrawedEnd = false;

    private BitmapDescriptor qw;
    private BitmapDescriptor qx;
    private final String tag = "TrackShowDemo";

    private Context mContext;

    private Calendar calendar;
    private SimpleDateFormat sdf;

    List<OverlayOptions> markeroptions;
    List<Marker> markers;

    /**
     * 详情框布局
     */
    private TrackAnalysisInfoLayout trackAnalysisInfoLayout = null;

    volatile boolean mapMatch = true;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private Marker mClickMarker = null;

    private int verticalMargin = -40;
    private Marker mMarkerS;
    private Marker mMarkerE;

    public MarkShowFragment() {
    }

    public static MarkShowFragment create(ArrayList<LatLng> points) {
        MarkShowFragment fragment = new MarkShowFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("points", points);
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

        mContext = getContext();


        mPolylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        markeroptions = new ArrayList<>();
        markers = new ArrayList<>();
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initMap(view);
//        initRadioGroup(view);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        mPolylines.clear();
        mPolylines.addAll(Constants.points);
        try {
            invalidateMapAndTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        getPointsFromArgument();
    }

    private void initMap(View view) {
        mMapView = (MapView) view.findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();

        trackAnalysisInfoLayout = new TrackAnalysisInfoLayout(mContext, mBaiduMap);
//        tracesFileNames = new Link<>();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mBaiduMap.hideInfoWindow();

                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(marker.getPosition()));

                trackAnalysisInfoLayout.titleText.setText("坐标点详情");
                trackAnalysisInfoLayout.key1.setText("纬度");
                trackAnalysisInfoLayout.value1.setText(marker.getPosition().latitude+"");
                trackAnalysisInfoLayout.key2.setText("经度");
                trackAnalysisInfoLayout.value2.setText(marker.getPosition().longitude+"");
                //  保存当前操作的marker
                mClickMarker = marker;

                //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, marker.getPosition(), verticalMargin);
                //显示InfoWindow
                mBaiduMap.showInfoWindow(trackAnalysisInfoWindow);
                return true;
            }
        });

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mBaiduMap.hideInfoWindow();
                }
            }
        });
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

    private void showTrackPointInfo(LatLng ll, String address) {
//        LogUtils.i(tag,"showTrackPointInfo");
        String latitude = String.valueOf(ll.latitude);
        if (latitude.length() > 8) {
            latitude = latitude.substring(0, 8);
        }
        String longitude = String.valueOf(ll.longitude);
        if (longitude.length() > 8) {
            longitude = longitude.substring(0, 8);
        }

        trackAnalysisInfoLayout.titleText.setText("覆盖点详情");
        trackAnalysisInfoLayout.key1.setText("坐标: ");
        trackAnalysisInfoLayout.value1.setText(latitude + "," + longitude);
        trackAnalysisInfoLayout.key2.setText("位置: ");
        if (address != null) {
            trackAnalysisInfoLayout.value2.setText(address);
        }

        InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, ll, verticalMargin);
        mBaiduMap.showInfoWindow(trackAnalysisInfoWindow);
    }


    private void getPointsFromArgument() {
        ArrayList<LatLng> points = getArguments().getParcelableArrayList("points");
        if (points != null && points.size() > 0) {
            mPolylines.clear();
            mPolylines.addAll(points);
        }

        try {
            invalidateMapAndTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setDataAndRefresh(List<LatLng> points) {
        if (points != null && points.size() > 0) {
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
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(mPolylines.get(mPolylines.size() - 1));
        builder.zoom(16.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        mMapView.showZoomControls(false);

        try {

//            if (!isDrawedStart) {
//                drawStart();
//            }
//            if (isDrawedEnd) {
//                drawEnd();
//            }
            drawStart();
            drawEnd();
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
        Log.d(tag, "drawPoints: markeroptions size is "+markeroptions.size());
//        mBaiduMap.addOverlays(markeroptions);
    }


    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//        LogUtils.i(tag,"onGetReverseGeoCodeResult");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未能获得反编码信息", Toast.LENGTH_LONG)
                    .show();
        }
        try {
            showTrackPointInfo(mClickMarker.getPosition(), result.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mBaiduMap.hideInfoWindow();
            clearOverlay();
            mMapView.onDestroy();
            mBaiduMap.clear();
//            TraceControl.getInstance().resetTrackResultListener();

            if (null != trackAnalysisInfoLayout) {
                trackAnalysisInfoLayout = null;
            }

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
