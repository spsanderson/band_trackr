package com.mybandtrackr.android.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.HmacSha1MessageSigner;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.ShowEventsActivity;
import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

public class OAuthActivity extends Activity {

	private ProgressDialog pd;

	private static final String TAG = OAuthActivity.class.getSimpleName();

	private OAuthDataHelper oauthDataHelper;

	private OAuthProvider oauthProvider;
	private OAuthConsumer oauthConsumer;

	private final String REDIRECT_URL = "bandtrackr-authflow://oauth";

	private Map<AuthData, String> userData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		oauthDataHelper = new OAuthDataHelper(this);

		// get input data for Activity
		getExtras(getIntent(), savedInstanceState);

		String oauthProviderKey = userData.get(AuthData.OAUTH_PROVIDER_KEY);

		// if user data not provided on Activity call
		if (oauthProviderKey == null) {

			// OAuth process is already in progress for this provider, lookup shared preferences
			loadUserData();
		} else {
			// save OAuth provider key to shared preferences for access token process
			saveUserData();

		}

		// initialize OAuth helper
		oauthProvider = new CommonsHttpOAuthProvider(userData.get(AuthData.REQUEST_TOKEN_URI), userData.get(AuthData.ACCESS_TOKEN_URI),
				userData.get(AuthData.AUTH_WEB_URI), App.getHttpClient());

		if ("true".equals(userData.get(AuthData.OAUTH_1a))) {
			oauthProvider.setOAuth10a(true);
		}

		oauthConsumer = new CommonsHttpOAuthConsumer(userData.get(AuthData.CONSUMER_KEY), userData.get(AuthData.CONSUMER_SECRET));
		oauthConsumer.setMessageSigner(new HmacSha1MessageSigner());

