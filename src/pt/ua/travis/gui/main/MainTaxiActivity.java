package pt.ua.travis.gui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.gui.ridelist.RideListFragment;
import pt.ua.travis.gui.travel.TravelToOriginActivity;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Tools;
import pt.ua.travis.utils.Validate;

// TODO: BACK STACK NEEDS WORK!

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainTaxiActivity extends MainActivity {

    private SlideHolder sideMenu;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_taxi_activity);

        CommonRes.initialize(this);

        goToScheduledRidesList(null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

        configureSideMenu(menu);

        configureSearchBar(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void configureSideMenu(Menu menu){
        sideMenu = (SlideHolder) findViewById(R.id.sideMenu);
        sideMenu.setDirection(SlideHolder.DIRECTION_LEFT);
        sideMenu.setAllowInterceptTouch(false);
        sideMenu.setEnabled(false);
        if(Validate.isTablet(this) && Validate.isLandscape(this)){
            sideMenu.setAlwaysOpened(true);
        } else {
            sideMenu.setOnSlideListener(new SlideHolder.OnSlideListener() {
                @Override
                public void onSlideCompleted(boolean b) {
                    sideMenu.setEnabled(b);
                }
            });
        }

        MenuItem toggleItem = menu.findItem(R.id.action_side_menu_toggle);
        if (toggleItem != null) {
            final int toggleButtonID = toggleItem.getItemId();
            toggleItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == toggleButtonID)
                        sideMenu.toggle();
                    return true;
                }
            });
        }

        // load client specific data in the side menu
        Taxi thisTaxi = PersistenceManager.selectThisTaxiAccount();
        int numOfRides = PersistenceManager.selectRidesFromTaxi().size();

        String imageUrl = thisTaxi.imageUri;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) findViewById(R.id.photo);
            Picasso.with(this).load(imageUrl).into(photoView);
        }

        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(thisTaxi.realName);

        TextView ridesCounter = (TextView) findViewById(R.id.rides_counter);
        ridesCounter.setText("" + numOfRides);
    }



    private void configureSearchBar(Menu menu){

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();


        // if a search item is collapsed, resets the shown taxis
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                goToScheduledRidesList(null);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                searchItem.collapseActionView();
            }
        });

        // sets a listener to filter results when the user submits a query in the search box
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty())
                    return false;

//                filteredTaxiList = new ArrayList<>();
//                String queryLC = query.toLowerCase();
//
//                // looks for matches and adds them to a temporary list
//                for (Taxi t : taxiList) {
//                    String nameLC = t.realName.toLowerCase();
//
//                    if (nameLC.contains(queryLC))
//                        filteredTaxiList.add(t);
//                }
//
//
//                // resets the fragments and subsequent adapter to show the filtered list with matches
//                showFilteredResults(0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void goToScheduledRidesList(View view){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, RideListFragment.newInstance(RideItem.SHOW_CLIENT, PersistenceManager.selectRidesFromTaxi()))
                .addToBackStack(null)
                .commit();

        if(sideMenu!=null)
            sideMenu.close();
    }

    @Override
    public void onDeletedRide(){
        goToScheduledRidesList(null);
    }

    public void onAuthentButtonClicked(View view){
        final Ride thisRide = PersistenceManager.selectRidesFromTaxi().get(0);

        Location currentLoc = Tools.getCurrentLocation(this);
        thisRide.originLat = currentLoc.getLatitude();
        thisRide.originLng = currentLoc.getLongitude();

        Context context = MainTaxiActivity.this;

        Intent resultIntent = new Intent(context, TravelToOriginActivity.class);
        resultIntent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification launches a new ("special") activity,
        // there's no need to create an artificial back stack.
        PendingIntent piAccept = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, MainTaxiActivity.class);
//        dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
        PendingIntent piDecline = PendingIntent.getActivity(this, 0, dismissIntent, 0);

        String msg = thisRide.client.realName+" requested you a ride "+
                thisRide.getRemaining()+".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.cabs)
                .setContentTitle("Ride Request from "+thisRide.client.realName)
                .setContentText(msg)
                .setTicker("Ride Request from "+thisRide.client.realName)
//                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .addAction (R.drawable.ic_action_accept,
                        getString(R.string.accept), piAccept)
                .addAction (R.drawable.ic_action_cancel,
                        getString(R.string.decline), piDecline);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(101, n);
    }

    @Override
    public void logout(View view) {
        super.logout(view);
    }
}