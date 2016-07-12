package com.softwaremobility.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Created by darkgeat on 4/27/16.
 */
public class PermissionsMarshmallow {

    public static final int REQUEST_CODE_GPS = 4356;
    public static final int REQUEST_CODE = 1707;

    public static boolean permissionForGPSGranted(Activity context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
                    return false;
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS);
                    return false;
                }
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    public static boolean permissionReadMemmory(Activity context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(context,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }else {
                    //No explanation needed, we can request the permissions.
                    ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},REQUEST_CODE);
                    return false;
                }
            }else {
                return true;
            }
        }else {
            return true;
        }
    }
}
