package pt.ua.travis.ui.navigationdrawer;

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

    public BlurDrawerItem(Context context, int icon, int titleID) {
        super(context);
        init(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(titleID);
    }

    private void init(Context context){
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_item, this);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }
}
