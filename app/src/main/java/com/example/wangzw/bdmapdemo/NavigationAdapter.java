package com.example.wangzw.bdmapdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine;

import java.util.List;

/**
 * Created by wangzw on 2017/6/8.
 */

public class NavigationAdapter extends BaseAdapter {

    private List<String> routeLines;
    private LayoutInflater layoutInflater;
    private NavigationAdapter.Type mtype;


    public NavigationAdapter(Context context, List<String> routeLines, Type type) {
        this.routeLines = routeLines;
        layoutInflater = LayoutInflater.from(context);
        mtype = type;
    }


    @Override
    public int getCount() {
        return routeLines.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavigationAdapter.NodeViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_navigation, null);
            holder = new NavigationAdapter.NodeViewHolder();
            holder.tv_describe = (TextView) convertView.findViewById(R.id.tv_describe);
            convertView.setTag(holder);
        } else {
            holder = (NavigationAdapter.NodeViewHolder) convertView.getTag();
        }

        switch (mtype) {
            case TRANSIT_ROUTE:
            case WALKING_ROUTE:
            case BIKING_ROUTE:
            case DRIVING_ROUTE:
                holder.tv_describe.setText(routeLines.get(position));
                break;
            case MASS_TRANSIT_ROUTE:
                break;

            default:
                break;

        }

        return convertView;
    }

    private class NodeViewHolder {

        private TextView tv_describe;

    }

    public enum Type {
        MASS_TRANSIT_ROUTE, // 综合交通
        TRANSIT_ROUTE, // 公交
        DRIVING_ROUTE, // 驾车
        WALKING_ROUTE, // 步行
        BIKING_ROUTE // 骑行

    }
}
