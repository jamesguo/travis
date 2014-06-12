package pt.ua.travis.ui.addresspicker;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.core.TravisMapFragment;
import pt.ua.travis.utils.TravisUtils;

import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AddressPickerDialog extends SimpleDialogFragment {

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

    private OnDoneButtonClickListener listener;

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


    public static AddressPickerDialog newInstance(final SherlockFragmentActivity parentActivity,
                                                  final OnDoneButtonClickListener listener) {

        AddressPickerDialog instance = new AddressPickerDialog();
        instance.parentActivity = parentActivity;
        instance.listener = listener;
        return instance;
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        builder.setView(generateView(inflater));
        builder.setPositiveButton(R.string.done, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(pickedPosition, addressLookupHistory.get(pickedPosition));
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyleLight_FullScreen);
    }


    public View generateView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.dialog_address_picker, null);
        addressLookupHistory = TravisUtils.newMap();


        // configures the map fragment to occupy it's container
        mapFragment = new TravisMapFragment();
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

                    String addressLine = TravisUtils.addressToString(address);
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

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity)
//                .setView(generateView(parentActivity.getLayoutInflater()))
//                .setTitle("")
//                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        listener.onClick(pickedPosition, addressLookupHistory.get(pickedPosition));
//                        dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dismiss();
//                    }
//                });
//        Dialog d = builder.create();
//
//        d.requestWindowFeature(DialogInterface.BUTTON_POSITIVE);
//        d.requestWindowFeature(DialogInterface.BUTTON_NEGATIVE);
//        return d;
//    }

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
            List<Address> addresses = TravisUtils.addressesFromLocation(parentActivity, position.latitude, position.longitude);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(parentActivity,
                        "No addresses at the tapped location were found!",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                addressLine = TravisUtils.addressToString(addresses.get(0));
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