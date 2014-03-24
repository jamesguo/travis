package pt.ua.travis.gui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import pt.ua.travis.R;
import pt.ua.travis.core.Account;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.clientschedule.ClientScheduleActivity;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.gui.main.MainTaxiActivity;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LoginActivity extends Activity {

    public double latitude;
    public double longitude;
    Account a1;
    Account a2;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
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

        TextView e = (TextView) findViewById(R.id.textView5);
        e.setText("Searching...");
        a1 = PersistenceManager.getClientAccount2();
        a2 = PersistenceManager.getTaxiAccount2();
    }


    public void sendMessage2(View view) { // INICIA A ACTIVITY MARKERSTAXI
        Intent intent;
        EditText name = (EditText) findViewById(R.id.editText2);
        EditText pass = (EditText) findViewById(R.id.editText);

        if(pass.getText().toString().equals(a1.pass) && name.getText().toString().equals(a1.user.name)) {
            intent = new Intent(this, MainClientActivity.class);
            startActivity(intent);

        }
        else if(pass.getText().toString().equals(a2.pass) && name.getText().toString().equals(a2.user.name)) {
            intent = new Intent(this, MainTaxiActivity.class);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Dados incorrectos, insira novamente!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onResume() {
        super.onResume();
        // CARREGA OS SERVIÇOS GPS


    }
}