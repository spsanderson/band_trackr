package com.mybandtrackr.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.TextView;

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.types.JREmailObject;
import com.janrain.android.engage.types.JRSmsObject;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.lastfm.DatabaseCache;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

import de.umass.lastfm.Caller;

@ReportsCrashes(formKey = "dElOOUNBNUdBMUZ5bUxldi10Y0lKVXc6MQ")
public class App extends Application {

	private static HttpClient httpClient;

	public final static SimpleDateFormat sdfEventListItem = new SimpleDateFormat("dd/M/yyyy");
	public final static SimpleDateFormat sdfEventMoreInfo = new SimpleDateFormat("E dd MMM yyyy");
	public final static SimpleDateFormat sdfTicketLeapDate = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

	public final static SimpleDateFormat sdfDateStandard = new SimpleDateFormat("yyyy-MM-dd");
	public final static SimpleDateFormat sdfDateTight = new SimpleDateFormat("yyyyMMdd");

	public final static String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String ERR_CUST_DAT_LOCATION_KEY = "location";
	public static final String ERR_CUST_DAT_LASTFM_KEY = "lastfm.user";
	public static final String ERR_CUST_DAT_EVENT_SERVICE = "event.service";

	public static final String JR_APP_ID = "ehkpeghcaghmenfcjiei"; // <-- Janrain Engage app ID
	public static final String JR_TOKEN_URL = ""; // <- your (optional) token URL

	public static final String LAST_FM_API_KEY = "eba1f94091538313f3d5dc630fe98910";
	public static final String EVENTFUL_API_KEY = "wprzNwTjKFCS8KcF";

	public static final String FLICKR_API_KEY = "858693da85219e4f09575eaebfad3652";
	public static final String FLICKR_API_SECRET = "b61ae383a301d5c6";
	public static final String FLICKR_USER_ID = "68613437@N06";

	public static final String JANRAIN_PROVIDER_KEY = "janrain";

	public static final String KEY_GPS_ENABLED = "gps.enabled";
	public static final String KEY_LASTFM_NAME = "lastfm.name";

	public static final String KEY_EVENT_SERVICE = "event.service";
	public static String DEF_EVENT_SERVICE;
	public static List<String> EVENT_SERVICES;

	public static final String KEY_MAX_DISTANCE = "max.distance";
	public static int DEF_MAX_DIST_KM;
	public static List<Integer> DISTANCES;

	public static final int DEF_MAX_LSED = 3;

	public static final String CONST_DISABLED = "DISABLED";

	public static Event event;

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();

		// Configure the last.fm client cache
		Caller.getInstance().setCache(new DatabaseCache(this));

		// initialize HttpClient
		initHttpClient();

		// set distances
		DISTANCES = new ArrayList<Integer>();
		DISTANCES.add(10);
		DISTANCES.add(25);
		DISTANCES.add(50);
		DISTANCES.add(100);

		DEF_MAX_DIST_KM = DISTANCES.get(0);

		// set event services
		EVENT_SERVICES = new ArrayList<String>();
		EVENT_SERVICES.add("BandTrackr Default");
		EVENT_SERVICES.add("TicketLeap");
		EVENT_SERVICES.add("Bands In Town");
		EVENT_SERVICES.add("Eventful");

