package com.mybandtrackr.android.share;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import com.mybandtrackr.android.App;
import com.mybandtrackr.android.AbstractActivity;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.persistence.OAuthDataHelper;

public class MediaSharingActivity extends AbstractActivity {

	private static final String TAG = MediaSharingActivity.class.getSimpleName();

	private Flickr flickr;
	private AuthInterface authInterface;
	private Auth auth;

	private OAuthDataHelper oauthDataHelper;
	private final String OAUTH_DB_KEY = "flickr";
	private String frob;

	private ProgressDialog pd;

	private boolean activityDone = false;

	protected String photoTitle;

	protected String photoDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// initialize SQLite OAuth storage
		oauthDataHelper = new OAuthDataHelper(this);

		try {
			REST restTransport = new REST();
			flickr = new Flickr(App.FLICKR_API_KEY, App.FLICKR_API_SECRET, restTransport);

		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Error initializing Flickr instance! " + e.getMessage());
		}
		Flickr.debugStream = false;
		authInterface = flickr.getAuthInterface();

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		if (activityDone) {
			finish();
		} else {

			Map<String, Map<String, String>> allProviders = oauthDataHelper.selectAll();
			if (null != allProviders && allProviders.containsKey(OAUTH_DB_KEY)) {

				Map<String, String> providerData = allProviders.get(OAUTH_DB_KEY);
				String isAuthorized = providerData.get("is_authorized");

				if (isAuthorized.equals("0")) {

					final AccessToken accessToken = new AccessToken();
					accessToken.execute(new Uri[] { null });

				} else if (isAuthorized.equals("1")) {
					try {
						auth = flickr.getAuthInterface().checkToken(providerData.get("oauth_token"));
					} catch (Exception e) {
						Log.e(TAG, e.toString());
					}
					// do share
					share();
				} else {
					checkOAuth();
				}
			} else {
				checkOAuth();
			}

		}

	}

	@Override
	protected void onRefreshClick(boolean askForLocationFix) {
		// do nothing
	}

	private void checkOAuth() {

		// get travel info
		final CheckOAuth checkOAuth = new CheckOAuth();
		checkOAuth.execute(new Object[] { null });
	}

	/**
	 * Top left Logo click event
	 */
	public void onLogoClick(View view) {
		// do nothing
	}

	private void share() {

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final EditText editTitle = new EditText(this);
		editTitle.setHint("Title");
		final EditText editDescription = new EditText(this);
		editDescription.setHint("Description");

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(editTitle);
		ll.addView(editDescription);

		alert.setView(ll);
		alert.setTitle("Add Photo info..");
		alert.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				photoTitle = editTitle.getText().toString().trim();
				if (!"".equals(photoTitle)) {
					photoDescription = editDescription.getText().toString().trim();

					UploadMedia share = new UploadMedia();
					share.execute(new Intent[] { getIntent() });

				} else {
					Util.showToast("Title cannot be empty!", MediaSharingActivity.this);
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

	private class CheckOAuth extends AsyncTask<Object, Integer, URL> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(MediaSharingActivity.this, "Checking OAuth status...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					CheckOAuth.this.cancel(true);
				}
			});
		}

		@Override
		protected URL doInBackground(Object... params) {

			URL authenticationUrl = null;

			Map<String, Map<String, String>> oauthProviders = oauthDataHelper.selectAll();

			if (oauthProviders.containsKey(OAUTH_DB_KEY) && oauthProviders.get(OAUTH_DB_KEY).get("is_authorized").equals("1")) {
				try {
					auth = flickr.getAuthInterface().checkToken(oauthProviders.get(OAUTH_DB_KEY).get("oauth_token"));
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}

			} else {
				try {
					try {
						frob = authInterface.getFrob();
					} catch (FlickrException e) {
						e.printStackTrace();
					}

					authenticationUrl = authInterface.buildAuthenticationUrl(Permission.WRITE, frob);

					// persist temporary frob
					oauthDataHelper.insert(OAUTH_DB_KEY, frob, flickr.getSharedSecret(), "0", null, null);

				} catch (IOException e) {
					Log.e(TAG, "Error in communication with Flickr! " + e.getMessage());
				} catch (SAXException e) {
					Log.e(TAG, "Error with Flickr response! " + e.getMessage());
				}

			}

			return authenticationUrl;

		}

		@Override
		protected void onPostExecute(URL result) {
			super.onPostExecute(result);

			if (null != result) {
				loadUserAuthorizationUrl(result.toString());
			} else {
				Util.showToast("No authorizationo URL, could be error on service side :( ", MediaSharingActivity.this);
			}

			pd.dismiss();
		}

	}

	private class AccessToken extends AsyncTask<Uri, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(MediaSharingActivity.this, "Finishing OAuth process...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					AccessToken.this.cancel(true);
				}
			});
		}

		@Override
		protected Boolean doInBackground(Uri... params) {

			boolean success = false;

			// check db first
			Map<String, Map<String, String>> allProviders = oauthDataHelper.selectAll();

			if (!success && null != params && params.length > 0 && !allProviders.isEmpty() && allProviders.containsKey(OAUTH_DB_KEY)) {

				Map<String, String> map = allProviders.get(OAUTH_DB_KEY);

				try {
					auth = flickr.getAuthInterface().getToken(map.get("oauth_token"));

					// persist access token
					RequestContext.getRequestContext().setAuth(auth);
					oauthDataHelper.updateByProvider(OAUTH_DB_KEY, auth.getToken(), map.get("oauth_token_secret"), "1", null, null);

				} catch (FlickrException e) {
					Log.e(TAG, "Authentication failed! " + e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "Error in communication with Flickr! " + e.getMessage());
				} catch (SAXException e) {
					Log.e(TAG, "Error with Flickr response! " + e.getMessage());
				}

				success = true;
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			pd.dismiss();

			if (success) {
				Util.showToast("User authenticated!", MediaSharingActivity.this);
				// do share
				share();
			} else {
				oauthDataHelper.deleteByProvider(OAUTH_DB_KEY);
				Util.showToast("Failed accessing OAuth Token. Sorry!", MediaSharingActivity.this);
			}

		}

	}

	private class UploadMedia extends AsyncTask<Intent, Integer, String> {

		private InputStream inputStream = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(MediaSharingActivity.this, "Uploading media to flickr.com ...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					UploadMedia.this.cancel(true);
					closeInputStream(inputStream);
				}

			});

		}

		@Override
		protected String doInBackground(Intent... params) {

			String photoId = null;

			Intent intent = params[0];

			Bundle extras = intent.getExtras();
			String action = intent.getAction();

			// if this is from the share menu
			if (Intent.ACTION_SEND.equals(action)) {
				if (extras.containsKey(Intent.EXTRA_STREAM)) {
					try {

						// Get resource path from intent callee
						Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

						// Query gallery for camera picture via
						// Android ContentResolver interface
						ContentResolver cr = getContentResolver();
						inputStream = cr.openInputStream(uri);
						try {

							Uploader uploader = flickr.getUploader();

							RequestContext.getRequestContext().setAuth(auth);

							UploadMetaData metaData = new UploadMetaData();
							metaData.setAsync(false);
							metaData.setTitle("Nice here, whoa? :)");
							metaData.setDescription("Some description lorem ipsum.. ");
							metaData.setPublicFlag(true);
							photoId = uploader.upload(inputStream, metaData);

						} finally {
							closeInputStream(inputStream);
						}

					} catch (Exception e) {
						Log.e(this.getClass().getName(), e.toString());
					} finally {
						closeInputStream(inputStream);
					}

				} else if (extras.containsKey(Intent.EXTRA_TEXT)) {
					return photoId;
				}
			}

			return photoId;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null != result) {
				shareViaJanrain(result);
			}

		}

		private void closeInputStream(InputStream inputStream) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// input stream already closed
				}
			}
		}

	}

	private void shareViaJanrain(String photoId) {

		String src = "http://www.flickr.com/photos/" + auth.getUser().getId() + "/" + photoId;

		App.shareViaJanrain(this, null, null, src);

	}

}
