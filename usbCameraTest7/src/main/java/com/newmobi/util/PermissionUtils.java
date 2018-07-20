package com.newmobi.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import rx.internal.operators.OnSubscribeRedo;

/**
 * Created by Alextao on 2018/7/18,星期三.
 * Email : tao_xue@new-mobi.com
 * This class is for solve the permission issues.
 */

public class PermissionUtils {

    /**
     * check itself has the permission or not.
     *
     * @param context    context
     * @param permission what permission is our need?
     */
    private static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * check more permissions
     *
     * @param context     context
     * @param permissions permissions
     * @return the permission list which not granted.
     */
    private static List<String> checkMorePermission(Context context, String[] permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                permissionList.add(permission);
            }
        }
        return permissionList;
    }

    /**
     * request the permission.
     */
    public static void requestPermission(Context context, String permission, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
    }


}
