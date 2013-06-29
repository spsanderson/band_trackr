package com.mybandtrackr.android.foursquare;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.ShowEventsActivity;
import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public class EventCheckInActivity extends Activity {

	private static final String TAG = EventCheckInActivity.class.getSimpleName();
	private String mVenueName;
	private FoursquareApi mFoursquareApi;
	private Location mLocation;
	public ProgressDialog pd;
	private Typeface droidSans;
	private Typeface droidSansBold;
	public ListView listView;
	private String mlatlongString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Intent callingIntent = getIntent();
		mVenueName = callingIntent.getStringExtra(getString(R.string.EXTRA_VENUE_NAME));
		mLocation = callingIntent.getParcelableExtra(getString(R.string.EXTRA_LOCATION));
		mlatlongString = String.valueOf(mLocation.getLatitude()) + "," + String.valueOf(mLocation.getLongitude());
		String accessToken = getAccessToken();

		droidSans = Typeface.createFromAsset(getAssets(), "fonts/DroidSans.ttf");
		droidSansBold = Typeface.createFromAsset(getAssets(), "fonts/DroidSans-Bold.ttf");

		if (accessToken == null) {
			startActivityForResult(new Intent(this, OAuthActivity.class), RequestCode.OAUTH.ordinal());
		} else {
			mFoursquareApi = new FoursquareApi(accessToken, new DefaultIOHandler());
			onRefreshClick();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			finish();
			return;
		} else if (resultCode == RESULT_OK) {
			if (requestCode == RequestCode.OAUTH.ordinal()) {
				String accessToken = getAccessToken();
				mFoursquareApi = new FoursquareApi(accessToken, new DefaultIOHandler());
				onRefreshClick();
			} else if (requestCode == RequestCode.ADD_VENUE.ordinal()) {
				onRefreshClick();
			}
		} else if (resultCode == RESULT_FIRST_USER) {
			Util.showToast(data.getStringExtra(getString(R.string.EXTRA_MSG)), EventCheckInActivity.this);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.foursquare_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		// Refresh
		case R.id.refresh:
			onRefreshClick();
			return true;

			// Events service
		case R.id.create_venue:
			createNewVenue();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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

	private void onRefreshClick() {
		VenuesSearch search = new VenuesSearch();
		search.execute(new Void[] { null });
	}

	private void createNewVenue() {

		Intent intent = new Intent(this, VenueAddActivity.class);
		intent.putExtra(getString(R.string.EXTRA_LOCATION), mLocation);
		startActivityForResult(intent, 10);
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

	private void checkIn(View view, final CompactVenue compactVenue) {

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setTitle("Shout..");
		alert.setPositiveButton("Comment", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				if (!"".equals(value)) {
					FoursquareCheckin checkIn = new FoursquareCheckin();
					checkIn.execute(new Object[] { compactVenue, value });
				} else {
					Util.showToast("Comment cannot be empty!", EventCheckInActivity.this);
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});

		alert.show();

	}

	private class VenuesSearch extends AsyncTask<Void, Integer, Result<VenuesSearchResult>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// raise progress dialog and make it cancelable
			pd = App.startProgressDialog(EventCheckInActivity.this, "Foursquare venues search...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					VenuesSearch.this.cancel(true);
					finish();
				}
			});

		}

		@Override
		protected Result<VenuesSearchResult> doInBackground(Void... arg0) {

			Result<VenuesSearchResult> venuesSearch = null;

			try {

				venuesSearch = mFoursquareApi.venuesSearch(mlatlongString, null, null, null, mVenueName, null, "checkin", null, null, null, null);

			} catch (FoursquareApiException e) {
				Log.e(TAG, e.getMessage());
			}

			return venuesSearch;
		}

		@Override
		protected void onPostExecute(final Result<VenuesSearchResult> result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null == listView) {
				listView = new ListView(EventCheckInActivity.this);
				listView.setCacheColorHint(0);
				ColorDrawable listDivider = new ColorDrawable(EventCheckInActivity.this.getResources().getColor(R.color.list_divider));
				listView.setDivider(listDivider);
				listView.setDividerHeight(3);
			} else {
				listView.invalidateViews();
			}

			if (null != result && null != result.getResult() && null != result.getResult().getVenues() && result.getResult().getVenues().length > 0) {

				CompactVenue[] venues = result.getResult().getVenues();

				OnItemClickListener onListViewClickListener = new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						checkIn(view, result.getResult().getVenues()[position]);
					}

				};

				listView.setOnItemClickListener(onListViewClickListener);

				listView.setAdapter(new VenuesSearchResultItemAdapter(EventCheckInActivity.this, R.layout.event_list_item, Arrays.asList(venues)));

			} else {
				listView.setAdapter(new ArrayAdapter<String>(EventCheckInActivity.this, R.layout.simple_list_item,
						new String[] { "Sorry, no venues found to check-in :(" }) {

					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View row;

						if (null == convertView) {
							row = getLayoutInflater().inflate(R.layout.simple_list_item, null);
						} else {
							row = convertView;
						}
						TextView tv = (TextView) row.findViewById(R.id.simple_text);
						tv.setText(getItem(position));

						return row;
					}
				});
			}

			LinearLayout contentLayout = (LinearLayout) EventCheckInActivity.this.findViewById(R.id.content_linear);
			contentLayout.removeAllViews();

			contentLayout.addView(listView);

		}

	}

	private class VenuesSearchResultItemAdapter extends ArrayAdapter<CompactVenue> {

		private List<CompactVenue> items;

		public VenuesSearchResultItemAdapter(Context context, int textViewResourceId, List<CompactVenue> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.event_list_item, null);
			}
			CompactVenue venue = items.get(position);
			if (venue != null) {

				TextView upperText = (TextView) v.findViewById(R.id.event_text_upper);

				if (upperText != null) {
					upperText.setTypeface(droidSansBold);

					upperText.setText(venue.getName());

				}

				TextView lowerText = (TextView) v.findViewById(R.id.event_text_lower);

				if (lowerText != null) {
					lowerText.setTypeface(droidSans);
					Category[] cats = venue.getCategories();
					if (null != cats && cats.length > 0) {
						String categories = "";
						for (int i = 0; i < cats.length; i++) {
							String catName = cats[i].getName();
							if (cats[i].getPrimary()) {
								catName = "[" + cats[i].getName() + "]";
							}
							categories += catName;
							if (i < cats.length - 1) {
								categories += ", ";
							}
						}
						lowerText.setText(categories);
					}
				}
			}
			return v;
		}
	}

	private class FoursquareCheckin extends AsyncTask<Object, Integer, Checkin> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = App.startProgressDialog(EventCheckInActivity.this, "Checking in...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					FoursquareCheckin.this.cancel(true);
				}
			});
		}

		@Override
		protected Checkin doInBackground(Object... params) {
			Result<Checkin> checkin = null;
			try {
				checkin = mFoursquareApi.checkinsAdd(((CompactVenue) params[0]).getId(), ((CompactVenue) params[0]).getName(), (String) params[1], null,
						mlatlongString, null, null, null);
			} catch (FoursquareApiException e) {
				Log.e(TAG, e.getMessage());
			}

			return null != checkin ? checkin.getResult() : null;
		}

		@Override
		protected void onPostExecute(Checkin result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null != result) {
				Util.showToast("Checkin to 4sq successful!", EventCheckInActivity.this);
				finish();
			} else {
				Util.showToast("Error during checkin to 4sq!", EventCheckInActivity.this);
			}
		}
	}

	private enum RequestCode {
		OAUTH, ADD_VENUE
	}

}
