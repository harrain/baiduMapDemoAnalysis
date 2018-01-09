package baidumapsdk.demo.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * function: convert other-type latlng to baidu type 坐标转换
 */

public class LatlngConverterHelper {

    private CoordinateConverter converter;

    /**
     * @param coordType GPS or COMMON
     */
    public LatlngConverterHelper(CoordinateConverter.CoordType coordType){
        converter = new CoordinateConverter();
        converter.from(coordType);
    }

    /**
     * @param latLng
     */
    public LatLng convertLatlng(LatLng latLng){
        return converter.coord(latLng).convert();
    }
}
