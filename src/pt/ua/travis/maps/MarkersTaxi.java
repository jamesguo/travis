package pt.ua.travis.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import pt.ua.travis.R;

import java.util.ArrayList;

public class MarkersTaxi extends FragmentActivity {

	LocationManager locationManager;
	MyCurrentLoctionListener locationListener;
	private GoogleMap map;
	Bundle extras;
	private double lat;
	private double log;
	private LatLng actual;
	SupportMapFragment fm;
	AlertDialog.Builder bb;
	AlertDialog.Builder builder;
	private static final int DIALOG_ALERT = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_markers_taxi);
		setLocation();
		ArrayList<LatLng> list = new ArrayList<LatLng>();
		bb = new AlertDialog.Builder(this);
		builder = new AlertDialog.Builder(this);

		// /COORDENADAS QUE VIRAM DA BASE DE DADOS PARA A LOCALIZA��O ACTUAL DOS
		// TAXIS
		list.add(new LatLng(40.646808, -8.662223));
		list.add(new LatLng(40.635606, -8.659305));
		list.add(new LatLng(40.645831, -8.640680));
		list.add(new LatLng(40.635411, -8.619823));

		String[] titulo = { "Aqui", "Ali", "Acol�", "Al�m" };
		String[] descricao = { "EU ESTOU AQUI", "EU ESTOU ALI",
				"EU ESTOU ACOL�", "EU ESTOU AL�M" };

		// CARREGA MAPA E AS SUAS DEFINI��ES
		fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(
				R.id.map);
		map = fm.getMap();
		map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

		Toast.makeText(this, String.valueOf(list.size()), Toast.LENGTH_SHORT)
				.show();

		// INSER��O DOS MARCADORES COM OS SEUS DETALHES
		for (int i = 0; i < list.size(); i++) {
			map.addMarker(new MarkerOptions()
					.position(list.get(i))
					.title(titulo[i])
					.snippet(descricao[i])
					.visible(true)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.cabs)));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 15));

		}

		// Eventos para ao clicar aparecer informa��o e ao clicar na informa��o
		// aparecer dialogbox
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {

				if (arg0.getTitle().equals("Aqui")) {
					arg0.showInfoWindow();
					// LISTENER PARA A INFOWINDOW
					map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						public void onInfoWindowClick(Marker arg0) {
							Toast.makeText(
									MarkersTaxi.this,
									"Queres boleia do: " + arg0.getTitle()
											+ " ?", Toast.LENGTH_SHORT).show();
							showDialog(DIALOG_ALERT);
						}
					});
					return true;
				}
				if (arg0.getTitle().equals("Ali")) {
					arg0.showInfoWindow();
					// LISTENER PARA A INFOWINDOW
					map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						public void onInfoWindowClick(Marker arg0) {
							Toast.makeText(
									MarkersTaxi.this,
									"Queres boleia do: " + arg0.getTitle()
											+ " ?", Toast.LENGTH_SHORT).show();
							showDialog(DIALOG_ALERT);
						}
					});
					return true;
				}
				if (arg0.getTitle().equals("Acol�")) {
					arg0.showInfoWindow();
					// LISTENER PARA A INFOWINDOW
					map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						public void onInfoWindowClick(Marker arg0) {
							Toast.makeText(
									MarkersTaxi.this,
									"Queres boleia do: " + arg0.getTitle()
											+ " ?", Toast.LENGTH_SHORT).show();
							showDialog(DIALOG_ALERT);
						}
					});
					return true;
				}
				if (arg0.getTitle().equals("Al�m")) {
					arg0.showInfoWindow();
					// LISTENER PARA A INFOWINDOW
					map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						public void onInfoWindowClick(Marker arg0) {
							Toast.makeText(
									MarkersTaxi.this,
									"Queres boleia do: " + arg0.getTitle()
											+ " ?", Toast.LENGTH_SHORT).show();
							showDialog(DIALOG_ALERT);
						}
					});
					return true;
				}
				return false;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.markers_taxi, menu);
		return true;
	}

	public void setLocation() {
		extras = getIntent().getExtras();

		if (getIntent().hasExtra("LAT")) {
			lat = extras.getDouble("LAT");
		}
		if (getIntent().hasExtra("LOG")) {
			log = extras.getDouble("LOG");
		}
		setActual(new LatLng(lat, log));
	}

	public LatLng getActual() {
		return actual;
	}

	public void setActual(LatLng actual) {
		this.actual = actual;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ALERT:
			android.app.AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setMessage("Quer o servi�o deste t�xi?");
			builder.setCancelable(true);
			builder.setPositiveButton("Sim", new OkOnClickListener());
			builder.setNegativeButton("N�o", new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return super.onCreateDialog(id);
	}

	private final class CancelOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(getApplicationContext(),
					"Cancle selected, activity continues", Toast.LENGTH_LONG)
					.show();
		}
	}

	private final class OkOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(getApplicationContext(), "T�xi Escolhido!",
					Toast.LENGTH_LONG).show();
		}
	}

}
