package com.mybandtrackr.android.foursquare;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class OAuthActivity extends Activity {

	public static final int REQUEST_AUTHENTICATE_USER = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FoursquareApi api = new FoursquareApi();

		setContentView(R.layout.main);

		loadUserAuthorizationUrl(api.getAjaxAuthenticationUrl());

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
		webView.setWebViewClient(new OAuthWebViewClient(this));
		contentLinear.addView(webView);

		new Handler().post(new Runnable() {
			// load URL
			public void run() {
				webView.loadUrl(url);
			}
		});

	}

	protected void authenticationError() {
		finish();
	}

	protected void authenticationSuccess(String accessToken) {
		storeAccessToken(accessToken);

		Intent data = new Intent();

		setResult(RESULT_OK, data);
		finish();
	}

	private void storeAccessToken(String accessToken) {
		OAuthDataHelper oAuthDataHelper = new OAuthDataHelper(getApplicationContext());
		oAuthDataHelper.deleteByProvider(FoursquareApi.OAUTH_PROVIDER);
		oAuthDataHelper.insert(FoursquareApi.OAUTH_PROVIDER, accessToken, "none", "1", null, null);
		oAuthDataHelper.close();
	}

}