		DEF_EVENT_SERVICE = EVENT_SERVICES.get(0);
	}

	private static void initHttpClient() {

		HttpParams params = new BasicHttpParams();
		params.setParameter("http.socket.timeout", new Integer(15000));
		params.setParameter("http.protocol.content-charset", "UTF-8");

		httpClient = new DefaultHttpClient(params);

	}

	public static ProgressDialog startProgressDialog(Context context, String message) {
		return startProgressDialog(context, message, false, null);
	}

	public static void showAboutAlertDialog(Context context, DialogInterface.OnClickListener onClickListener) {
		// show about msg
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setPositiveButton("OK", onClickListener);
		alert.setTitle("This app is in *Open Beta*");
		alert.setMessage("Whether you rate our app with 1 star or 5 we would greatly appreciate constructive feedback from you. Our goal at Band Trackr is to offer you the best app possible that will both meet your needs and keep it as simple as possible in order to constantly improve the experience and performance for you. So go ahead and let us grow together.");
		alert.setCancelable(false);
		alert.show();
	}

	public static ProgressDialog startProgressDialog(Context context, String message, boolean cancelable, OnCancelListener onCancelListener) {

		// start dialog
		if (null == onCancelListener) {
			return ProgressDialog.show(context, null, message, true, cancelable);
		} else {
			return ProgressDialog.show(context, null, message, true, cancelable, onCancelListener);
		}
	}

	public static TextView getStandardTextView(Context parent, AssetManager assets, Integer linkify) {
		Typeface droidSans = Typeface.createFromAsset(assets, "fonts/DroidSans.ttf");

		TextView text = new TextView(parent);
		text.setTypeface(droidSans);
		if (null != linkify) {
			text.setAutoLinkMask(linkify);
		}
		text.setPadding(10, 10, 10, 10);
		text.setTextColor(parent.getResources().getColor(R.color.event_info));
		text.setLinkTextColor(parent.getResources().getColor(R.color.event_info_link));

		return text;
	}

	public static WebView getStandardWebView(Context parent, String interfaceName) {
		WebView webView = new WebView(parent);
		webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		webView.requestFocus(View.FOCUS_DOWN);
		webView.setBackgroundColor(parent.getResources().getColor(R.color.transparent));
		webView.getSettings().setJavaScriptEnabled(true);
		if (null != interfaceName) {
			webView.addJavascriptInterface(parent, interfaceName);
		}
		return webView;
	}

	public static void shareViaJanrain(Activity context, String msg, String detail, String url) {
		String sharedViaInfo = "Shared via BandTrackr - http://www.mybandtrackr.com";
		JRActivityObject activity = new JRActivityObject((null != msg ? msg + " - " : "") + sharedViaInfo, url);
		JREmailObject email = new JREmailObject((null != msg ? msg : url), (null != detail ? detail + " " : "") + url + " " + sharedViaInfo);
		activity.setEmail(email);
		JRSmsObject sms = new JRSmsObject((null != msg ? msg : "") + (null != detail ? " " + detail : ""));
		activity.setSms(sms);

		JREngage jrEngage = initJrEngage(context, JANRAIN_PROVIDER_KEY);

		jrEngage.showSocialPublishingDialog(activity);

	}

	public static void signOutJanrain(Activity context) {
		JREngage jrEngage = initJrEngage(context, JANRAIN_PROVIDER_KEY);
		jrEngage.signoutUserForAllProviders();
	}

	private static JREngage initJrEngage(final Activity context, final String providerKey) {
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

				// save login info
				OAuthDataHelper dataHelper = new OAuthDataHelper(context);
				dataHelper.insert(providerKey, null, null, "1", null, auth_info.toJSON());
			}

			public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrAuthenticationDidNotComplete() {

				// remove login info
				OAuthDataHelper dataHelper = new OAuthDataHelper(context);
				dataHelper.deleteByProvider(providerKey);
			}

			public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}

			public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
				// TODO Auto-generated method stub

			}
		};

		return JREngage.initInstance(context, JR_APP_ID, JR_TOKEN_URL, delegate);
	}

	public static int getScreenOrientation(Display getOrient) {
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight()) {
			orientation = Configuration.ORIENTATION_SQUARE;
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else {
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		return orientation;
	}

	public static HttpClient getHttpClient() {
		if (null == httpClient) {
			initHttpClient();
		}
		return httpClient;
	}

	public static int getMaxDistance(Activity context) {
		return DISTANCES.get(getMaxDistanceId(context));
	}

	public static int getMaxDistanceId(Activity context) {
		return context.getPreferences(MODE_PRIVATE).getInt(App.KEY_MAX_DISTANCE, 0);
	}

	public static String getEventService(Activity context) {
		return EVENT_SERVICES.get(getEventServiceId(context));
	}

	public static int getEventServiceId(Activity context) {
		return context.getPreferences(MODE_PRIVATE).getInt(App.KEY_EVENT_SERVICE, 0);
	}

}
