package com.mybandtrackr.android;

import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

/**
 * BandTrackr main Activity - checks for user authentication - starts first Activity
 * 
 * @author senchi
 * 
 */
public class BandTrackrActivity extends AbstractActivity {

	private static final String TAG = BandTrackrActivity.class.getName();

	private OAuthDataHelper dataHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		dataHelper = new OAuthDataHelper(this);

		Map<String, Map<String, String>> allProviders = dataHelper.selectAll();

		if (allProviders.containsKey(App.JANRAIN_PROVIDER_KEY)) {
			/**
			 * User authenticated
			 */

			// start show events activity
			startShowEventsActivity();

		} else {
			// Show About Alert Dialog
			App.showAboutAlertDialog(this, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					login();
				}
			});
		}

	}

	@Override
	protected void onRefreshClick(boolean askForLocationUpdate) {
		// do nothing
	}

	private void login() {
		/**
		 * User Login
		 */

		// Initialize JREngage
		JREngageDelegate delegate = new JREngageDelegate() {

			public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrSocialDidNotCompletePublishing() {
				// TODO Auto-generated method stub

			}

			public void jrSocialDidCompletePublishing() {
				// TODO Auto-generated method stub

			}

			public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
				// TODO Auto-generated method stub

			}

			public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {

				dataHelper.insert(App.JANRAIN_PROVIDER_KEY, null, null, "1", null, auth_info.toJSON());

				// start show events activity
				startShowEventsActivity();
			}

			public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrAuthenticationDidNotComplete() {
				finish();

			}

			public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}
		};

		JREngage jrEngage = JREngage.initInstance(this, App.JR_APP_ID, App.JR_TOKEN_URL, delegate);

		jrEngage.showAuthenticationDialog();
	}

	private void startShowEventsActivity() {
		Log.d(TAG, "startShowEventsActivity");

		// close connection to DB
		dataHelper.close();

		// start activity
		Intent intent = new Intent(this, ShowEventsActivity.class);
		startActivity(intent);

		// close this activity as it's not needed anymore
		finish();
	}

}
