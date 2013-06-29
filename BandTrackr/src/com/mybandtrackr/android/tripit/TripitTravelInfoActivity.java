package com.mybandtrackr.android.tripit;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.ShowEventsActivity;
import com.mybandtrackr.android.domain.Trip;
import com.mybandtrackr.android.domain.TripInfo;
import com.mybandtrackr.android.persistence.OAuthDataHelper;
import com.mybandtrackr.android.protocol.AuthData;
import com.mybandtrackr.android.protocol.CustomHttpClient;
import com.mybandtrackr.android.protocol.CustomHttpClient.ReturnType;
import com.mybandtrackr.android.protocol.OAuthActivity;
import com.tripit.api.Client;
import com.tripit.auth.OAuthCredential;

public class TripitTravelInfoActivity extends Activity {

	private static final String TAG = TripitTravelInfoActivity.class.getSimpleName();

	private final String API_URL_PREFIX = Client.DEFAULT_API_URI_PREFIX;
	private final String WEB_URL_PREFIX = "https://m.tripit.com";

	private final String REQUEST_TOKEN_URI = API_URL_PREFIX + "/oauth/request_token";
	private final String AUTH_WEB_URI = WEB_URL_PREFIX + "/oauth/authorize";
	private final String ACCESS_TOKEN_URI = API_URL_PREFIX + "/oauth/access_token";

	private String consumerKey = "ee6ae1d67b3ea1c2d45181592f73b309a4c6a6b2";
	private String consumerSecret = "858eb4c5f9b831bd59f8db8175fe31501db089f0";

	private OAuthDataHelper oauthDataHelper;
	private final String OAUTH_DB_KEY = "tripit";

	private OAuthConsumer oauthConsumer;

	private ProgressDialog pd;

	private ListView mListView;

	private boolean authAttended = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		setContentView(R.layout.main);

