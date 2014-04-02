package pt.ua.travis.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import pt.ua.travis.R;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class CommonRes {

    private static boolean isInitialized = false;

    public static void initialize(Context context){
        if(!isInitialized) {
            AVAILABLE_SHAPE = context.getResources().getDrawable(R.drawable.available_shape);
            UNAVAILABLE_SHAPE = context.getResources().getDrawable(R.drawable.unavailable_border);
            FAVORITE_ICON = context.getResources().getDrawable(R.drawable.ic_favorites);
            SELECTED_ITEM_COLOR_INT = context.getResources().getColor(R.color.selectorSelectedBg);

            isInitialized = true;
        }
    }

    public static Drawable AVAILABLE_SHAPE;
    public static Drawable UNAVAILABLE_SHAPE;
    public static Drawable FAVORITE_ICON;
    public static int SELECTED_ITEM_COLOR_INT;


}
