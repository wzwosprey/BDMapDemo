package com.example.wangzw.bdmapdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.wangzw.bdmapdemo.overlayutil.BikingRouteOverlay;
import com.example.wangzw.bdmapdemo.overlayutil.DrivingRouteOverlay;
import com.example.wangzw.bdmapdemo.overlayutil.MassTransitRouteOverlay;
import com.example.wangzw.bdmapdemo.overlayutil.TransitRouteOverlay;
import com.example.wangzw.bdmapdemo.overlayutil.WalkingRouteOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzw on 2017/6/8.
 */

public class RoutePlanMapActivity extends Activity implements BaiduMap.OnMapClickListener, AdapterView.OnItemClickListener {

    private ListView lv_route;
    private List<String> nodeList;
    private NavigationAdapter adapter;
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;

    private RouteLine route = null;
    private int nodeIndex = -1; // 节点索引,供浏览节点时使用
    private int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。
    private WalkingRouteLine walkingRouteLine;
    private BikingRouteLine bikingRouteLine;
    private TransitRouteLine transitRouteLine;
    private DrivingRouteLine drivingRouteLine;
    private MassTransitRouteLine massTransitRouteLine;
    private TextView popupText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan_map);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        lv_route = (ListView) findViewById(R.id.lv_route);
        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        //普通地图
        mBaidumap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaidumap.setMapStatus(MapStatusUpdateFactory.zoomTo(14));
    }

    private void initData() {
        nodeList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nowSearchType = extras.getInt("searchType", -1);
            if (nowSearchType == 0) {
                massTransitRouteLine = extras.getParcelable("searchResult");
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(massTransitRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
                //massTransitRouteLine.getNewSteps()
                route = massTransitRouteLine;
            } else if (nowSearchType == 1) {
                drivingRouteLine = extras.getParcelable("searchResult");
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(drivingRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
                List<DrivingRouteLine.DrivingStep> allStep = drivingRouteLine.getAllStep();
                for (int i = 0; i < allStep.size(); i++) {
                    nodeList.add(allStep.get(i).getInstructions());
                }
                adapter = new NavigationAdapter(this, nodeList, NavigationAdapter.Type.DRIVING_ROUTE);
                lv_route.setAdapter(adapter);
                route = drivingRouteLine;
            } else if (nowSearchType == 2) {
                transitRouteLine = extras.getParcelable("searchResult");
                TransitRouteOverlay overlay = new TransitRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(transitRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
                List<TransitRouteLine.TransitStep> allStep = transitRouteLine.getAllStep();
                for (int i = 0; i < allStep.size(); i++) {
                    nodeList.add(allStep.get(i).getInstructions());
                }
                adapter = new NavigationAdapter(this, nodeList, NavigationAdapter.Type.TRANSIT_ROUTE);
                lv_route.setAdapter(adapter);
                route = transitRouteLine;
            } else if (nowSearchType == 3) {
                walkingRouteLine = extras.getParcelable("searchResult");
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(walkingRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
                List<WalkingRouteLine.WalkingStep> allStep = walkingRouteLine.getAllStep();
                for (int i = 0; i < allStep.size(); i++) {
                    nodeList.add(allStep.get(i).getInstructions());
                }
                adapter = new NavigationAdapter(this, nodeList, NavigationAdapter.Type.WALKING_ROUTE);
                lv_route.setAdapter(adapter);
                route = walkingRouteLine;
            } else if (nowSearchType == 4) {
                bikingRouteLine = extras.getParcelable("searchResult");
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaidumap);
                mBaidumap.setOnMarkerClickListener(overlay);
                overlay.setData(bikingRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
                List<BikingRouteLine.BikingStep> allStep = bikingRouteLine.getAllStep();
                for (int i = 0; i < allStep.size(); i++) {
                    nodeList.add(allStep.get(i).getInstructions());
                }
                adapter = new NavigationAdapter(this, nodeList, NavigationAdapter.Type.BIKING_ROUTE);
                lv_route.setAdapter(adapter);
                route = bikingRouteLine;
            } else {

            }
        }
    }


    private void initListener() {
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);

        lv_route.setOnItemClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        nodeClick(position);
    }


    /**
     * 节点浏览示例
     *
     * @param position
     */
    public void nodeClick(int position) {
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = null;

        if (nowSearchType != 0 && nowSearchType != -1) {
            nodeIndex = position;
            // 非跨城综合交通
            if (route == null || route.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1) {
                return;
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
//
//
//            if (massroute == null || massroute.getNewSteps() == null) {
//                return;
//            }
//            if (nodeIndex == -1 && v.getId() == R.id.pre) {
//                return;
//            }
//            boolean isSamecity = nowResultmass.getOrigin().getCityId() == nowResultmass.getDestination().getCityId();
//            int size = 0;
//            if (isSamecity) {
//                size = massroute.getNewSteps().size();
//            } else {
//                for (int i = 0; i < massroute.getNewSteps().size(); i++) {
//                    size += massroute.getNewSteps().get(i).size();
//                }
//            }
//
//            // 设置节点索引
//            if (v.getId() == R.id.next) {
//                if (nodeIndex < size - 1) {
//                    nodeIndex++;
//                } else {
//                    return;
//                }
//            } else if (v.getId() == R.id.pre) {
//                if (nodeIndex > 0) {
//                    nodeIndex--;
//                } else {
//                    return;
//                }
//            }
//            if (isSamecity) {
//                // 同城
//                step = massroute.getNewSteps().get(nodeIndex).get(0);
//            } else {
//                // 跨城
//                int num = 0;
//                for (int j = 0; j < massroute.getNewSteps().size(); j++) {
//                    num += massroute.getNewSteps().get(j).size();
//                    if (nodeIndex - num < 0) {
//                        int k = massroute.getNewSteps().get(j).size() + nodeIndex - num;
//                        step = massroute.getNewSteps().get(j).get(k);
//                        break;
//                    }
//                }
//            }
//
//            nodeLocation = ((MassTransitRouteLine.TransitStep) step).getStartLocation();
//            nodeTitle = ((MassTransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }

        // 移动节点至中心
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(RoutePlanMapActivity.this);
        popupText.setBackgroundResource(R.drawable.icon_location_msg_bg);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        popupText.setPadding(8, 8, 8, 8);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
