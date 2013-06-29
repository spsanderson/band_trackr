package com.mybandtrackr.android.bit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;

import com.mybandtrackr.android.R;

public class BitTicketUrlActivity extends Activity {

	private WebView webView;
	private String ticketUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.web_view);

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);

		String eventIdKey = getString(R.string.EXTRA_EVENT_TICKET_URL);

		if (null != savedInstanceState) {

			ticketUrl = savedInstanceState.getString(eventIdKey);

		} else {
			if (null != getIntent()) {
				Bundle extras = getIntent().getExtras();
				if (null != extras) {
					ticketUrl = extras.getString(eventIdKey);
				}
			}
		}

		if (null == ticketUrl) {
			ticketUrl = "No ticket Url for selected Event... Sorry!";
		}

		new Handler().post(new Runnable() {

			public void run() {
				webView.loadUrl(ticketUrl);
			}

		});
	}

}
