package com.mybandtrackr.android.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationUpdateActivity extends Activity {

	public static final String TAG = LocationUpdateActivity.class.getSimpleName();
	protected static final Handler HANDLER = new Handler();
	private int TIMEOUT_SEC = 15;
	private boolean flagGetGPSDone = false;
	private boolean flagNetworkDone = false;
	private boolean flagGPSEnable = true;
	private boolean flagNetworkEnable = true;

	private LocationManager locationManager;

	private Location currentLocationGPS = null;
	private Location currentLocationNetwork = null;
	private Location bestLocation = null;
	private int counts = 15;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pd = App.startProgressDialog(this, "Looking up location...", true, new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});

		initGPS();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopAllUpdate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startAllUpdate();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopAllUpdate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopAllUpdate();
	}

	private void initGPS() {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (getIntent().getBooleanExtra(App.KEY_GPS_ENABLED, true)) {
			flagGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			flagGPSEnable = false;
		}

		flagNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!flagGPSEnable) {
			flagGetGPSDone = true;
		} else {
			flagGetGPSDone = false;
		}
		if (!flagNetworkEnable) {
			flagNetworkDone = true;
		} else {
			flagNetworkDone = false;
		}

		// start location updates
		startAllUpdate();

		// check for location fix
		HANDLER.postDelayed(locationHandler, 1000);

		bestLocation = null;
		counts = 0;
	}

	private Runnable locationHandler = new Runnable() {
		public void run() {
			counts++;
			// If operation timed out, set flags to retrieve non-null location
			if (counts > TIMEOUT_SEC) {
				flagGetGPSDone = true;
				flagNetworkDone = true;
			}
			bestLocation = getCurrentLocation();

			if (bestLocation == null) {
				// bestLocation == null, continue wait......
				HANDLER.postDelayed(this, 1000);
			} else {

				Intent intent = new Intent();
				intent.putExtra(getString(R.string.EXTRA_LOCATION), bestLocation);

				if (!getString(R.string.LOC_PROVIDER_PASSIVE).equals(bestLocation.getProvider())) {
					Geocoder geocoder = new Geocoder(LocationUpdateActivity.this, Locale.getDefault());
					try {
						List<Address> addresses = geocoder.getFromLocation(bestLocation.getLatitude(), bestLocation.getLongitude(), 1);
						if (null != addresses && !addresses.isEmpty()) {
							intent.putExtra(getString(R.string.EXTRA_ADDRESS), addresses.get(0));
						}
					} catch (IOException e) {
						Log.w(TAG, "Location could not be reverse geocoded! " + e.getMessage());
					}
				}

				setResult(RESULT_OK, intent);

				// set result to caller
				pd.dismiss();
				// close this activity
				finish();

			}
		}
	};

	// turn on the GPS NETWORK update
	private void startAllUpdate() {
		if (flagGPSEnable) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
		}
		if (flagNetworkEnable) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
		}
	}

	// turn off GPS NETWORK update
	private void stopAllUpdate() {
		if (flagGPSEnable) {
			locationManager.removeUpdates(locationListenerGps);
		}
		if (flagNetworkEnable) {
			locationManager.removeUpdates(locationListenerNetwork);
		}
	}

	private final LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			flagGetGPSDone = true;
			flagNetworkDone = true;
			stopAllUpdate();
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private final LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
			flagNetworkDone = true;
			locationManager.removeUpdates(locationListenerNetwork);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private Location getCurrentLocation() {
		Location retLocation = null;
		if ((flagGetGPSDone == true && flagNetworkDone == true)) {
			currentLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			currentLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (currentLocationGPS == null && currentLocationNetwork == null) {
				retLocation = locationManager.getLastKnownLocation(getString(R.string.LOC_PROVIDER_PASSIVE));
				if (retLocation == null) {
					retLocation = new Location(getString(R.string.LOC_PROVIDER_PASSIVE));
				}
			} else {
				// calculate better location fix
				if (LocationUtility.isBetterLocation(currentLocationGPS, currentLocationNetwork)) {
					retLocation = currentLocationGPS;
				} else {
					retLocation = currentLocationNetwork;
				}
			}
			// stop location updates
			stopAllUpdate();
		}
		return retLocation;
	}

}
