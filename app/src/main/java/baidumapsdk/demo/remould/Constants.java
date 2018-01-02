package baidumapsdk.demo.remould;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public final class Constants {

    public static final String TAG = "BaiduTraceSDK_V3";

    public static final int REQUEST_CODE = 1;

    public static final int RESULT_CODE = 1;

    public static final int DEFAULT_RADIUS_THRESHOLD = 100;

    public static final int PAGE_SIZE = 1000;

    /**
     * 默认采集周期
     */
    public static final int DEFAULT_GATHER_INTERVAL = 300;//秒

    /**
     * 默认打包周期
     */
    public static final int DEFAULT_PACK_INTERVAL = 300;//秒

    /**
     * 实时定位间隔(单位:秒)
     */
    public static final int LOC_INTERVAL = 5;

    /**
     * 最后一次定位信息
     */
    public static final String LAST_LOCATION = "last_location";

    /**
     * 停留点默认停留时间（1分钟）
     */
    public static final int STAY_TIME = 600;

    public static List<LatLng> points = new ArrayList<>();

}
