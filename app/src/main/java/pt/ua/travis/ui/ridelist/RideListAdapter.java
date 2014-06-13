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
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.ui.addresspicker.AddressPickerDialog;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.TravisUtils;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListAdapter extends BaseAdapter implements ListAdapter {

    private MainActivity parentActivity;
    private RideListFragment rideListFragment;
    private List<RideItem> itemList;

    RideListAdapter(final MainActivity parentActivity, final RideListFragment rideListFragment, final List<RideItem> rideItemList) {
        this.parentActivity = parentActivity;
        this.rideListFragment = rideListFragment;
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final RideItem item = getItem(position);
        final ViewHolder holder;
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
            holder.optionButton = (BootstrapButton) convertView.findViewById(R.id.ride_item_option_button);
            holder.cancelRideButton = (BootstrapButton) convertView.findViewById(R.id.cancel_ride_button);
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
        TravisUtils.addressesFromLocation(parentActivity, pos1.latitude, pos1.longitude, new Callback<List<Address>>() {
            @Override
            public void onResult(List<Address> addressList1) {
                if(addressList1!=null && !addressList1.isEmpty()) {
                    holder.originLabel.setText(TravisUtils.addressToString(addressList1.get(0)));
                }
            }
        });


        LatLng pos2 = r.destinationPosition();
        TravisUtils.addressesFromLocation(parentActivity, pos2.latitude, pos2.longitude, new Callback<List<Address>>() {
            @Override
            public void onResult(List<Address> addressList2) {
                if(addressList2!=null && !addressList2.isEmpty()) {
                    holder.destinationLabel.setText(TravisUtils.addressToString(addressList2.get(0)));
                } else {
                    holder.destinationLabel.setText(parentActivity.getString(R.string.unknown_address));
                }
            }
        });

        if (item.getUserTypeToShow() == RideItem.SHOW_TAXI) {
            holder.optionButton.setText("Set Destination");
            holder.optionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddressPickerDialog.newInstance(parentActivity, new AddressPickerDialog.OnDoneButtonClickListener() {
                        @Override
                        public void onClick(LatLng pickedPosition, String addressText) {
                            Ride r  = item.getRideObject();
                            r.setDestinationLocation(pickedPosition.latitude, pickedPosition.longitude);
                            PersistenceManager.save(r, null);
                            rideListFragment.onRefreshStarted(null);
                            Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();
                            PersistenceManager.stopWatchingRides();
                            PersistenceManager.startWatchingNewRidesForClient(thisClient, parentActivity);
                        }
                    }).show(parentActivity.getSupportFragmentManager(), "DestinationPicker");
                }
            });
        } else {
            holder.optionButton.setText("Start travel");
            holder.optionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainTaxiActivity) parentActivity).startTravel(item.getRideObject());
                    rideListFragment.onRefreshStarted(null);
                }
            });
        }

        holder.cancelRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.remove(r);
                PersistenceManager.delete(r);
                notifyDataSetChanged();

                String cancelString = parentActivity.getString(R.string.scheduled_ride_was_cancelled);
                parentActivity.showTravisNotification(cancelString, null, MainActivity.NotificationColor.DEFAULT);
                rideListFragment.onRefreshStarted(null);
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
        BootstrapButton optionButton;
        BootstrapButton cancelRideButton;
    }
}
