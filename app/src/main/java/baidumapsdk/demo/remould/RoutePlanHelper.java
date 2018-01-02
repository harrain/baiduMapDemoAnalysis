package baidumapsdk.demo.remould;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.List;

import baidumapsdk.demo.R;
import baidumapsdk.demo.search.RouteLineAdapter;
import baidumapsdk.demo.search.RoutePlanDemo;

/**
 * Created by net on 2017/12/29.
 */

public class RoutePlanHelper implements OnGetRoutePlanResultListener {


    // 搜索相关
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用

    Context mContext;
    BaiduMap mBaidumap;

    boolean hasShownDialogue = false;
    boolean useDefaultIcon = false;
    private OnPreDoListener onPreDoListener;
    private OnObtainRouteObjectListener onObtainRouteObjectListener;
    private OnMassTransitRouteObjectListener onMassTransitRouteObjectListener;

    public RoutePlanHelper(Context context) {
        mContext = context;
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

    }

    public void setMap(BaiduMap map) {
        mBaidumap = map;
    }

    public void masstransitSearch(PlanNode stMassNode, PlanNode enMassNode) {
        mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
    }

    public void drivingSearch(PlanNode stNode, PlanNode enNode) {
        mSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode).to(enNode));
    }

    public void transitSearch(PlanNode stNode, PlanNode enNode) {
        mSearch.transitSearch((new TransitRoutePlanOption())
                .from(stNode).city("北京").to(enNode));
    }

    public void walkingSearch(PlanNode stNode, PlanNode enNode) {
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode).to(enNode));
    }

    public void bikingSearch(PlanNode stNode, PlanNode enNode) {
        mSearch.bikingSearch((new BikingRoutePlanOption())
                .from(stNode).to(enNode));
    }

    public void setUseDefaultIcon(boolean defaultIcon) {
        useDefaultIcon = defaultIcon;
    }

    /**
     * 路线规划接口步行下结果
     */
    @Override
    public void onGetWalkingRouteResult(final WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (onPreDoListener!=null)onPreDoListener.preDo();

            if (result.getRouteLines().size() > 1) {

                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(mContext,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.WALKING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            overlay.setData(result.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();

                            if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(position),overlay);
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

                if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(0),overlay);

            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    /**
     * 路线规划接口下公交线路结果
     */
    @Override
    public void onGetTransitRouteResult(final TransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {

            if (onPreDoListener!=null)onPreDoListener.preDo();

            if (result.getRouteLines().size() > 1) {

                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(mContext,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.TRANSIT_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {

                            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            overlay.setData(result.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();

                            if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(position),overlay);
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

                if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(0),overlay);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }


        }
    }

    /**
     * 路线规划接口下跨城交通线路结果
     */
    @Override
    public void onGetMassTransitRouteResult(final MassTransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {


            if (onPreDoListener!=null)onPreDoListener.preDo();

            if (!hasShownDialogue) {
                // 列表选择
                MyTransitDlg myTransitDlg = new MyTransitDlg(mContext,
                        result.getRouteLines(),
                        RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);

                myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        hasShownDialogue = false;
                    }
                });
                myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                    public void onItemClick(int position) {

                        MyMassTransitRouteOverlay overlay = new MyMassTransitRouteOverlay(mBaidumap);
                        mBaidumap.setOnMarkerClickListener(overlay);
                        overlay.setData(result.getRouteLines().get(position));

                        MassTransitRouteLine line = result.getRouteLines().get(position);
                        overlay.setData(line);
                        if (result.getOrigin().getCityId() == result.getDestination().getCityId()) {
                            // 同城
                            overlay.setSameCity(true);
                        } else {
                            // 跨城
                            overlay.setSameCity(false);

                        }
                        mBaidumap.clear();
                        overlay.addToMap();
                        overlay.zoomToSpan();

                        if (onMassTransitRouteObjectListener!=null)onMassTransitRouteObjectListener.obtainMassTransitRouteObject(result,result.getRouteLines().get(position),overlay);
                    }

                });
                myTransitDlg.show();
                hasShownDialogue = true;
            }
        }
    }

    /**
     * 路线规划接口下的驾车线路结果
     */
    @Override
    public void onGetDrivingRouteResult(final DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (onPreDoListener!=null)onPreDoListener.preDo();

            /** 得到线路推荐数据，弹窗显示 */
            if (result.getRouteLines().size() > 1) {
                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(mContext,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.DRIVING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    /** 点击哪条线路，在地图上显示那条推荐线路 */
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            overlay.setData(result.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();

                            if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(position),overlay);
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {//一个的话，就显示这一个，不用弹窗选择了
                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);
                if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(0),overlay);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    /**
     * 路线规划接口下的骑行线路结果
     */
    @Override
    public void onGetBikingRouteResult(final BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (onPreDoListener!=null)onPreDoListener.preDo();

            if (result.getRouteLines().size() > 1) {
                if (!hasShownDialogue) {
                    MyTransitDlg myTransitDlg = new MyTransitDlg(mContext,
                            result.getRouteLines(),
                            RouteLineAdapter.Type.DRIVING_ROUTE);
                    myTransitDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShownDialogue = false;
                        }
                    });
                    myTransitDlg.setOnItemInDlgClickLinster(new OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                            mBaidumap.setOnMarkerClickListener(overlay);
                            overlay.setData(result.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();

                            if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(position),overlay);
                        }

                    });
                    myTransitDlg.show();
                    hasShownDialogue = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);
                if (onObtainRouteObjectListener!=null)onObtainRouteObjectListener.obtainRouteObject(result.getRouteLines().get(0),overlay);
            } else {
                Log.d("route result", "结果数<0");
                return;
            }

        }
    }

    public void destroy() {
        if (mSearch != null) mSearch.destroy();
    }

    public void setOnMassTransitRouteObjectListener(OnMassTransitRouteObjectListener listener){
        onMassTransitRouteObjectListener = listener;
    }

    public void setOnPreDoListener(OnPreDoListener listener) {
        onPreDoListener = listener;
    }

    public void setOnObtainRouteObjectListener(OnObtainRouteObjectListener listener){
        onObtainRouteObjectListener = listener;
    }


    public interface OnMassTransitRouteObjectListener{
        void obtainMassTransitRouteObject(MassTransitRouteResult result,MassTransitRouteLine routeLine,OverlayManager routeOverlay);
    }

    public interface OnObtainRouteObjectListener{
        void obtainRouteObject(RouteLine route,OverlayManager routeOverlay);
    }

    public interface OnPreDoListener {
        void preDo();
    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    // 供路线选择的Dialog
    class MyTransitDlg extends Dialog {

        private List<? extends RouteLine> mtransitRouteLines;
        private ListView transitRouteList;
        private RouteLineAdapter mTransitAdapter;

        OnItemInDlgClickListener onItemInDlgClickListener;

        public MyTransitDlg(Context context, int theme) {
            super(context, theme);
        }

        public MyTransitDlg(Context context, List<? extends RouteLine> transitRouteLines, RouteLineAdapter.Type
                type) {
            this(context, 0);
            mtransitRouteLines = transitRouteLines;
            mTransitAdapter = new RouteLineAdapter(context, mtransitRouteLines, type);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        @Override
        public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
            super.setOnDismissListener(listener);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transit_dialog);

            transitRouteList = (ListView) findViewById(R.id.transitList);
            transitRouteList.setAdapter(mTransitAdapter);

            transitRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemInDlgClickListener.onItemClick(position);
//                    mBtnPre.setVisibility(View.VISIBLE);
//                    mBtnNext.setVisibility(View.VISIBLE);
                    dismiss();
                    hasShownDialogue = false;
                }
            });
        }

        public void setOnItemInDlgClickLinster(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }

    }

    // 定制驾车RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    private class MyMassTransitRouteOverlay extends MassTransitRouteOverlay {
        public MyMassTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

}
