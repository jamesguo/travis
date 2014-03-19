package pt.ua.travis.utils;

import android.app.Activity;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Validate {

    private Validate(){}

    public static boolean hasGooglePlayServices(Activity activity){
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if(status == ConnectionResult.SUCCESS)
            return true;
        else {
            GooglePlayServicesUtil.getErrorDialog(status, activity, 5).show();
            return false;
        }
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isLandscape(Context context){
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
