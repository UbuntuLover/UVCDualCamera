package com.newmobi.alextaohelper.CrashUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alextao on 2018/7/21,星期六.
 * Email : tao_xue@new-mobi.com
 * This is a helper class.
 * Handle the crash and log for file.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    //log tag.
    private static final String TAG = CrashHandler.class.getSimpleName();

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private Context mContext;

    private Map<String, String> infoMap = new HashMap<>();

    public void init(Context context, Class<?> activityClazz) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            //the user does not handle the exception,then go to
            //default do this.
            mDefaultHandler.uncaughtException(t, e);
        } else {
            System.out.println("uncaughtexception -------->" + e.getLocalizedMessage());
            logError(e);

        }

    }

    /**
     * @param ex exceptions what we need to handle.
     * @return handle the exception or not.
     * @author Alextao
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext.getApplicationContext()
                        , "程序有异常。。", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
        collectDeviceInfo();
        logError(ex);
        return true;
    }


    private void collectDeviceInfo() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infoMap.put("versionName", versionName);
                infoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                infoMap.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void logError(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        int num = ex.getStackTrace().length;
        for (int i = 0; i < num; i++) {
            sb.append(ex.getStackTrace()[i].toString());
            sb.append('\n');
        }
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + System.currentTimeMillis() + ".log");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write((sb.toString() + "异常：" + ex.getLocalizedMessage()).getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
