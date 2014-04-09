package pt.ua.travis.gui.travel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.mapnavigator.Directions;
import pt.ua.travis.mapnavigator.Navigator;
import pt.ua.travis.utils.CommonKeys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravelToOriginActivity extends SherlockFragmentActivity implements Navigator.OnPathSetListener {

    private GoogleMap map;

    private Ride thisRide;

    private ListView routeList;

    private Navigator navigator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.travel_activity);
        getSupportActionBar().hide();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        map = mapFragment.getMap();
//        routeList = (ListView) findViewById(R.id.route_list);

        thisRide = (Ride) getIntent().getSerializableExtra(CommonKeys.SCHEDULED_RIDE);



    }

    private void getDirection(LatLng destination){

        navigator = new Navigator(map, thisRide.originPosition(), destination);
        navigator.findDirections(true);
        navigator.setOnPathSetListener(this);
    }

    @Override
    public void onPathSetListener(Directions directions) {
//        List<Route> routes = directions.getRoutes();
//        List<String> routesString = Lists.newArrayList(routes.size());
//        for(Route r : routes){
//            routesString.add("From: "+r.getStartAddress()+"\nTo: "+r.getEndAddress());
//        }
//        routeList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routesString));


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(navigator.getStartPoint(), 15));
    }

    public void onAuthentButtonClicked(View view){
        Intent intent = new Intent(this, AuthenticationTaxiActivity.class);
        intent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
        startActivity(intent);
    }

    public void onNotificationButtonClicked(View view){

    }
}