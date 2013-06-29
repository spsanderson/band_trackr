package com.mybandtrackr.android.protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

public class OAuth2Activity extends Activity {

	final String TAG = getClass().getName();

	private OAuthDataHelper prefs;

	private Map<AuthData, String> userData;

	private static final String REDIRECT_URL = "bandtrackr-authflow://oauth2";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Starting task to retrieve request token.");
		this.prefs = new OAuthDataHelper(this);

		// get input data for Activity
		getExtras(getIntent(), savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);

		final String CLIENT_ID = userData.get(AuthData.CONSUMER_KEY);
		final String CLIENT_SECRET = userData.get(AuthData.CONSUMER_SECRET);
		final String SCOPE = userData.get(AuthData.SCOPE);
		final String REDIRECT_URI = null != userData.get(AuthData.CALLBACK) ? userData.get(AuthData.CALLBACK) : REDIRECT_URL;
		final String OAUTH_PROVIDER_KEY = userData.get(AuthData.OAUTH_PROVIDER_KEY);

		String authorizationUrl = new GoogleAuthorizationRequestUrl(CLIENT_ID, REDIRECT_URI, SCOPE).build();
		/* WebViewClient must be set BEFORE calling loadUrl! */
		webview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap bitmap) {
				System.out.println("onPageStarted : " + url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				if (url.startsWith(REDIRECT_URI)) {
					try {

						if (url.indexOf("code=") != -1) {

							String code = extractCodeFromUrl(url);

							AccessTokenResponse accessTokenResponse = new GoogleAuthorizationCodeGrant(new NetHttpTransport(), new JacksonFactory(), CLIENT_ID,
									CLIENT_SECRET, code, REDIRECT_URI).execute();

							if (prefs.selectAll().containsKey(OAUTH_PROVIDER_KEY)) {
								prefs.updateByProvider(OAUTH_PROVIDER_KEY, accessTokenResponse.accessToken, accessTokenResponse.refreshToken, "1", null, null);
							} else {
								prefs.insert(OAUTH_PROVIDER_KEY, accessTokenResponse.accessToken, accessTokenResponse.refreshToken, "1", null, null);
							}
							view.setVisibility(View.INVISIBLE);
							String msg = "OAuth2.0 established with " + OAUTH_PROVIDER_KEY;
							Util.showToast(msg, OAuth2Activity.this);
							finish(RESULT_OK, msg);
						} else if (url.indexOf("error=") != -1) {
							view.setVisibility(View.INVISIBLE);
							prefs.deleteByProvider(OAUTH_PROVIDER_KEY);

							String msg = "OAuth2.0 failed! :( Credentials cleared for " + OAUTH_PROVIDER_KEY + " ...";
							Util.showToast(msg, OAuth2Activity.this);
							finish(RESULT_FIRST_USER, msg);
						}

					} catch (IOException e) {
						String msg = "Error in OAuth2 flow: AccessToken" + e.getMessage();
						Log.e(TAG, msg);
						finish(RESULT_FIRST_USER, msg);
					}

				}
				Log.d(TAG, "onPageFinished : " + url);

			}

			private String extractCodeFromUrl(String url) {
				return url.substring(REDIRECT_URI.length() + 7, url.length());
			}
		});

		webview.loadUrl(authorizationUrl);
	}

	private void finish(int status, String msg) {
		Intent intent = new Intent();
		if (null != msg && !"".equals(msg)) {
			intent.putExtra(getString(R.string.EXTRA_MSG), msg);
		}

		// set status
		setResult(status, intent);

		// close this activity
		finish();
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

}
