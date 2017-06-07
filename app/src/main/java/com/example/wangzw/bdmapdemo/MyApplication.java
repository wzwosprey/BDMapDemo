package com.example.wangzw.bdmapdemo;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by wangzw on 2017/6/7.
 */

public class MyApplication extends Application {
    /**
     * 百度定位
     */
    private LocationService locationService;
    private Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
        initLogger();
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public Vibrator getmVibrator() {
        return mVibrator;
    }

    /**
     * 初始化日志功能
     */
    private void initLogger() {
        Logger.init("my_tag")                   // 如果仅仅调用 init 不传递参数，默认标签是 PRETTYLOGGER
                .methodCount(3)                 // 显示调用方法链的数量，默认是2
                .hideThreadInfo()               // 隐藏线程信息，默认是隐藏
                .logLevel(LogLevel.FULL)        // 日志等级，其实就是控制是否打印，默认为 LogLevel.FULL,LogLevel.NONE不打印
                .methodOffset(2);               // default 0
    }
}
