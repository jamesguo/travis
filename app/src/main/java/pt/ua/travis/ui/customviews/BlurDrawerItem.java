package pt.ua.travis.ui.customviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import pt.ua.travis.R;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class BlurDrawerItem extends BlurDrawerObject {

    private ImageView iv_icon;
    private TextView tv_title;

    public BlurDrawerItem(Context context) {
        super(context);
        init(context);
    }

    public BlurDrawerItem(Context context, int icon, int titleID) {
        super(context);
        init(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(titleID);
    }

    public BlurDrawerItem(Context context, int icon, String title) {
        super(context);
        init(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(title);
    }

    private void init(Context context){
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.blur_item, this);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }
}
