package pt.ua.travis.ui.riderequest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.travel.AuthenticationClientActivity;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.utils.CommonKeys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestAcceptedDialog extends SimpleDialogFragment {

    private MainClientActivity parentActivity;
    private Ride thisRide;

    public static RideRequestAcceptedDialog newInstance(MainClientActivity parentActivity, String rideID) {
        RideRequestAcceptedDialog instance = new RideRequestAcceptedDialog();
        instance.parentActivity = parentActivity;
        instance.thisRide = PersistenceManager.getFromCache(rideID);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.id.dialog_ride_request_accepted, null);

//        BootstrapButton btDestination = (BootstrapButton) v.findViewById(R.id.btDestination);
//        if(hasDestination)
//            btDestination.setBootstrapType("default");
//
//        RideItem rideItem = RideItem.newInstance(RideItem.SHOW_TAXI, thisRide, this);
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.ride_item_container, rideItem)
//                .commit();
        return v;
    }

    public void onDestinationButtonClicked(View view){
//        AddressPickerDialog.newInstance(this, new AddressPickerDialog.OnDoneButtonClickListener() {
//            @Override
//            public void onClick(LatLng pickedPosition, String addressText) {
//                thisRide.setDestinationLocation(pickedPosition.latitude, pickedPosition.longitude);
//                refreshActivity(true);
//
//            }
//        }).show(getSupportFragmentManager(), "DestinationAddressPickerDialog");
    }

    public void onAuthentButtonClicked(View view){
        Taxi taxi = thisRide.taxi();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(parentActivity)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.cabs)
                .setContentTitle("Taxi arrived!")
                .setContentText(taxi.name() + " has arrived at the specified " +
                        "origin location! Press this to proceed!")
                .setTicker(taxi.name() + " has arrived!")
                .setWhen(System.currentTimeMillis());

        Picasso.with(parentActivity).load(taxi.imageUri()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                builder.setLargeIcon(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
            }
        });

        Intent intent = new Intent(parentActivity, AuthenticationClientActivity.class);
        intent.putExtra(CommonKeys.SCHEDULED_RIDE_ID, thisRide.id());
        PersistenceManager.addToCache(thisRide);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(parentActivity);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainTaxiActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(103, n);
    }
}