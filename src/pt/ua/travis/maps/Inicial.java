package pt.ua.travis.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import pt.ua.travis.R;

public class Inicial extends Activity {

	LocationManager locationManager;
	MyCurrentLoctionListener locationListener;
	TextView e;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inicial);
		e = (TextView) findViewById(R.id.textView3);
		e.setText("Searching...");

	}

	public void sendMessage(View view) { // INICIA A ACTIVITY MAINACTIVITY
		Intent intent = new Intent(this, MainActivity.class);

		double lat = locationListener.getLat();
		double log = locationListener.getLog();
		intent.putExtra("LAT", lat);
		intent.putExtra("LOG", log);
		// Log.d("LAT", String.valueOf(lat));
		// Log.d("LOG", String.valueOf(log));
		startActivity(intent);
	}

	public void sendMessage2(View view) { // INICIA A ACTIVITY MARKERSTAXI
		Intent intent = new Intent(this, MarkersTaxi.class);
		double lat = locationListener.getLat();
		double log = locationListener.getLog();
		intent.putExtra("LAT", lat);
		intent.putExtra("LOG", log);
		// Log.d("LAT", String.valueOf(lat));
		// Log.d("LOG", String.valueOf(log));
		startActivity(intent);
	}

	protected void onResume() {
		super.onResume();
		// CARREGA OS SERVI�OS GPS
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyCurrentLoctionListener(e);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.inicial, menu);
		return true;
	}

}
