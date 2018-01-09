package baidumapsdk.demo.remould.routeplan;

import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by net on 2017/12/29.
 */

public class SimpleOnGetRoutePlanResult implements OnGetRoutePlanResultListener {

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        onSimpleGetWalkingRouteResult(walkingRouteResult);
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        onSimpleGetTransitRouteResult(transitRouteResult);
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
        onSimpleGetMassTransitRouteResult(massTransitRouteResult);
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        onSimpleGetDrivingRouteResult(drivingRouteResult);
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
        onSimpleGetIndoorRouteResult(indoorRouteResult);
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        onSimpleGetBikingRouteResult(bikingRouteResult);
    }

    public void onSimpleGetWalkingRouteResult(WalkingRouteResult walkingRouteResult){}

    public void onSimpleGetTransitRouteResult(TransitRouteResult transitRouteResult){}

    public void onSimpleGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult){}

    public void onSimpleGetDrivingRouteResult(DrivingRouteResult drivingRouteResult){}

    public void onSimpleGetIndoorRouteResult(IndoorRouteResult indoorRouteResult){}

    public void onSimpleGetBikingRouteResult(BikingRouteResult bikingRouteResult){}
}