		// extract the OAUTH token if it exists
		Uri uri = this.getIntent().getData();
		if (uri != null) {
			final AccessToken getAccessToken = new AccessToken();
			getAccessToken.execute(new Uri[] { uri });

		} else {

			final CheckOAuth checkOAuth = new CheckOAuth();
			checkOAuth.execute(new Void[] { null });
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

	private void loadUserData() {
		for (AuthData key : AuthData.values()) {
			userData.put(key, getPreferences(MODE_PRIVATE).getString(key.name(), null));
		}
	}

	private void saveUserData() {
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		for (AuthData key : AuthData.values()) {
			editor.putString(key.name(), userData.get(key));
		}

		// Commit the edits!
		editor.commit();
	}

	private void getExtras(Intent intent, Bundle savedInstance) {

		userData = new HashMap<AuthData, String>(0);

		if (null != savedInstance) {
			for (AuthData key : AuthData.values()) {
				String value = savedInstance.getString(key.name());
				if (null != value && !"".equals(value)) {
					userData.put(key, value);
				}
			}
		} else {
			Bundle extras = intent.getExtras();
			if (null != extras) {
				for (AuthData key : AuthData.values()) {
					String value = extras.getString(key.name());
					if (null != value && !"".equals(value)) {
						userData.put(key, value);
					}
				}
			}
		}
	}

	private void loadUserAuthorizationUrl(final String url) {

		// prepare content layout and add view
		LinearLayout contentLinear = (LinearLayout) findViewById(R.id.content_linear);
		if (contentLinear.getChildCount() > 0) {
			contentLinear.removeAllViews();
		}
		contentLinear.setPadding(0, 0, 0, 0);

		// initialize WebView
		final WebView webView = App.getStandardWebView(this, null);
		contentLinear.addView(webView);

		new Handler().post(new Runnable() {
			// load URL
			public void run() {
				webView.loadUrl(url);
			}
		});

	}

	private void finish(int status, String msg) {

		// show message to user
		if (null != msg && !"".equals(msg)) {
			Util.showToast(msg, this);
		}

		// close this activity
		finish();
	}

	private class CheckOAuth extends AsyncTask<Void, Integer, Map<Integer, String>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(OAuthActivity.this, "Checking OAuth!", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					CheckOAuth.this.cancel(true);
					finish(RESULT_FIRST_USER, "User canceled operation!");
				}
			});

		}

		@Override
		protected Map<Integer, String> doInBackground(Void... params) {
			// check in DB if user is already authorized
			Map<String, Map<String, String>> oauthProviders = oauthDataHelper.selectAll();

			Map<Integer, String> result = new HashMap<Integer, String>(1);

			Map<String, String> provider = oauthProviders.get(userData.get(AuthData.OAUTH_PROVIDER_KEY));

			if (null != provider && provider.get("is_authorized").equals("1")) {
				// user already authorized with TripIt
				oauthConsumer.setTokenWithSecret(provider.get("oauth_token"), provider.get("oauth_token_secret"));
				result.put(RESULT_CANCELED, "User already authorized :)");

			} else {
				// authorize user

				try {
					String authorizeURI = oauthProvider.retrieveRequestToken(oauthConsumer, REDIRECT_URL);
					result.put(RESULT_OK, authorizeURI);
					// persist tmp token
					if (null == provider) {
						oauthDataHelper.insert(userData.get(AuthData.OAUTH_PROVIDER_KEY), oauthConsumer.getToken(), oauthConsumer.getTokenSecret(), "0", null,
								null);
					} else {
						// prevent duplication
						oauthDataHelper.updateByProvider(userData.get(AuthData.OAUTH_PROVIDER_KEY), oauthConsumer.getToken(), oauthConsumer.getTokenSecret(),
								"0", null, null);
					}

				} catch (OAuthMessageSignerException e) {
					String msg = "Error OAuth get token! " + e.getMessage();
					Log.e(TAG, msg);
					result.put(RESULT_FIRST_USER, msg);
				} catch (OAuthNotAuthorizedException e) {
					String msg = "Error OAuth get token! " + e.getMessage();
					Log.e(TAG, msg);
					result.put(RESULT_FIRST_USER, msg);
				} catch (OAuthExpectationFailedException e) {
					String msg = "Error OAuth get token! " + e.getMessage();
					Log.e(TAG, msg);
					result.put(RESULT_FIRST_USER, msg);
				} catch (OAuthCommunicationException e) {
					String msg = "Error OAuth get token! " + e.getMessage();
					Log.e(TAG, msg);
					result.put(RESULT_FIRST_USER, msg);
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(Map<Integer, String> result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (!result.isEmpty()) {
				Entry<Integer, String> entry = result.entrySet().iterator().next();

				if (entry.getKey().equals(RESULT_OK)) {
					// request token retrieved
					// load user authorization URL
					loadUserAuthorizationUrl(entry.getValue());
				} else {
					// error or cancel due to user being already authorized
					finish(entry.getKey(), entry.getValue());
				}

			} else {
				// any other error (should not happen)!
				String errMsg = "Failed recieving OAuth Token. Sorry!";
				finish(RESULT_FIRST_USER, errMsg);

			}
		}

	}

	private class AccessToken extends AsyncTask<Uri, Integer, Map<Integer, String>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(OAuthActivity.this, "Finishing OAuth process...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					AccessToken.this.cancel(true);
					finish(RESULT_FIRST_USER, "User canceled operation!");
				}
			});
		}

		@Override
		protected Map<Integer, String> doInBackground(Uri... params) {

			Map<Integer, String> result = new HashMap<Integer, String>(1);

			// check db first
			final String providerKey = userData.get(AuthData.OAUTH_PROVIDER_KEY);
			Map<String, Map<String, String>> allProviders = oauthDataHelper.selectAll();

			Map<String, String> provider = allProviders.get(providerKey);
			if (null != provider) {

				if (provider.get("is_authorized").equals("1")) {
					// user already authorized
					result.put(RESULT_CANCELED, "User already authorized!");

				} else if (null != params && params.length > 0) {
					Uri uri = params[0];

					try {
						oauthConsumer.setTokenWithSecret(provider.get("oauth_token"), provider.get("oauth_token_secret"));

						oauthProvider.retrieveAccessToken(oauthConsumer, uri.getQueryParameter("oauth_token").trim());

						// persist authorized token
						String accessToken = oauthConsumer.getToken();
						String accessTokenSecret = oauthConsumer.getTokenSecret();

						if (null != accessToken && !"".equals(accessToken) && null != accessTokenSecret && !"".equals(accessTokenSecret)) {
							oauthDataHelper.updateByProvider(providerKey, accessToken, accessTokenSecret, "1", null, null);
							// TODO: assert data persisted needed?

							result.put(RESULT_OK, "User successfully authorized to " + providerKey);
						} else {
							result.put(RESULT_FIRST_USER, "Error in retrieve access token for " + providerKey);
						}

					} catch (OAuthMessageSignerException e) {
						String msg = "Error OAuth access token! " + e.getMessage();
						Log.e(TAG, msg);
						result.put(RESULT_FIRST_USER, msg);
					} catch (OAuthNotAuthorizedException e) {
						String msg = "Error OAuth access token! " + e.getMessage();
						Log.e(TAG, msg);
						result.put(RESULT_FIRST_USER, msg);
					} catch (OAuthExpectationFailedException e) {
						String msg = "Error OAuth access token! " + e.getMessage();
						Log.e(TAG, msg);
						result.put(RESULT_FIRST_USER, msg);
					} catch (OAuthCommunicationException e) {
						String msg = "Error OAuth access token! " + e.getMessage();
						Log.e(TAG, msg);
						result.put(RESULT_FIRST_USER, msg);
					}

				}

			} else {
				result.put(RESULT_FIRST_USER, "Error, no oauth info in DB for " + providerKey + "!");
			}

			// clear shared preferences tmp data
			SharedPreferences settings = getPreferences(MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();

			for (AuthData key : AuthData.values()) {
				editor.remove(key.name());
			}

			// Commit the edits!
			editor.commit();

			return result;
		}

		@Override
		protected void onPostExecute(Map<Integer, String> result) {
			super.onPostExecute(result);

			pd.dismiss();

			Entry<Integer, String> entry = result.entrySet().iterator().next();
			finish(entry.getKey(), entry.getValue());

			// if (entry.getKey().equals(RESULT_FIRST_USER)) {
			// // if error then remove any entries for this provider from DB
			// oauthDataHelper.deleteByProvider(userData.get(AuthData.OAUTH_PROVIDER_KEY));
			// // this error won't be handled in the calling activity since
			// // WebView activity is set in between and "setStatus" won't work
			// // here
			// printToUser(entry.getValue());
			//
			// } else {
			// // finish OAuth activity regularly
			// finish(entry.getKey(), entry.getValue());
			// }

		}

	}

	// private void printToUser(final String contentHtml) {
	//
	// // prepare content layout and add view
	// LinearLayout contentLinear = (LinearLayout) findViewById(R.id.content_linear);
	// if (contentLinear.getChildCount() > 0) {
	// contentLinear.removeAllViews();
	// }
	// // initialize TextView
	// TextView text = App.getStandardTextView(this, getAssets(), null);
	// text.setText(contentHtml);
	//
	// // fill content
	// contentLinear.addView(text);
	// }

}
