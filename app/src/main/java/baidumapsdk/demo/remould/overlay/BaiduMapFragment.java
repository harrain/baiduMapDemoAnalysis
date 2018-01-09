package baidumapsdk.demo.remould.overlay;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.listener.FragmentLifeCycleListener;
import baidumapsdk.demo.widget.TrackAnalysisInfoLayout;

public class BaiduMapFragment extends Fragment implements OnGetGeoCoderResultListener {


    private MapView mapView;
    private BaiduMap baiduMap;
    private TrackAnalysisInfoLayout trackAnalysisInfoLayout;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private Marker mClickMarker = null;
    private int verticalMargin = -40;
    Context mContext;
    private FragmentLifeCycleListener fragmentLifeCycleListener;

    public BaiduMapFragment() {
        // Required empty public constructor
    }

    public static BaiduMapFragment newInstance(String param1, String param2) {
        BaiduMapFragment fragment = new BaiduMapFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_baidu_map, container, false);
        mapView = (MapView) view.findViewById(R.id.baidu_map_view);
        mContext = getActivity();
        baiduMap = mapView.getMap();
        initMap();
        if (fragmentLifeCycleListener!=null) fragmentLifeCycleListener.onCreateView();
        return view;
    }

    private void initMap() {
        mapView.showZoomControls(false);

        trackAnalysisInfoLayout = new TrackAnalysisInfoLayout(mContext, baiduMap);
//        tracesFileNames = new Link<>();
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                baiduMap.hideInfoWindow();

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
                baiduMap.showInfoWindow(trackAnalysisInfoWindow);
                return true;
            }
        });

        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    baiduMap.hideInfoWindow();
                }
            }
        });
    }


    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
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
        baiduMap.showInfoWindow(trackAnalysisInfoWindow);
    }

    public void setFragmentLifeCycleListener(FragmentLifeCycleListener listener){
        fragmentLifeCycleListener = listener;
    }

    public MapView getMapView() {
        return mapView;
    }

    public BaiduMap getBaiduMap() {
        return baiduMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            baiduMap.hideInfoWindow();
            mapView.onDestroy();
            baiduMap.clear();
//            TraceControl.getInstance().resetTrackResultListener();

//            if (null != trackAnalysisInfoLayout) {
//                trackAnalysisInfoLayout = null;
//            }

        } catch (Exception e) {
//            LogUtils.e(tag, e.getMessage());
        }

    }
}
