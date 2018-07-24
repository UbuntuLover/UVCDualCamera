package com.newmobi.application;

import android.app.Application;

import com.newmobi.alextaohelper.CrashUtil.CrashHandler;

/**
 * Created by Alextao on 2018/7/21,星期六.
 * Email : tao_xue@new-mobi.com
 */
public class NewMobiApplication extends Application {
    private CrashHandler crashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        crashHandler = new CrashHandler();
        crashHandler.init(getApplicationContext(), getClass());
    }
}
