package baidumapsdk.demo.util;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by data on 2017/8/3.
 */

public class LocationRequest {

    private Context mContext;
    private LocationFormatListener mListener;
    private LocationInfoListener locationInfoListener;
    private static LocationRequest instance;

    public LocationRequest(Context context){
        mContext = context;
        mLocClient = new LocationClient(mContext);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);//定位时间间隔   //默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocClient.setLocOption(option);
        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocClient.setLocOption(option);
    }

    public LocationRequest(Context context,int millis){
        mContext = context;
        mLocClient = new LocationClient(mContext);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(millis);//定位时间间隔
        mLocClient.setLocOption(option);
        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocClient.setLocOption(option);
    }

    public static LocationRequest getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationRequest.class){
                if (instance == null)instance = new LocationRequest(context);
            }
        }
        return instance;
    }

    /**
     * @param millis 毫秒
     */
    public static LocationRequest getInstance(Context context,int millis) {
        if (instance == null) {
            synchronized (LocationRequest.class){
                if (instance == null)instance = new LocationRequest(context,millis);
            }
        }
        return instance;
    }

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private String mCurrentLat;
    private String mCurrentLon;
    private String mCurrentAccracy;

    public void startLocate(LocationFormatListener locationListener){

        mListener = locationListener;


        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocClient.start();
    }

    public void startLocate(LocationInfoListener locationListener){

        locationInfoListener = locationListener;


        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数, 需实现BDLocationListener里的方法
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null) {
                return;
            }
            mCurrentLat = String.valueOf(location.getLatitude());
            mCurrentLon = String.valueOf(location.getLongitude());
            mCurrentAccracy = String.valueOf(location.getRadius());
            if (mListener!=null)mListener.onLocate(mCurrentLat,mCurrentLon);
            if (locationInfoListener!=null) locationInfoListener.onLocationInfo(location.getLatitude(),location.getLongitude(),location.getRadius());
        }

        @Override
        public void onConnectHotSpotMessage(String var1, int var2){}
    }

    public void releaseLocate(){
        // 退出时销毁定位
        mLocClient.stop();
        mListener = null;
        locationInfoListener = null;
    }

    public interface LocationFormatListener {
        void onLocate(String latitude, String longtitude);
    }

    public interface LocationInfoListener{
        void onLocationInfo(double lat,double lng,float radius);
    }
}
