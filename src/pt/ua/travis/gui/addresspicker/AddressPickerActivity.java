package pt.ua.travis.gui.addresspicker;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.utils.Keys;
import pt.ua.travis.utils.Tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AddressPickerActivity extends SherlockFragmentActivity {

    private GoogleMap map;

    private Geocoder geocoder;

    private TextView selectedMarker;

    private AddressPickerAdapter adapter;



    /**
     * History of already looked up addresses, having these mapped by location
     * and optimizing the picking process (since it avoids using the geocoder
     * web service).
     */
    private Map<LatLng, String> addressLookupHistory;
    private LatLng pickedPosition;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_picker);
        getSupportActionBar().hide();
        geocoder = new Geocoder(this);
        addressLookupHistory = new ArrayMap<>();


        // configures the map
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = supportMapFragment.getExtendedMap();

        Location l = Tools.getCurrentLocation(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 15));
        map.setMyLocationEnabled(false);


        //
        selectedMarker = (TextView) findViewById(R.id.selected_address_marker);
        selectedMarker.setText("<none>");


        //
        final AutoCompleteTextView searchField = (AutoCompleteTextView) findViewById(R.id.search_field);
        adapter = new AddressPickerAdapter(this);
        searchField.setAdapter(adapter);
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                Address address = (Address) parent.getItemAtPosition(index);

                if(address!=null) {
                    LatLng position = new LatLng(
                            address.getLatitude(),
                            address.getLongitude());

                    String addressLine = Tools.formatAddress(address);
                    addressLookupHistory.put(position, addressLine);
                    select(position, addressLine);
                }
            }
        });
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    adapter.getFilter().filter(s);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                select(position);
            }
        });
    }

    private void select(LatLng position){
        String addressLine = addressLookupHistory.get(position);

        if(addressLine!=null) {
            select(position, addressLine);

        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
                if (addresses.isEmpty()) {
                    Toast.makeText(AddressPickerActivity.this,
                            "No addresses at the tapped location were found!",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                addressLine = Tools.formatAddress(addresses.get(0));
                addressLookupHistory.put(position, addressLine);
                select(position, addressLine);

            } catch (IOException ex) {
                Log.e("Address lookup", ex.toString());
            }
        }
    }


    private void select(LatLng position, String addressLine){
        map.clear();
        Marker marker = map.addMarker(new MarkerOptions()
                .title(addressLine)
                .position(position));

        marker.showInfoWindow();
        selectedMarker.setText(addressLine);
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
        pickedPosition = position;
    }


    public void onDoneButtonPressed(View view){
        if(pickedPosition!=null){
            Intent intent = new Intent();
            intent.putExtra(Keys.PICKED_POSITION_LAT, pickedPosition.latitude);
            intent.putExtra(Keys.PICKED_POSITION_LNG, pickedPosition.longitude);
            intent.putExtra(Keys.PICKED_POSITION_ADDRESS, addressLookupHistory.get(pickedPosition));
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    public void onCancelButtonPressed(View view){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}