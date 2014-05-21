package pt.ua.travis.ui.addresspicker;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androidmapsextensions.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AddressPickerDialog extends SherlockDialogFragment {

    public interface OnDoneButtonClickListener {

        /**
         * The action that should occur when the user presses the "Done"
         * button after picking an address.
         *
         * @param pickedPosition the picked address's position
         * @param addressText the picked address
         */
        void onClick(LatLng pickedPosition, String addressText);
    }

    private SherlockFragmentActivity parentActivity;

    private OnDoneButtonClickListener onDoneButtonPressed;

    private SupportMapFragment mapFragment;

    private GoogleMap map;

    private TextView selectedMarker;


    /**
     * History of already looked up addresses, having these mapped by location
     * and optimizing the picking process (since it avoids using the geocoder
     * web service).
     */
    private Map<LatLng, String> addressLookupHistory;
    private LatLng pickedPosition;


    public static AddressPickerDialog newInstance(SherlockFragmentActivity parentActivity,
                                                  OnDoneButtonClickListener onDoneButtonPressed) {

        AddressPickerDialog p = new AddressPickerDialog();
        p.parentActivity = parentActivity;
        p.onDoneButtonPressed = onDoneButtonPressed;
        return p;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_address_picker, null);

        addressLookupHistory = Utils.newMap();


        // configures the button click listeners, where done button runs the
        // implemented interface specified in the constructor
        BootstrapButton done = (BootstrapButton) v.findViewById(R.id.done_button);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pickedPosition!=null) {
                    onDoneButtonPressed.onClick(
                            pickedPosition,
                            addressLookupHistory.get(pickedPosition));
                    dismiss();
                }
            }
        });
        BootstrapButton cancel = (BootstrapButton) v.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // configures the map fragment to occupy it's container
        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.map_container, mapFragment)
                .commit();


        // sets the indication of the selected address as none
        selectedMarker = (TextView) v.findViewById(R.id.selected_address_text);
        selectedMarker.setText("<none selected>");


        // configures the auto-complete search field that shows hint-data based
        // on the current geolocation
        final AutoCompleteTextView searchField = (AutoCompleteTextView) v.findViewById(R.id.search_field);
        final AutoCompleteAdapter adapter = new AutoCompleteAdapter(parentActivity);
        searchField.setAdapter(adapter);
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                Address address = (Address) parent.getItemAtPosition(index);

                if(address!=null) {
                    LatLng position = new LatLng(
                            address.getLatitude(),
                            address.getLongitude());

                    String addressLine = Utils.addressToString(address);
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

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        map = mapFragment.getExtendedMap();
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            boolean movedCamera = false;
            @Override
            public void onMyLocationChange(Location location) {
                if(!movedCamera) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    movedCamera = true;
                }
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
            List<Address> addresses = Utils.addressesFromLocation(parentActivity, position.latitude, position.longitude);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(parentActivity,
                        "No addresses at the tapped location were found!",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                addressLine = Utils.addressToString(addresses.get(0));
                addressLookupHistory.put(position, addressLine);
                select(position, addressLine);
            }
        }
    }


    private void select(LatLng position, String addressLine){
        map.clear();
        Marker marker = map.addMarker(new MarkerOptions()
//                .title(addressLine)
                .position(position));

//        marker.showInfoWindow();
        selectedMarker.setText(addressLine);
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
        pickedPosition = position;
    }
}