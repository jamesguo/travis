package pt.ua.travis.gui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.core.User;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.gui.main.MainTaxiActivity;
import pt.ua.travis.utils.CommonKeys;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LoginActivity extends SherlockActivity {

    private SharedPreferences prefs;
    private BootstrapEditText usernameField, passField;
    private CheckBox rememberPassCheckbox;

    private double latitude;
    private double longitude;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();

        prefs = this.getSharedPreferences("TravisPreferences", MODE_PRIVATE);

        usernameField = (BootstrapEditText) findViewById(R.id.username_field);
        passField = (BootstrapEditText) findViewById(R.id.pass_field);
        final TextView gpsStatus = (TextView) findViewById(R.id.gps_status);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                gpsStatus.setText("Available");
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        });

        rememberPassCheckbox = (CheckBox) findViewById(R.id.remember_pass_checkbox);
        rememberPassCheckbox.setChecked(prefs.getBoolean(CommonKeys.REMEMBER_PASS_CHECKED, false));

        gpsStatus.setText("Searching...");
    }

    public void onRegisterButtonClicked(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onLoginButtonClicked(View view) {

        prefs.edit().putBoolean(CommonKeys.REMEMBER_PASS_CHECKED, rememberPassCheckbox.isChecked()).commit();

        String usernameFieldText = usernameField.getText().toString();
        String passFieldText = passField.getText().toString();

        try {
            Intent intent = null;

            MessageDigest md = MessageDigest.getInstance("SHA1");
            String passMD = new String(md.digest(passFieldText.getBytes()));

            for(User user : PersistenceManager.selectUsersWithPasswordDigest(passMD)) {
                if (usernameFieldText.equals(user.userName)) {

                    if(user instanceof Client) {
                        intent = new Intent(this, MainClientActivity.class);
                    } else if (user instanceof Taxi){
                        intent = new Intent(this, MainTaxiActivity.class);
                    }
                }
            }

            if(intent!=null){
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, getResources().getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                passField.getText().clear();
            }

        }catch (GeneralSecurityException ex){}
    }
}