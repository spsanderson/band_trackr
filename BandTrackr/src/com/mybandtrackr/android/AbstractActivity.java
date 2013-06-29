package com.mybandtrackr.android;

import org.acra.ErrorReporter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mybandtrackr.android.foursquare.EventCheckInActivity;
import com.mybandtrackr.android.maps.GoogleMapsActivity;
import com.mybandtrackr.android.persistence.OAuthDataHelper;
import com.mybandtrackr.android.tripit.TripitTravelInfoActivity;

/**
 * To be extended by all BandTrackr activities<br/>
 * Content view not set by BaseActivity<br/>
 * Is location aware
 * 
 * @author senchi
 * 
 */
public abstract class AbstractActivity extends Activity {

	protected String TAG = null;

	protected Location bestLocation = null;
	protected Address geocodedAddress = null;

	protected boolean showTripItMenu = true;

	protected int eventServiceId;

	private String lastFmUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (null != intent) {
			bestLocation = intent.<Location> getParcelableExtra(getString(R.string.EXTRA_LOCATION));
		}

		// check for user preferred event search service

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		eventServiceId = preferences.getInt(App.KEY_EVENT_SERVICE, 0);
		ErrorReporter.getInstance().putCustomData(App.ERR_CUST_DAT_EVENT_SERVICE, String.valueOf(eventServiceId));

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(4).getSubMenu().getItem(0).setTitle(getPreferences(MODE_PRIVATE).getBoolean(App.KEY_GPS_ENABLED, true) ? "Turn GPS off" : "Turn GPS on");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		// Google Maps
		case R.id.google_maps:
			onGoogleMapsClick();
			return true;

			// Social
		case R.id.janrain:
			onJanrainClick();
			return true;
		case R.id.tripit:
			onTripitClick();
			return true;
		case R.id.foursquare:
			onFoursquareClick(null);
			return true;

			// Settings
		case R.id.gpsenable:
			onGpsEnabledClick();
			return true;
		case R.id.lastfm:
			onLastFmClick();
			return true;
		case R.id.distances:
			onSetDistanceClick();
			return true;

			// Logout
		case R.id.logout:
			onLogoutClick();
			return true;

			// About
		case R.id.about:
			onAboutClick();
			return true;

			// Refresh
		case R.id.refresh:
			onRefreshClick(true);
			return true;

			// Events service
			// case R.id.ticketleap:
			// case R.id.bit:
			// case R.id.eventful:
			// case R.id.bt_default:
		case R.id.event_service:
			onEventServiceClick();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void savePreferences(String prefKey, Object value) {
		Editor editor = getPreferences(MODE_PRIVATE).edit();

		if (value instanceof String) {
			editor.putString(prefKey, (String) value);
		} else if (value instanceof Integer) {
			editor.putInt(prefKey, (Integer) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(prefKey, (Boolean) value);
		} else if (value instanceof Float) {
			editor.putFloat(prefKey, (Float) value);
		} else if (value instanceof Long) {
			editor.putLong(prefKey, (Long) value);
		}

		// Commit the edits!
		editor.commit();

		// save to ACRA error report
		ErrorReporter.getInstance().putCustomData(prefKey, value.toString());
	}

	/**
	 * Top left Logo click event
	 */
	public void onLogoClick(View view) {
		// return home
		Intent intent = new Intent(this, ShowEventsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Options Menu Refresh button click event
	 * 
	 */
	protected abstract void onRefreshClick(boolean askForLocationUpdate);

	/**
	 * Options Menu Event Service click event
	 */
	private void onEventServiceClick() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Choose Event Service");
		alert.setSingleChoiceItems(App.EVENT_SERVICES.toArray(new CharSequence[] {}), App.getEventServiceId(this), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				// store user preference
				eventServiceId = item;
				savePreferences(App.KEY_EVENT_SERVICE, new Integer(eventServiceId));
				Util.showToast("Event Service changed to " + App.EVENT_SERVICES.get(eventServiceId) + ".", AbstractActivity.this);

				dialog.dismiss();
				onRefreshClick(true);
			}
		});

		alert.show();

	}

