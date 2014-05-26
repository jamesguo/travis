package pt.ua.travis.ui.navigationdrawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.ui.main.MainActivity;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class DrawerViewAdapter extends ArrayAdapter<DrawerView> {

    private static final int LAYOUT_RESOURCE = R.layout.item_drawer;


    private MainActivity parentActivity;

    private List<DrawerView> objects;


    public DrawerViewAdapter(MainActivity parentActivity, List<DrawerView> objects) {
        super(parentActivity, LAYOUT_RESOURCE, objects);
        this.parentActivity = parentActivity;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItemHolder holder;

        if (view == null) {
            LayoutInflater inflater = parentActivity.getLayoutInflater();
            holder = new DrawerItemHolder();
            view = inflater.inflate(LAYOUT_RESOURCE, parent, false);

            holder.drawerUserLayout = (LinearLayout) view.findViewById(R.id.drawer_user_layout);
            holder.drawerItemLayout = (RelativeLayout) view.findViewById(R.id.drawer_item_layout);
            holder.drawerSeparator = view.findViewById(R.id.drawer_separator);

            holder.drawerUserName = (TextView) view.findViewById(R.id.drawer_user_name);
            holder.drawerUserPhoto = (ImageView) view.findViewById(R.id.drawer_user_photo);
            holder.drawerItemName = (TextView) view.findViewById(R.id.drawer_item_name);
            holder.drawerItemIcon = (ImageView) view.findViewById(R.id.drawer_item_icon);
            holder.drawerItemCounter = (TextView) view.findViewById(R.id.drawer_item_counter);

            view.setTag(holder);

        } else {
            holder = (DrawerItemHolder) view.getTag();
        }

        DrawerView drawerView = objects.get(position);

        if(drawerView instanceof DrawerUser){
            holder.drawerUserLayout.setVisibility(LinearLayout.VISIBLE);
            holder.drawerSeparator.setVisibility(View.INVISIBLE);
            holder.drawerItemLayout.setVisibility(RelativeLayout.GONE);
            DrawerUser duii = (DrawerUser) drawerView;

            Picasso.with(parentActivity).load(duii.loggedInUser.imageUri()).into(holder.drawerUserPhoto);
            holder.drawerUserName.setText(duii.loggedInUser.name());

        } else if(drawerView instanceof DrawerItem) {
            holder.drawerUserLayout.setVisibility(LinearLayout.GONE);
            holder.drawerSeparator.setVisibility(View.GONE);
            holder.drawerItemLayout.setVisibility(RelativeLayout.VISIBLE);
            DrawerItem dli = (DrawerItem) drawerView;

            holder.drawerItemIcon.setImageDrawable(view.getResources().getDrawable(dli.itemImageID));
            holder.drawerItemName.setText(view.getResources().getString(dli.itemNameID));
            if (dli.itemCounter != null) {
                holder.drawerItemCounter.setText(dli.itemCounter);
                holder.drawerItemCounter.setVisibility(View.VISIBLE);
            } else {
                holder.drawerItemCounter.setVisibility(View.INVISIBLE);
            }
        } else if(drawerView instanceof DrawerSeparator){
            holder.drawerUserLayout.setVisibility(LinearLayout.GONE);
            holder.drawerSeparator.setVisibility(View.VISIBLE);
            holder.drawerItemLayout.setVisibility(RelativeLayout.GONE);
        }

        return view;
    }

    private static class DrawerItemHolder {

        // Parent views, to be shown or hidden according to the type
        private LinearLayout drawerUserLayout;
        private RelativeLayout drawerItemLayout;
        private View drawerSeparator;

        // DrawerUser views
        private ImageView drawerUserPhoto;
        private TextView drawerUserName;

        // DrawerItem views
        private TextView drawerItemName;
        private ImageView drawerItemIcon;
        private TextView drawerItemCounter;

    }
}
