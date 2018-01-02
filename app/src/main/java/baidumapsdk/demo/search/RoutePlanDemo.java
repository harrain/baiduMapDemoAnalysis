/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package baidumapsdk.demo.search;

import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import baidumapsdk.demo.R;
import baidumapsdk.demo.remould.RoutePlanHelper;

/**
 * 此demo用来展示如何进行驾车、步行、公交、骑行、跨城综合路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class RoutePlanDemo extends Activity implements BaiduMap.OnMapClickListener{

    // 浏览路线节点相关
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    
    RouteLine route = null;
    MassTransitRouteLine massroute = null;//跨城线路
    OverlayManager routeOverlay = null;
    
    boolean useDefaultIcon = false;
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;

    MassTransitRouteResult nowResultmass = null;//跨城交通

    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。

    String startNodeStr = "西二旗地铁站";
    String endNodeStr = "百度科技园";
    private RoutePlanHelper routePlanHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeplan);
        CharSequence titleLable = "路线规划功能";
        setTitle(titleLable);
        // 初始化地图
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听

        /** ----------------------- 路线规划启动代码 ----------------------------- */
        routePlanHelper = new RoutePlanHelper(this);
        routePlanHelper.setMap(mBaidumap);
        routePlanHelper.setOnPreDoListener(new RoutePlanHelper.OnPreDoListener() {
            @Override
            public void preDo() {
                nodeIndex = -1;
                mBtnPre.setVisibility(View.VISIBLE);
                mBtnNext.setVisibility(View.VISIBLE);
            }
        });
        routePlanHelper.setOnObtainRouteObjectListener(new RoutePlanHelper.OnObtainRouteObjectListener() {
            @Override
            public void obtainRouteObject(RouteLine route, OverlayManager routeOverlay) {

                RoutePlanDemo.this.route = route;
                RoutePlanDemo.this.routeOverlay = routeOverlay;
            }
        });
        routePlanHelper.setOnMassTransitRouteObjectListener(new RoutePlanHelper.OnMassTransitRouteObjectListener() {
            @Override
            public void obtainMassTransitRouteObject(MassTransitRouteResult result, MassTransitRouteLine routeLine, OverlayManager overlayManager) {
                massroute = routeLine;
                nowResultmass = result;
                routeOverlay = overlayManager;
            }
        });
        /** ------------------------ 只需这几句代码，即可在完成初始设置------------------------- */
    }

    /**
     * 发起路线规划搜索示例
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        route = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaidumap.clear();
        
        // 处理搜索按钮响应
        // 设置起终点信息，对于tranist search 来说，城市名无意义
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", startNodeStr);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", endNodeStr);

        // 实际使用中请对起点终点城市进行正确的设定

        if (v.getId() == R.id.mass) {
            PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
            PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");

            routePlanHelper.masstransitSearch(stMassNode,enMassNode);
            nowSearchType = 0;
            
        } else if (v.getId() == R.id.drive) {
            /*----PlanNode可调用PlanNode.withLocation()传入坐标，得到点---*/
            /** 直接调用对应方法，传入起点、终点，搜索路线规划，即可在mapview上出现结果。接口回调结果已在routePlanHelper内处理 */
            routePlanHelper.drivingSearch(stNode,enNode);
            nowSearchType = 1;
            
        } else if (v.getId() == R.id.transit) {
            routePlanHelper.transitSearch(stNode,enNode);
            nowSearchType = 2;
        } else if (v.getId() == R.id.walk) {
            routePlanHelper.walkingSearch(stNode,enNode);
            nowSearchType = 3;
        } else if (v.getId() == R.id.bike) {
            routePlanHelper.bikingSearch(stNode,enNode);
            nowSearchType = 4;
        }
    }

    /**
     * 节点浏览示例。。。左右按钮查看的节点信息
     *
     * @param v
     */
    public void nodeClick(View v) {
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;

        if (nowSearchType != 0 && nowSearchType != -1) {
            // 非跨城综合交通
            if (route == null || route.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < route.getAllStep().size() - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            // 获取节结果信息
            step = route.getAllStep().get(nodeIndex);
            if (step instanceof DrivingRouteLine.DrivingStep) {
                nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
                nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
            } else if (step instanceof WalkingRouteLine.WalkingStep) {
                nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
                nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
            } else if (step instanceof TransitRouteLine.TransitStep) {
                nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
                nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
            } else if (step instanceof BikingRouteLine.BikingStep) {
                nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
                nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
            }
        } else if (nowSearchType == 0) {
            // 跨城综合交通  综合跨城公交的结果判断方式不一样


            if (massroute == null || massroute.getNewSteps() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
            int size = 0;
            if (isSamecity) {
                size = massroute.getNewSteps().size();
            } else {
                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
                    size += massroute.getNewSteps().get(i).size();
                }
            }

            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < size - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            if (isSamecity) {
                // 同城
                step = massroute.getNewSteps().get(nodeIndex).get(0);
            } else {
                // 跨城
                int num = 0;
                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
                    num += massroute.getNewSteps().get(j).size();
                    if (nodeIndex - num < 0) {
                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
                        step = massroute.getNewSteps().get(j).get(k);
                        break;
                    }
                }
            }

            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        // 移动节点至中心
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(RoutePlanDemo.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this,
                    "将使用系统起终点图标",
                    Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this,
                    "将使用自定义起终点图标",
                    Toast.LENGTH_SHORT).show();

        }
        useDefaultIcon = !useDefaultIcon;
        routePlanHelper.setUseDefaultIcon(useDefaultIcon);
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        routePlanHelper.destroy();
        mMapView.onDestroy();
        super.onDestroy();
    }


}
