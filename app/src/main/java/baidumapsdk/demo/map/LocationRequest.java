package baidumapsdk.demo.map;

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
    private LocationListener mListener;

    public LocationRequest(Context context){
        mContext = context;
    }

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private String mCurrentLat;
    private String mCurrentLon;
    private String mCurrentAccracy;

    public void startLocate(LocationListener locationListener){

        mListener = locationListener;

        mLocClient = new LocationClient(mContext);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
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
            mListener.onLocate(mCurrentLat,mCurrentLon);

        }

        @Override
        public void onConnectHotSpotMessage(String var1, int var2){}
    }

    public void releaseLocate(){
        // 退出时销毁定位
        mLocClient.stop();
    }

    public interface LocationListener{
        void onLocate(String latitude, String longtitude);
    }
}
