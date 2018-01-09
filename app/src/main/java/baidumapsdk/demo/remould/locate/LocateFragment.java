package baidumapsdk.demo.remould.locate;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.listener.FragmentLifeCycleListener;
import baidumapsdk.demo.remould.overlay.MarkerShowFragment;
import baidumapsdk.demo.util.LocationRequest;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
/**
 * Created by net on 2018/1/9.
 */

public class LocateFragment extends Fragment implements SensorEventListener {

    // 定位相关
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;//定位光圈填充色
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;//定位光圈边线色
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;//方向信息，顺时针0-360
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;//测量精度，用于定位图标的光圈显示，0则无光圈。

    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;

    public LocateFragment() {
    }

    public static LocateFragment create(List<LatLng> points) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        latLngs.addAll(points);
        LocateFragment fragment = new LocateFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("points", latLngs);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        requestLocButton = (Button) view.findViewById(R.id.button1);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");

        initMarkerShowFragment(view);
        return view;
    }

    private void initMarkerShowFragment(final View view){
        final MarkerShowFragment markerShowFragment = new MarkerShowFragment();
        markerShowFragment.setDefaultMapStatus(false);
        markerShowFragment.setDrawedStart(false);
        markerShowFragment.setDrawedEnd(false);
        getFragmentManager().beginTransaction().add(R.id.bmapView_locate_fl,markerShowFragment).commit();
        markerShowFragment.setFragmentLifeCycleListener(new FragmentLifeCycleListener() {
            @Override
            public void onCreateView() {
                mBaiduMap = markerShowFragment.getBaiduMap();
                mMapView = markerShowFragment.getMapView();
                initListener(view);
                initLocate();
            }
        });
    }

    private void initListener(View view){
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);//俯仰角
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroup);
        radioButtonListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentMarker = null;
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_geo);
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker,
                            accuracyCircleFillColor, accuracyCircleStrokeColor));//第二个参数 是否允许显示方向信息
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);
    }

    private void initLocate(){
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        LocationRequest.getInstance(getActivity()).startLocate(new LocationRequest.LocationInfoListener() {
            @Override
            public void onLocationInfo(double lat, double lng, float radius) {
                if (mMapView == null)return;
                mCurrentLat = lat;
                mCurrentLon = lng;
                mCurrentAccracy = radius;
                locData = new MyLocationData.Builder()
                        .accuracy(radius)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentDirection).latitude(lat)
                        .longitude(lng).build();
                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(lat,
                            lng);//经纬度容器.支持写parcel
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(20.0f);//target 设置地图中心点（会显示位置图标） ； zoom 设置缩放级别
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//animateMapStatus 以动画方式更新地图状态，动画耗时 300 ms
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];//android手机传感器返回的x坐标
        if (Math.abs(x - lastX) > 1.0) { //差的绝对值 > 1 ，修正方向  (x的值为 0 - 360)。
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            if (mBaiduMap!=null)mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 退出时销毁定位
        LocationRequest.getInstance(getActivity()).releaseLocate();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);

    }
}
