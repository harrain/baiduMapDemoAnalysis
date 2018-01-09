package baidumapsdk.demo.remould.overlay;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.listener.FragmentLifeCycleListener;

/**
 * Created by net on 2018/1/9.
 */

public class MarkerMoveSmoothlyFragment extends Fragment {

    private BaiduMap mBaiduMap;
    private Polyline mPolyline;
    private Marker mMoveMarker;
    private Handler mHandler;


    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE = 0.00002;
    private List<LatLng> polylines;
    private FragmentLifeCycleListener fragmentLifeCycleListener;

    public static MarkerMoveSmoothlyFragment create(List<LatLng> points) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        latLngs.addAll(points);
        MarkerMoveSmoothlyFragment fragment = new MarkerMoveSmoothlyFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("points", latLngs);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.simple_framelayout, container, false);
        polylines = new ArrayList<>();
        getPointsFromArgument();

        return view;
    }

    private void getPointsFromArgument() {
        ArrayList<LatLng> points = getArguments().getParcelableArrayList("points");
        if (points == null && points.size() == 0) {
            return;
        }
        polylines.clear();
        polylines.addAll(points);

        initMarkerShowFragment();
    }

    private void initMarkerShowFragment(){
        final MarkerShowFragment markerShowFragment = MarkerShowFragment.create(polylines);
        markerShowFragment.setDefaultMapStatus(false);
        markerShowFragment.setDrawedStart(false);
        markerShowFragment.setDrawedEnd(false);
        getFragmentManager().beginTransaction().add(R.id.simple_fragment_container,markerShowFragment).commit();
        markerShowFragment.setFragmentLifeCycleListener(new FragmentLifeCycleListener() {
            @Override
            public void onCreateView() {
                mBaiduMap = markerShowFragment.getBaiduMap();
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(40.056865, 116.307766));
                builder.zoom(19.0f);
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                mHandler = new Handler(Looper.getMainLooper());
                mPolyline = markerShowFragment.getPolyline();
                drawPolyLine();

                moveLooper();
            }
        });
    }

    private void drawPolyLine() {

        OverlayOptions markerOptions;
        markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)).position(polylines.get(0))
                .rotate((float) getAngle(0));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mPolyline.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mPolyline.getPoints().get(startIndex);
        LatLng endPoint = mPolyline.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率 = 纬度差与经度差的比率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));//纬度差与经度差的比率
        return slope;

    }



    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
        new Thread() {

            public void run() {

                while (true) {

                    for (int i = 0; i < polylines.size() - 1; i++) {


                        final LatLng startPoint = polylines.get(i);
                        final LatLng endPoint = polylines.get(i + 1);
                        mMoveMarker
                                .setPosition(startPoint);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // refresh marker's rotate
//                                if (mMapView == null) {
//                                    return;
//                                }
                                mMoveMarker.setRotate((float) getAngle(startPoint,
                                        endPoint));
                            }
                        });
                        double slope = getSlope(startPoint, endPoint);
                        // 是不是正向的标示
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

                        double intercept = getInterception(slope, startPoint);

                        double xMoveDistance = isReverse ? getXMoveDistance(slope) :
                                -1 * getXMoveDistance(slope);


                        for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
                             j = j - xMoveDistance) {
                            LatLng latLng = null;
                            if (slope == Double.MAX_VALUE) {
                                latLng = new LatLng(j, startPoint.longitude);
                            } else {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            }

                            final LatLng finalLatLng = latLng;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
//                                    if (mMapView == null) {
//                                        return;
//                                    }
                                    mMoveMarker.setPosition(finalLatLng);
                                }
                            });
                            try {
                                Thread.sleep(TIME_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }

        }.start();
    }


}
