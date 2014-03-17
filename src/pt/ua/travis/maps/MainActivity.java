package pt.ua.travis.maps;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import pt.ua.travis.R;

public class MainActivity extends FragmentActivity {

	private GoogleMap map;

	/*
	 * private LatLng LOCALIZACAO; Button btn1; LocationManager locationManager;
	 * MyCurrentLoctionListener locationListener; Bundle extras; private double
	 * lat; private double log; private LatLng actual;
	 */

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mainview = inflater.inflate(R.layout.activity_main, null);
		return mainview;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// setLocation();

		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		map = fm.getMap();

		LatLng mapCenter = new LatLng(41.889, -87.622);

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 13));

		// Flat markers will rotate when the map is rotated,
		// and change perspective when the map is tilted.
		map.addMarker(new MarkerOptions()
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_launcher))
				.position(mapCenter).flat(true).rotation(245));

		CameraPosition cameraPosition = CameraPosition.builder()
				.target(mapCenter).zoom(13).bearing(90).build();

		// Animate the change in camera view over 2 seconds
		map.animateCamera(
				CameraUpdateFactory.newCameraPosition(cameraPosition), 2000,
				null);
	}

	/*
	 * protected void onResume() { super.onResume(); LOCALIZACAO = new
	 * LatLng(lat, log); SupportMapFragment fm = (SupportMapFragment)
	 * getSupportFragmentManager() .findFragmentById(R.id.map); map =
	 * fm.getMap();
	 * map.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCALIZACAO, 15));
	 * map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
	 * map.setMyLocationEnabled(true);
	 * map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	 * 
	 * map.addMarker(new MarkerOptions().title("LOCALIZACAO ACTUAL")
	 * .snippet("AQUI VAI DEPOIS A DESCRI��O").position(LOCALIZACAO)); }
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * public LatLng getActual() { return actual; }
	 * 
	 * public void setActual(LatLng actual) { this.actual = actual; }
	 * 
	 * public void setLocation() { extras = getIntent().getExtras();
	 * 
	 * if (getIntent().hasExtra("LAT")) { lat = extras.getDouble("LAT"); } if
	 * (getIntent().hasExtra("LOG")) { log = extras.getDouble("LOG"); }
	 * setActual(new LatLng(lat, log)); }
	 */

}