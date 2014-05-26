package pt.ua.travis.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import pt.ua.travis.R;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class CommonRes {

    private static CommonRes instance = null;

//    public final Drawable AVAILABLE_SHAPE;
//    public final Drawable UNAVAILABLE_SHAPE;
    public final int AVAILABLE_COLOR;
    public final int UNAVAILABLE_COLOR;
    public final Drawable FAVORITE_ICON;
    public final int SELECTED_ITEM_COLOR;
    public final String UNKNOWN_ADDRESS;
    public final Drawable LOADING_PLACEHOLDER;

    private CommonRes(Context context) {
//        AVAILABLE_SHAPE = context.getResources().getDrawable(R.drawable.available_shape);
//        UNAVAILABLE_SHAPE = context.getResources().getDrawable(R.drawable.unavailable_border);
        AVAILABLE_COLOR = context.getResources().getColor(R.color.taxi_available_border);
        UNAVAILABLE_COLOR = context.getResources().getColor(R.color.taxi_unavailable_border);
        FAVORITE_ICON = context.getResources().getDrawable(R.drawable.ic_fav);
        SELECTED_ITEM_COLOR = context.getResources().getColor(R.color.selectorSelectedBg);
        UNKNOWN_ADDRESS = context.getResources().getString(R.string.unknown_address);
        LOADING_PLACEHOLDER = context.getResources().getDrawable(R.drawable.progress_bar);
    }

    public static void init(Context context){
        if(instance==null)
            instance = new CommonRes(context);
    }

    public static CommonRes get(){
        return instance;
    }
}
