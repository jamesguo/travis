package pt.ua.travis.maps;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

public class MyCurrentLoctionListener implements LocationListener {

	public String myLocation;
	private TextView mTextView;
	private double lat;
	private double log;

	MyCurrentLoctionListener(TextView tv) {
		this.mTextView = tv;
	}

	@Override
	public void onLocationChanged(Location location) {

		setLat(location.getLatitude());
		setLog(location.getLongitude());
		mTextView.setText(location.getLatitude() + ","
				+ location.getLongitude());

	}

	public double getLat() {
		return lat;

	}

	public double getLog() {
		return log;

	}

	public void setLat(double d) {
		this.lat = d;
	}

	public void setLog(double log) {
		this.log = log;
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
}