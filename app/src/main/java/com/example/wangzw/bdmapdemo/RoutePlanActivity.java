/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.example.wangzw.bdmapdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
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


/**
 * 此demo用来展示如何进行驾车、步行、公交、骑行、跨城综合路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 */
public class RoutePlanActivity extends Activity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener, AdapterView.OnItemClickListener {

    private EditText startEt;
    private EditText endEt;
    private ListView routeLv;

    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    // 搜索相关
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用

    WalkingRouteResult nowResultwalk = null;
    BikingRouteResult nowResultbike = null;
    TransitRouteResult nowResultransit = null;
    DrivingRouteResult nowResultdrive = null;
    MassTransitRouteResult nowResultmass = null;

    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。

    String startNodeStr = "天安门";
    String endNodeStr = "北京西站地铁";

    private BDLocation myLocation = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeplan);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            myLocation = extras.getParcelable("myLocation");
        }
        CharSequence titleLable = "路线规划功能";
        setTitle(titleLable);
        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        startEt = (EditText) findViewById(R.id.et_start);
        endEt = (EditText) findViewById(R.id.et_end);
        routeLv = (ListView) findViewById(R.id.lv_route);

        routeLv.setOnItemClickListener(this);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        if (myLocation != null && myLocation.getLatitude() != 0 && myLocation.getLongitude() != 0) {
            startEt.setText("我的位置");
        }else {
            startEt.setText("天安门");
        }
    }

    /**
     * 发起路线规划搜索示例
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        mBaidumap.clear();
        startNodeStr = startEt.getText().toString().trim();
        endNodeStr = endEt.getText().toString().trim();

        if (TextUtils.isEmpty(startNodeStr)) {
            Toast.makeText(this, "请输入起点", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(endNodeStr)) {
            Toast.makeText(this, "请输入终点", Toast.LENGTH_LONG).show();
            return;
        }
        // 处理搜索按钮响应
        // 设置起终点信息，对于tranist search 来说，城市名无意义
        PlanNode stNode;
        if (myLocation != null && "我的位置".equals(startEt.getText().toString())) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            stNode = PlanNode.withLocation(latLng);
        } else {
            stNode = PlanNode.withCityNameAndPlaceName("北京", startNodeStr);
        }
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", endNodeStr);


        // 实际使用中请对起点终点城市进行正确的设定

        if (v.getId() == R.id.mass) {
            PlanNode stMassNode = PlanNode.withCityNameAndPlaceName("北京", "天安门");
            PlanNode enMassNode = PlanNode.withCityNameAndPlaceName("上海", "东方明珠");

            mSearch.masstransitSearch(new MassTransitRoutePlanOption().from(stMassNode).to(enMassNode));
            nowSearchType = 0;
        } else if (v.getId() == R.id.drive) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 1;
        } else if (v.getId() == R.id.transit) {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode).city("北京").to(enNode));
            nowSearchType = 2;
        } else if (v.getId() == R.id.walk) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 3;
        } else if (v.getId() == R.id.bike) {
            mSearch.bikingSearch((new BikingRoutePlanOption())
                    .from(stNode).to(enNode));
            nowSearchType = 4;
        }
        routeLv.setVisibility(View.GONE);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//             result.getSuggestAddrInfo();
            AlertDialog.Builder builder = new AlertDialog.Builder(RoutePlanActivity.this);
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
            if (result.getRouteLines().size() > 0) {
                nowResultwalk = result;
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActivity.this, result.getRouteLines(), RouteLineAdapter.Type.WALKING_ROUTE);
                routeLv.setAdapter(routeLineAdapter);
                routeLv.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//             result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (result.getRouteLines().size() > 0) {
                nowResultransit = result;
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActivity.this, result.getRouteLines(), RouteLineAdapter.Type.TRANSIT_ROUTE);
                routeLv.setAdapter(routeLineAdapter);
                routeLv.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点模糊，获取建议列表
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nowResultmass = result;
            if (result.getRouteLines().size() > 0) {
                nowResultmass = result;
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActivity.this, result.getRouteLines(), RouteLineAdapter.Type.MASS_TRANSIT_ROUTE);
                routeLv.setAdapter(routeLineAdapter);
                routeLv.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (result.getRouteLines().size() > 0) {
                nowResultdrive = result;
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActivity.this, result.getRouteLines(), RouteLineAdapter.Type.DRIVING_ROUTE);
                routeLv.setAdapter(routeLineAdapter);
                routeLv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(RoutePlanActivity.this);
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
            if (result.getRouteLines().size() > 0) {
                nowResultbike = result;
                RouteLineAdapter routeLineAdapter = new RouteLineAdapter(RoutePlanActivity.this, result.getRouteLines(), RouteLineAdapter.Type.BIKING_ROUTE);
                routeLv.setAdapter(routeLineAdapter);
                routeLv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListAdapter adapter = routeLv.getAdapter();
        if (adapter != null) {
            Intent intent = new Intent(RoutePlanActivity.this, RoutePlanMapActivity.class);
            intent.putExtra("searchType", nowSearchType);
            if (nowSearchType == 0) {
                MassTransitRouteLine searchResult = nowResultmass.getRouteLines().get(position);
                if (searchResult != null) {
                    intent.putExtra("searchResult", searchResult);
                }
            } else if (nowSearchType == 1) {
                DrivingRouteLine searchResult = nowResultdrive.getRouteLines().get(position);
                if (searchResult != null) {
                    intent.putExtra("searchResult", searchResult);
                }
            } else if (nowSearchType == 2) {
                TransitRouteLine searchResult = nowResultransit.getRouteLines().get(position);
                if (searchResult != null) {
                    intent.putExtra("searchResult", searchResult);
                }
            } else if (nowSearchType == 3) {
                WalkingRouteLine searchResult = nowResultwalk.getRouteLines().get(position);
                if (searchResult != null) {
                    intent.putExtra("searchResult", searchResult);
                }
            } else if (nowSearchType == 4) {
                BikingRouteLine searchResult = nowResultbike.getRouteLines().get(position);
                if (searchResult != null) {
                    intent.putExtra("searchResult", searchResult);
                }
            } else {
                return;
            }
            startActivity(intent);
        }
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
        if (mSearch != null) {
            mSearch.destroy();
        }
        mMapView.onDestroy();
        super.onDestroy();
    }
}