	/**
	 * Options Menu Logout button click event
	 * 
	 */
	private void onLogoutClick() {

		// remove all auth data
		OAuthDataHelper dataHelper = new OAuthDataHelper(this);
		dataHelper.deleteAll();
		dataHelper.close();

		// log out JREngage
		App.signOutJanrain(this);

		// start login from home screen
		Intent intent = new Intent(getApplicationContext(), BandTrackrActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Options Menu GpsEnabled button click event
	 */
	private void onGpsEnabledClick() {
		// get current state and apply negation
		boolean isGpsEnabled = !getPreferences(MODE_PRIVATE).getBoolean(App.KEY_GPS_ENABLED, true);
		savePreferences(App.KEY_GPS_ENABLED, isGpsEnabled);

		Util.showToast("GPS is now turned " + (isGpsEnabled ? "ON" : "OFF"), this, Toast.LENGTH_SHORT);
	}

	/**
	 * Options Menu Last.fm button click event
	 */
	private void onLastFmClick() {
		showLastFmAlertDialog(false);
	}

	/**
	 * Options Menu Set Distance button click event
	 * 
	 * @param distanceId
	 */
	private void onSetDistanceClick() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Choose Search Radius (km)");
		CharSequence[] array = new String[App.DISTANCES.size()];
		for (int i = 0; i < App.DISTANCES.size(); i++) {
			array[i] = App.DISTANCES.get(i).toString();
		}
		alert.setSingleChoiceItems(array, App.getMaxDistanceId(this), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Util.showToast("Search radius set to " + App.DISTANCES.get(item).toString() + " km", AbstractActivity.this, Toast.LENGTH_SHORT);
				savePreferences(App.KEY_MAX_DISTANCE, item);
				dialog.dismiss();
				onRefreshClick(true);
			}
		});

		alert.show();

	}

	/**
	 * Options Menu About button click event
	 */
	private void onAboutClick() {
		// Show About Alert Dialog
		App.showAboutAlertDialog(this, null);
	}

	/**
	 * Options Menu Google Maps button click event
	 */
	private void onGoogleMapsClick() {
		if (null != bestLocation && !bestLocation.getProvider().equals(getString(R.string.LOC_PROVIDER_PASSIVE))) {
			Intent intent = new Intent(this, GoogleMapsActivity.class);
			intent.putExtra(getString(R.string.EXTRA_LOCATION), bestLocation);
			startActivity(intent);
		} else {
			Util.showToast("Please refresh location first!", this);
		}
	}

	/**
	 * Options Menu Janrain button click event
	 */
	private void onJanrainClick() {
		App.shareViaJanrain(this, null, null, null);
	}

	/**
	 * Options Menu TripIt button click event
	 */
	private void onTripitClick() {
		Intent intent = new Intent(this, TripitTravelInfoActivity.class);
		startActivity(intent);
	}

	/**
	 * Options Menu Foursquare button click event
	 * 
	 * @param eventListIdx
	 */
	protected void onFoursquareClick(String locationName) {
		Intent intent = new Intent(this, EventCheckInActivity.class);
		if (null != bestLocation) {
			intent.putExtra(getString(R.string.EXTRA_LOCATION), bestLocation);
			if (null != locationName) {
				intent.putExtra(getString(R.string.EXTRA_VENUE_NAME), locationName);
			}

			startActivity(intent);
		} else {
			Util.showToast("No location fix for check in!", this);
		}

	}

	protected void showLastFmAlertDialog(final boolean doRefreshOnCancel) {
		new AlertDialog.Builder(this).setTitle("Last.fm").setMessage("Enable my Last.fm account when searching for events?")
				.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Enable", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

						final AlertDialog.Builder alert = new AlertDialog.Builder(AbstractActivity.this);
						final EditText input = new EditText(AbstractActivity.this);
						alert.setView(input);
						alert.setTitle("Please enter your last.fm user name:");
						alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String userName = input.getText().toString().trim();
								if (!"".equals(userName)) {

									lastFmUser = userName;
									savePreferences(App.KEY_LASTFM_NAME, lastFmUser);

									// show events for location
									onRefreshClick(true);

								} else {
									Util.showToast("User name cannot be empty!", AbstractActivity.this);
									dialog.cancel();
								}
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.cancel();

								// show events for location
								onRefreshClick(true);
							}
						});

						alert.show();

					}
				}).setNegativeButton("Disable", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						Toast.makeText(AbstractActivity.this, "Okay, last.fm will be disabled! You can enable it later in Settings in the Options Menu",
								Toast.LENGTH_LONG).show();

						lastFmUser = null;
						savePreferences(App.KEY_LASTFM_NAME, App.CONST_DISABLED);

						// show events for location
						onRefreshClick(true);

					}
				}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

					public void onCancel(DialogInterface dialog) {

						if (doRefreshOnCancel) {
							// show events for location
							onRefreshClick(true);
						}

					}
				}).show();
	}

}
