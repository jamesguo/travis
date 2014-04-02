package pt.ua.travis.gui.taxiridesetup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.gui.addresspicker.AddressPickerDialog;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.gui.main.MainTaxiActivity;
import pt.ua.travis.gui.ridelist.RideDeletedListener;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.gui.travel.AuthenticationClientActivity;
import pt.ua.travis.utils.CommonKeys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class WaitForTaxiActivity extends SherlockFragmentActivity implements RideDeletedListener {

    private Ride newRide;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        newRide = (Ride) getIntent().getSerializableExtra(CommonKeys.SCHEDULED_RIDE);

        refreshActivity(false);
    }

    private void refreshActivity(boolean hasDestination){
        setContentView(R.layout.wait_for_taxi);

        BootstrapButton btDestination = (BootstrapButton) findViewById(R.id.btDestination);
        if(hasDestination)
            btDestination.setBootstrapType("default");

        RideItem rideItem = RideItem.newInstance(RideItem.SHOW_TAXI, newRide, this);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.ride_item_container, rideItem)
                .commit();
    }

    public void onDestinationButtonClicked(View view){
        AddressPickerDialog.newInstance(this, new AddressPickerDialog.OnDoneButtonClickListener() {
            @Override
            public void onClick(LatLng pickedPosition, String addressText) {
                newRide.destinationLat = pickedPosition.latitude;
                newRide.destinationLng = pickedPosition.longitude;
                newRide.destinationAddress = addressText;
                refreshActivity(true);

            }
        }).show(getSupportFragmentManager(), "DestinationAddressPickerDialog");
    }

    public void onAnotherTaxiButtonClicked(View view){
        Intent intent = new Intent(this, MainClientActivity.class);
        startActivity(intent);
    }

    public void onSeeMoreRidesButtonClicked(View view){
        Intent intent = new Intent(this, MainClientActivity.class);
        intent.putExtra(CommonKeys.GO_TO_RIDE_LIST, 1);
        startActivity(intent);
    }

    @Override
    public void onDeletedRide(){
        onAnotherTaxiButtonClicked(null);
    }

    public void onAuthentButtonClicked(View view){
        Context context = WaitForTaxiActivity.this;

        final Notification.Builder builder = new Notification.Builder(context)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.cabs)
                .setContentTitle("Taxi arrived!")
                .setContentText(newRide.taxi.realName + " has arrived at the specified origin location! Press this to proceed!")
                .setTicker(newRide.taxi.realName + " has arrived!")
                .setWhen(System.currentTimeMillis());

        Picasso.with(context).load(newRide.taxi.imageUri).into(new Target() {
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

        Intent intent = new Intent(this, AuthenticationClientActivity.class);
        intent.putExtra(CommonKeys.SCHEDULED_RIDE, newRide);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainTaxiActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(103, n);
    }
}