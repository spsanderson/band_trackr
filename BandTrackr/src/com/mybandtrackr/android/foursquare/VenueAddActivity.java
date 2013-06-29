package com.mybandtrackr.android.foursquare;

import java.util.Map;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.ResultMeta;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompleteVenue;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class VenueAddActivity extends Activity {

	private static final String TAG = VenueAddActivity.class.getSimpleName();
	private FoursquareApi mFoursquareApi;
	private Category[] result;
	private String[] categoryStrings;
	private Location location;
	private String latLongString;

	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.foursquare_venue_add);

		Intent intent = getIntent();
		if (null != intent) {
			location = intent.<Location> getParcelableExtra(getString(R.string.EXTRA_LOCATION));
			if (null != location) {

				String accessToken = getAccessToken();
				if (null != accessToken) {

					latLongString = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());

					mFoursquareApi = new FoursquareApi(accessToken, new DefaultIOHandler());

					Prepare4sqCategories prepare = new Prepare4sqCategories();
					prepare.execute(new Void[] { null });

				} else {
					finish("User not authenticated to Foursquare!", true);
				}
			} else {
				finish("Location not provided!", true);
			}
		}

	}

	private class Prepare4sqCategories extends AsyncTask<Void, Integer, Result<Category[]>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = App.startProgressDialog(VenueAddActivity.this, "Preparing submit form...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					Prepare4sqCategories.this.cancel(true);
				}
			});
		}

		@Override
		protected Result<Category[]> doInBackground(Void... params) {

			Result<Category[]> result = null;

			// TODO: check why venue add not working when adding category
			try {
				result = mFoursquareApi.venuesCategories();

			} catch (FoursquareApiException e) {
				finish("Error gettting 4sq categories! " + e.getMessage(), true);
			}

			return result;
		}

		@Override
		protected void onPostExecute(Result<Category[]> venuesCategories) {
			super.onPostExecute(venuesCategories);

			pd.dismiss();

			if (null != venuesCategories && null != venuesCategories.getResult() && venuesCategories.getResult().length > 0) {

				Spinner spinner = (Spinner) findViewById(R.id.venue_add_category);
				result = venuesCategories.getResult();
				categoryStrings = new String[result.length];
				int i = 0;
				for (Category cat : result) {
					categoryStrings[i] = cat.getName();
					i++;
				}

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(VenueAddActivity.this, android.R.layout.simple_spinner_item, categoryStrings);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);

			}

			Button save = (Button) findViewById(R.id.venue_add_save);

			save.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					addVenue();
				}
			});

		}
	}

	private void addVenue() {
		EditText tvName = (EditText) findViewById(R.id.venue_add_name);
		EditText tvAddress = (EditText) findViewById(R.id.venue_add_address);
		EditText tvCrossStreet = (EditText) findViewById(R.id.venue_add_cross_street);
		EditText tvCity = (EditText) findViewById(R.id.venue_add_city);
		EditText tvState = (EditText) findViewById(R.id.venue_add_state);
		EditText tvZip = (EditText) findViewById(R.id.venue_add_zip);
		EditText tvPhone = (EditText) findViewById(R.id.venue_add_phone);
		Spinner spCat = (Spinner) findViewById(R.id.venue_add_category);

		String name = tvName.getText().toString().trim();
		if ("".equals(name)) {
			finish("Venue name cannot be empty!", true);
		} else {
			String address = tvAddress.getText().toString().trim();
			String crossStreet = tvCrossStreet.getText().toString().trim();
			String city = tvCity.getText().toString().trim();
			String state = tvState.getText().toString().trim();
			String zip = tvZip.getText().toString().trim();
			String phone = tvPhone.getText().toString().trim();
			String primCatId = null != result ? result[spCat.getSelectedItemPosition()].getId() : null;

			AddVenue addVenue = new AddVenue();
			addVenue.execute(new String[] { name, address, crossStreet, city, state, zip, phone, primCatId });

		}
	}

	private void finish(String msg, boolean error) {
		Intent intent = new Intent();
		intent.putExtra(getString(R.string.EXTRA_MSG), msg);
		if (error) {
			Log.e(TAG, msg);
			setResult(RESULT_FIRST_USER, intent);
		} else {
			Log.d(TAG, msg);
			setResult(RESULT_OK, intent);
		}
		finish();
	}

	private String getAccessToken() {
		OAuthDataHelper oAuthDataHelper = new OAuthDataHelper(getApplicationContext());
		Map<String, Map<String, String>> selectAll = oAuthDataHelper.selectAll();
		oAuthDataHelper.close();

		Map<String, String> map = selectAll.get(FoursquareApi.OAUTH_PROVIDER);
		if (null != map && "1".equals(map.get("is_authorized"))) {
			return map.get("oauth_token");
		} else {
			return null;
		}
	}

	private class AddVenue extends AsyncTask<String, Integer, Result<CompleteVenue>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(VenueAddActivity.this, "Adding Venue...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					AddVenue.this.cancel(true);
				}
			});
		}

		@Override
		protected Result<CompleteVenue> doInBackground(String... params) {
			String name = params[0];
			String address = setString(params[1]);
			String crossStreet = setString(params[2]);
			String city = setString(params[3]);
			String state = setString(params[4]);
			String zip = setString(params[5]);
			String phone = setString(params[6]);
			String primaryCategoryId = setString(params[7]);

			Result<CompleteVenue> result = null;

			try {
				result = mFoursquareApi.venuesAdd(name, address, crossStreet, city, state, zip, phone, latLongString, primaryCategoryId);
			} catch (FoursquareApiException e) {
				Log.e(TAG, e.getMessage());
			}

			return result;
		}

		private String setString(String string) {
			return "".equals(string) ? null : string;
		}

		@Override
		protected void onPostExecute(Result<CompleteVenue> result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null != result) {
				CompleteVenue venue = result.getResult();
				if (null != venue) {
					finish("Venue added!", false);
				} else {
					ResultMeta meta = result.getMeta();
					finish("Error adding venue! " + meta.getErrorDetail(), true);
				}
			} else {
				finish("Error adding venue!", true);
			}
		}

	}

}
