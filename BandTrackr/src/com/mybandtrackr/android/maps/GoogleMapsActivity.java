package com.mybandtrackr.android.maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.ShowEventsActivity;
import com.mybandtrackr.android.Util;

public class GoogleMapsActivity extends MapActivity {

	private static final String TAG = GoogleMapsActivity.class.getSimpleName();

	private MapView mapView;
	private GeoPoint geoPoint;
	private Location location;
	private String addressString;
	private Location locationCurrent;

	private boolean isRouteDisplayed = false;

	public Address address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.google_maps);

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
		// mapView.setStreetView(true);

		// ---Add a location marker---
		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);

		locationCurrent = getIntent().<Location> getParcelableExtra(getString(R.string.EXTRA_LOCATION_CURRENT));
		addressString = getIntent().getStringExtra(getString(R.string.EXTRA_ADDRESS_STRING));
		location = getIntent().<Location> getParcelableExtra(getString(R.string.EXTRA_LOCATION));
		if (null != location && !getString(R.string.LOC_PROVIDER_PASSIVE).equals(location.getProvider())) {
			geoPoint = getGeopoint(location);
		} else if (null != addressString) {
			geoPoint = forwardGeocode(addressString);
		}

		if (null != geoPoint) {
			float[] results = new float[1];
			if (null != locationCurrent && !getString(R.string.LOC_PROVIDER_PASSIVE).equals(locationCurrent.getProvider())) {
				Location.distanceBetween(locationCurrent.getLatitude(), locationCurrent.getLongitude(), location.getLatitude(), location.getLongitude(),
						results);
			}
			if (results.length > 0 && results[0] > 50) {
				// if distance is more than 50 m calculate route
				DisplayRoute displayRoute = new DisplayRoute();
				displayRoute.execute(new Location[] { locationCurrent, location });
			} else {
				animateTo(geoPoint, 17, false);
			}
		} else {
			Util.showToast("Location could not be identified!", this);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maps_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		// Google Maps
		case R.id.search_events:
			onSearchEventsClick();
			return true;
		case R.id.search_location:
			onSearchLocatonClick();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return isRouteDisplayed;
	}

	public GeoPoint forwardGeocode(String address) {
		GeoPoint geoPoint = null;
		Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocationName(address, 1);
			if (addresses.size() > 0) {
				geoPoint = getGeopoint(addresses.get(0));
				location = getLocation(geoPoint, LocationManager.GPS_PROVIDER);
			}
		} catch (IOException e) {
			Log.e(TAG, "Unable to forward geocode address (" + address + ")! " + e.getMessage());
		}
		return geoPoint;
	}

	private void animateTo(GeoPoint point, Integer zoomLevel, boolean zoomIn) {
		MapController mc = mapView.getController();
		mc.animateTo(point);
		if (null != zoomLevel) {
			mc.setZoom(zoomLevel);
		}
		if (zoomIn) {
			mc.zoomIn();
		}

		mapView.invalidate();
	}

	private void onSearchEventsClick() {
		Intent intent = new Intent(this, ShowEventsActivity.class);
		intent.putExtra(getString(R.string.EXTRA_LOCATION), location);
		startActivity(intent);
	}

	private void onSearchLocatonClick() {

		final EditText addressView = new EditText(this);
		addressView.setHint("Street, City, Postalcode, Country...");

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Please enter City or Address");
		alert.setView(addressView);
		alert.setCancelable(true);
		alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String address = addressView.getText().toString().trim();
				if (!"".equals(address)) {
					GeoPoint point = forwardGeocode(address);
					if (null != point) {
						animateTo(point, 17, false);
					} else {
						dialog.dismiss();
						Util.showToast("Sorry, location you provided cannot be geocoded! Please try another location.", GoogleMapsActivity.this);
					}
				}
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alert.show();
	}

	private GeoPoint getGeopoint(Location location) {
		return new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
	}

	private GeoPoint getGeopoint(Address address) {
		return new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
	}

	private Location getLocation(GeoPoint geoPoint, String provider) {
		Location location = new Location(provider);
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
	}

	/**
	 * Does the actual drawing of the route, based on the geo points provided in the nav set
	 * 
	 * @param navSet
	 *            Navigation set bean that holds the route information, incl. geo pos
	 * @param color
	 *            Color in which to draw the lines
	 * @param mMapView01
	 *            Map view to draw onto
	 */
	private void drawPath(NavigationDataSet navSet, int color, MapView mMapView01) {

		Log.d(TAG, "map color before: " + color);

		// color correction for dining, make it darker
		if (color == Color.parseColor("#add331"))
			color = Color.parseColor("#6C8715");
		Log.d(TAG, "map color after: " + color);

		Collection<Overlay> overlaysToAddAgain = new ArrayList<Overlay>();
		for (Overlay o : mMapView01.getOverlays()) {
			Log.d(TAG, "overlay type: " + o.getClass().getName());
			if (!RouteOverlay.class.getName().equals(o.getClass().getName())) {
				// mMapView01.getOverlays().remove(o);
				overlaysToAddAgain.add(o);
			}
		}
		mMapView01.getOverlays().clear();
		mMapView01.getOverlays().addAll(overlaysToAddAgain);

		String path = navSet.getRoutePlacemark().getCoordinates();
		Log.d(TAG, "path=" + path);
		if (path != null && path.trim().length() > 0) {
			String[] pairs = path.trim().split(" ");

			Log.d(TAG, "pairs.length=" + pairs.length);

			String[] lngLat = pairs[0].split(","); // lngLat[0]=longitude lngLat[1]=latitude lngLat[2]=height

			Log.d(TAG, "lnglat =" + lngLat + ", length: " + lngLat.length);

			if (lngLat.length < 3)
				lngLat = pairs[1].split(","); // if first pair is not transferred completely, take seconds pair //TODO

			try {
				GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
				mMapView01.getOverlays().add(new RouteOverlay(startGP, startGP, 1));
				GeoPoint gp1;
				GeoPoint gp2 = startGP;

				for (int i = 1; i < pairs.length; i++) // the last one would be crash
				{
					lngLat = pairs[i].split(",");

					gp1 = gp2;

					if (lngLat.length >= 2 && gp1.getLatitudeE6() > 0 && gp1.getLongitudeE6() > 0 && gp2.getLatitudeE6() > 0 && gp2.getLongitudeE6() > 0) {

						// for GeoPoint, first:latitude, second:longitude
						gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));

						if (gp2.getLatitudeE6() != 22200000) {
							mMapView01.getOverlays().add(new RouteOverlay(gp1, gp2, 2, color));
							Log.d(TAG, "draw:" + gp1.getLatitudeE6() + "/" + gp1.getLongitudeE6() + " TO " + gp2.getLatitudeE6() + "/" + gp2.getLongitudeE6());
						}
					}
					// Log.d(TAG,"pair:" + pairs[i]);
				}
				// routeOverlays.add(new RouteOverlay(gp2,gp2, 3));
				mMapView01.getOverlays().add(new RouteOverlay(gp2, gp2, 3));
			} catch (NumberFormatException e) {
				Log.e(TAG, "Cannot draw route.", e);
			}
		}
		// mMapView01.getOverlays().addAll(routeOverlays); // use the default color
		mMapView01.setEnabled(true);
	}

	private class DisplayRoute extends AsyncTask<Location, Integer, NavigationDataSet> {

		private ProgressDialog pd;
		private Location locationFrom;
		private Location locationTo;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = App.startProgressDialog(GoogleMapsActivity.this, "Calculating route...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					DisplayRoute.this.cancel(true);
				}
			});
		}

		@Override
		protected NavigationDataSet doInBackground(Location... params) {
			locationFrom = params[0];
			locationTo = params[1];

			return MapService.calculateRoute(locationFrom.getLatitude(), locationFrom.getLongitude(), locationTo.getLatitude(), locationTo.getLongitude(),
					MapService.MODE_CAR);
		}

		@Override
		protected void onPostExecute(NavigationDataSet success) {
			super.onPostExecute(success);

			pd.dismiss();

			// set flag
			if (null != success) {
				drawPath(success, 999, mapView);
				isRouteDisplayed = true;
			}

			animateTo(getGeopoint(locationTo), 14, false);

		}
	}

	private class MapOverlay extends com.google.android.maps.Overlay {

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
			super.draw(canvas, mapView, shadow);

			// ---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(geoPoint, screenPts);

			// ---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pushpin);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 40, null);
			return true;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			geoPoint = p;
			location = getLocation(p, LocationManager.GPS_PROVIDER);
			animateTo(geoPoint, null, false);
			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			try {
				List<Address> addresses = geoCoder.getFromLocation(geoPoint.getLatitudeE6() / 1E6, geoPoint.getLongitudeE6() / 1E6, 1);

				String add = "";
				if (addresses.size() > 0) {
					address = addresses.get(0);
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
						add += address.getAddressLine(i) + "\n";
				}

				Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Log.w(TAG, "Unable to reverse geocode geoPoint " + geoPoint.toString() + "! " + e.getMessage());
			}

			return true;

		}

	}

}