		// initialize SQLite OAuth storage
		oauthDataHelper = new OAuthDataHelper(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		if (null == oauthConsumer) {
			oauthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
			oauthConsumer.setMessageSigner(new HmacSha1MessageSigner());
		}

		// check if user is already authorized
		if (!isUserAuthorized()) {
			if (authAttended) {

				final AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setMessage("Authentication to TripIt failed. Would you like to try again?");
				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						startOAuthActivity();
					}
				});

				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
						finish();
					}
				});

				alert.show();

			} else {

				startOAuthActivity();

			}
		} else {

			lookupTravelInfo();
		}

	}

	private void startOAuthActivity() {
		authAttended = true;
		Intent intent = new Intent(this, OAuthActivity.class);
		setOauthUserData(intent);
		startActivity(intent);
	}

	private boolean isUserAuthorized() {
		Map<String, Map<String, String>> oauthProviders = oauthDataHelper.selectAll();

		if (oauthProviders.containsKey(OAUTH_DB_KEY) && oauthProviders.get(OAUTH_DB_KEY).get("is_authorized").equals("1")) {
			// user already authorized with TripIt
			oauthConsumer.setTokenWithSecret(oauthProviders.get(OAUTH_DB_KEY).get("oauth_token"), oauthProviders.get(OAUTH_DB_KEY).get("oauth_token_secret"));
			return true;
		}
		return false;
	}

	private void setOauthUserData(Intent intent) {
		intent.putExtra(AuthData.REQUEST_TOKEN_URI.name(), REQUEST_TOKEN_URI);
		intent.putExtra(AuthData.ACCESS_TOKEN_URI.name(), ACCESS_TOKEN_URI);
		intent.putExtra(AuthData.AUTH_WEB_URI.name(), AUTH_WEB_URI);

		intent.putExtra(AuthData.CONSUMER_KEY.name(), consumerKey);
		intent.putExtra(AuthData.CONSUMER_SECRET.name(), consumerSecret);

		intent.putExtra(AuthData.OAUTH_PROVIDER_KEY.name(), OAUTH_DB_KEY);

		intent.putExtra(AuthData.OAUTH_1a.name(), "true");

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

	private class LookupTravelInfo extends AsyncTask<Void, Integer, TripInfo> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(TripitTravelInfoActivity.this, "Getting TripIt travel info...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					LookupTravelInfo.this.cancel(true);
				}
			});
		}

		@Override
		protected TripInfo doInBackground(Void... params) {

			TripInfo tripInfo = null;

			String requestUri = "https://api.tripit.com/v1/list/trip";
			try {

				HttpRequestBase getTripInfo = new HttpGet(requestUri);

				OAuthCredential cred = new OAuthCredential(oauthConsumer.getConsumerKey(), oauthConsumer.getConsumerSecret(), oauthConsumer.getToken(),
						oauthConsumer.getTokenSecret());
				cred.authorize(getTripInfo);

				InputSource response = (InputSource) CustomHttpClient.executeBaseRequest(App.getHttpClient(), getTripInfo, ReturnType.InputSource);

				if (null != response) {
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();
					XMLReader xr = parser.getXMLReader();

					TripitTravelInfoXmlHandler handler = new TripitTravelInfoXmlHandler();
					xr.setContentHandler(handler);
					xr.parse(response);

					tripInfo = handler.getTripInfo();
				}

			} catch (ClientProtocolException e) {
				Log.e(TAG, "Error executing HTTP request! " + e.getMessage());
			} catch (ParserConfigurationException e) {
				Log.e(TAG, "Error instantiating SAX Parser! " + e.getMessage());
			} catch (SAXException e) {
				Log.e(TAG, "Error instantiating SAX Parser! " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "Error getting TripInfo or parsing XML document! " + e.getMessage());
			} catch (Exception e) {
				Log.e(TAG, "Error authorizing request with OAUTH! " + e.getMessage());
			}

			return tripInfo;
		}

		@Override
		protected void onPostExecute(TripInfo result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null != result && !result.getTrips().isEmpty()) {
				showTripInfoListView(result);
			} else {
				printToUser("Sorry, no trips found for your account!");
			}
		}

	}

	private void showTripInfoListView(final TripInfo result) {

		OnItemClickListener onListViewClickListener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Intent intent = new Intent(TripitTravelInfoActivity.this, ShowEventsActivity.class);
				Trip trip = result.getTrips().get(position);
				Location location = new Location(LocationManager.GPS_PROVIDER);
				location.setLatitude(trip.getLatitude());
				location.setLongitude(trip.getLongitude());
				intent.putExtra(getString(R.string.EXTRA_LOCATION), location);

				intent.putExtra(getString(R.string.EXTRA_START_TIME), App.sdfEventMoreInfo.format(trip.getStartDate().getTime()));
				intent.putExtra(getString(R.string.EXTRA_END_TIME), App.sdfEventMoreInfo.format(trip.getEndDate().getTime()));

				// start ShowEventsActivity for selected location
				startActivity(intent);

			}
		};

		if (null == mListView) {
			mListView = new ListView(this);
			mListView.setCacheColorHint(0);
			ColorDrawable listDivider = new ColorDrawable(this.getResources().getColor(R.color.list_divider));
			mListView.setDivider(listDivider);
			mListView.setDividerHeight(3);
		} else {
			mListView.invalidateViews();
		}

		mListView.setAdapter(new ArrayAdapter<Trip>(this, R.layout.simple_list_item, result.getTrips()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row;

				if (null == convertView) {
					row = getLayoutInflater().inflate(R.layout.simple_list_item, null);
				} else {
					row = convertView;
				}
				Trip trip = getItem(position);
				TextView tv = (TextView) row.findViewById(R.id.simple_text);
				tv.setText(trip.toString() + ", " + trip.getCity() + ", " + App.sdfEventListItem.format(trip.getStartDate().getTime()));

				return row;
			}
		});

		mListView.setOnItemClickListener(onListViewClickListener);

		LinearLayout contentLayout = (LinearLayout) findViewById(R.id.content_linear);
		contentLayout.removeAllViews();
		contentLayout.addView(mListView);

	}

	private void lookupTravelInfo() {

		LookupTravelInfo lookupTravelInfo = new LookupTravelInfo();
		lookupTravelInfo.execute(new Void[] { null });

	};

	private void printToUser(final String contentHtml) {

		// prepare content layout and add view
		LinearLayout contentLinear = (LinearLayout) findViewById(R.id.content_linear);
		if (contentLinear.getChildCount() > 0) {
			contentLinear.removeAllViews();
		}
		// initialize TextView
		TextView text = App.getStandardTextView(this, getAssets(), null);
		text.setText(contentHtml);

		// fill content
		contentLinear.addView(text);
	}

}
