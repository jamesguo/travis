package pt.ua.travis.utils;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.common.base.Defaults;

import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Validate {

    private Validate(){}

    public static boolean argExists(SharedPreferences prefs, String prefKey){
        if(prefs==null)
            return false;

        // Attempts to get a map of preferences
        Map<String, ?> allPrefs = prefs.getAll();
        if(allPrefs==null)
            return false;

        // Attempts to return a preference with the provided key.
        Object obtainedPref = allPrefs.get(prefKey);
        return argExistsAux(obtainedPref);
    }

    public static boolean argExists(Intent intent, String extraKey){
        if(intent==null)
            return false;

        // Attempts to get a bundle of extras
        Bundle extras = intent.getExtras();
        if(extras==null)
            return false;

        // Attempts to return a extra with the provided key.
        Object obtainedExtra = extras.get(extraKey);
        return argExistsAux(obtainedExtra);
    }

    private static boolean argExistsAux(Object obtainedArg){
        if(obtainedArg==null)
            return false;

        Class<?> obtainedArgClass = obtainedArg.getClass();
        if(obtainedArgClass.isInstance(String.class)){
            return !((String)obtainedArg).isEmpty();

        } else {
            return !obtainedArgClass.isPrimitive() ||
                    obtainedArg == Defaults.defaultValue(obtainedArgClass);
        }
    }

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
