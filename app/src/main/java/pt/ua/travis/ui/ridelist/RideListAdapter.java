package pt.ua.travis.ui.ridelist;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Utils;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListAdapter extends BaseAdapter implements ListAdapter {

    private MainActivity parentActivity;
    private List<RideItem> itemList;

    RideListAdapter(final MainActivity parentActivity, final List<RideItem> rideItemList) {
        this.parentActivity = parentActivity;
        this.itemList = rideItemList;
    }

    public void update(List<RideItem> newRideItems){
        itemList.clear();
        itemList.addAll(newRideItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public RideItem getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RideItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.item_ride, parent, false);
            holder = new ViewHolder();
            holder.withUserPhoto = (CircularImageView) convertView.findViewById(R.id.with_user_photo);
            holder.withUserFavoriteFlag = (ImageView) convertView.findViewById(R.id.with_user_favorite_flag);
            holder.withUserName = (TextView) convertView.findViewById(R.id.with_user_name);
            holder.timeToRide = (TextView) convertView.findViewById(R.id.time_to_ride);
            holder.originIcon = (FontAwesomeText) convertView.findViewById(R.id.origin_icon);
            holder.originLabel = (TextView) convertView.findViewById(R.id.origin_label);
            holder.destinationLabel = (TextView) convertView.findViewById(R.id.destination_label);
            holder.acceptDelete = (BootstrapButton) convertView.findViewById(R.id.accept_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView)parent).recycle(convertView, position);

        Picasso.with(parentActivity).load(item.getUserPhoto()).into(holder.withUserPhoto);
        holder.withUserName.setText(item.getUserName());
        if (item.showFavoriteIcon()) {
            holder.withUserFavoriteFlag.setImageDrawable(CommonRes.get().FAVORITE_ICON_FILLED);
        }

        final Ride r = item.getRideObject();
        holder.timeToRide.setText(r.getRemaining());
        if (item.getUserTypeToShow() == RideItem.SHOW_TAXI) {
            holder.originIcon.setIcon("fa-taxi");
        }
        LatLng pos1 = r.originPosition();
        List<Address> addressList1 = Utils.addressesFromLocation(parentActivity, pos1.latitude, pos1.longitude);
        if(addressList1!=null && !addressList1.isEmpty()) {
            holder.originLabel.setText(Utils.addressToString(addressList1.get(0)));
        }
        LatLng pos2 = r.destinationPosition();
        List<Address> addressList2 = Utils.addressesFromLocation(parentActivity, pos2.latitude, pos2.longitude);
        if(addressList2!=null && !addressList2.isEmpty()) {
            holder.destinationLabel.setText(Utils.addressToString(addressList2.get(0)));
        } else {
            holder.destinationLabel.setText(parentActivity.getString(R.string.unknown_address));
        }


        holder.acceptDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.remove(r);
                PersistenceManager.delete(r);
                notifyDataSetChanged();
            }
        });


        return convertView;
    }

    static class ViewHolder {
        CircularImageView withUserPhoto;
        ImageView withUserFavoriteFlag;
        TextView withUserName;
        TextView timeToRide;
        FontAwesomeText originIcon;
        TextView originLabel;
        TextView destinationLabel;
        BootstrapButton acceptDelete;
    }
}
