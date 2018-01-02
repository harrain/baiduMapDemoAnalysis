package baidumapsdk.demo.remould.track;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;

import baidumapsdk.demo.R;


/**
 * 轨迹分析详情对话框布局
 *
 * @author baidu
 */
public class TrackAnalysisInfoLayout extends LinearLayout {

    public TextView titleText = null;
    public TextView key1 = null;
    public TextView key2 = null;
    public TextView key3 = null;
    public TextView key4 = null;
    public TextView key5 = null;
    public TextView key6 = null;
    public TextView value1 = null;
    public TextView value2 = null;
    public TextView value3 = null;
    public TextView value4 = null;
    public TextView value5 = null;
    public TextView value6 = null;

    public View mView = null;

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public TrackAnalysisInfoLayout(final Context parent, final BaiduMap baiduMap) {
        super(parent);
        LayoutInflater inflater = (LayoutInflater) parent
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_track_analysis_info, null);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        key1 = (TextView) mView.findViewById(R.id.info_key_1);
        key2 = (TextView) mView.findViewById(R.id.info_key_2);
        key3 = (TextView) mView.findViewById(R.id.info_key_3);
        key4 = (TextView) mView.findViewById(R.id.info_key_4);
        key5 = (TextView) mView.findViewById(R.id.info_key_5);
        key6 = (TextView) mView.findViewById(R.id.info_key_6);
        value1 = (TextView) mView.findViewById(R.id.info_value_1);
        value2 = (TextView) mView.findViewById(R.id.info_value_2);
        value3 = (TextView) mView.findViewById(R.id.info_value_3);
        value4 = (TextView) mView.findViewById(R.id.info_value_4);
        value5 = (TextView) mView.findViewById(R.id.info_value_5);
        value6 = (TextView) mView.findViewById(R.id.info_value_6);

        setFocusable(true);
        mView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                baiduMap.hideInfoWindow();
            }
        });
    }

}
