package com.mybandtrackr.android.share;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.persistence.OAuthDataHelper;
import com.mybandtrackr.android.protocol.CustomHttpClient;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class VideoShareActivity extends Activity {

	private static final String TAG = VideoShareActivity.class.getSimpleName();

	// public static final String RESUMABLE_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	public static final String YOUTUBE_UPLOAD_URL = "http://uploads.gdata.youtube.com/feeds/api/users/default/uploads";

	public static final String YOUTUBE_DEV_KEY = "AI39si4HRSLKkLF4pRRDSh3JkQq7eWTAs7qadtY7zEJy41jti7cqelOOjNS0fxXU9uUBsFeUzhrWV8ZQHkObIrzYu-Qs1dPuGw";

	private OAuthDataHelper oauthDataHelper;
	private static final String OAUTH_DB_KEY = "youtube";

	private ProgressDialog pd;

	private String accessToken;

	private boolean isActivityDone = false;

	protected String videoTitle;

	protected String videoDescription;

	protected String videoCategory;

	protected String videoKeywords;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// initialize SQLite OAuth storage
		oauthDataHelper = new OAuthDataHelper(this);

		initUserAuth(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		if (isActivityDone) {
			finish();
		}

	}

	/**
	 * Top left Logo click event
	 */
	public void onLogoClick(View view) {
		// do nothing
	}

	private void initUserAuth(boolean invalidateToken) {

		if (isUserAuthorized()) {
			uploadAndShare();
		} else {

			String accountType = "com.google";
			String authTokenType = "youtube";

			AccountManager.get(this).getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null, new AccountManagerCallback<Bundle>() {

				public void run(AccountManagerFuture<Bundle> future) {
					try {
						Bundle bundle = future.getResult();
						accessToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

						if (null != accessToken) {
							uploadAndShare();
						} else {
							Log.i(TAG, "Problem in getting Auth Token...");
						}
					} catch (Exception e) {
						Util.showToast(e.getMessage(), VideoShareActivity.this);
					}
				}
			}, null);
		}

	}

	private boolean isUserAuthorized() {
		Map<String, Map<String, String>> oauthProviders = oauthDataHelper.selectAll();

		if (oauthProviders.containsKey(OAUTH_DB_KEY) && oauthProviders.get(OAUTH_DB_KEY).get("is_authorized").equals("1")) {
			// user already authorized
			accessToken = oauthProviders.get(OAUTH_DB_KEY).get("oauth_token");
			return true;
		}
		return false;
	}

	private void uploadAndShare() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		final EditText editTitle = new EditText(this);
		editTitle.setHint("Title");
		final EditText editDescription = new EditText(this);
		editDescription.setHint("Description");
		final Spinner spinnerCategory = new Spinner(this);
		String[] array_spinner = new String[14];
		array_spinner[0] = "Autos & Vehicles";
		array_spinner[1] = "Comedy";
		array_spinner[2] = "Education";
		array_spinner[3] = "Entertainment";
		array_spinner[4] = "Film & Animation";
		array_spinner[5] = "Gaming";
		array_spinner[6] = "Howto & Style";
		array_spinner[7] = "News & Politics";
		array_spinner[8] = "Nonprofits & Activism";
		array_spinner[9] = "People & Blogs";
		array_spinner[10] = "Pets & Animals";
		array_spinner[11] = "Science & Technology";
		array_spinner[12] = "Sports";
		array_spinner[13] = "Travel & Events";
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCategory.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				videoCategory = parent.getItemAtPosition(pos).toString();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				videoCategory = null;
			}
		});
		spinnerCategory.setAdapter(adapter);
		final EditText editKeywords = new EditText(this);
		editKeywords.setHint("Keywords");

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(editTitle);
		ll.addView(editDescription);
		ll.addView(spinnerCategory);
		// TODO default keywords?
		ll.addView(editKeywords);

		alert.setView(ll);
		alert.setTitle("Add Video info..");
		alert.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				videoTitle = editTitle.getText().toString().trim();
				videoKeywords = editKeywords.getText().toString().trim();
				if (!"".equals(videoTitle) && !"".equals(videoKeywords)) {
					videoDescription = editDescription.getText().toString().trim();

					UploadYoutube upload = new UploadYoutube();
					upload.execute(new Intent[] { getIntent() });

				} else {
					Util.showToast("Title and Keywords cannot be empty!", VideoShareActivity.this);
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

	private class UploadYoutube extends AsyncTask<Intent, Integer, Map<Integer, String>> {

		private InputStream inputStream;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(VideoShareActivity.this, "Sharing video...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					UploadYoutube.this.cancel(true);
					closeInputStream(inputStream);
				}
			});
		}

		@Override
		protected Map<Integer, String> doInBackground(Intent... arg0) {

			final Map<Integer, String> result = new HashMap<Integer, String>();

			String exErr = "Unknown error!";

			try {

				Bundle extras = arg0[0].getExtras();
				String action = arg0[0].getAction();

				// if this is from the share menu
				if (Intent.ACTION_SEND.equals(action)) {
					if (extras.containsKey(Intent.EXTRA_STREAM)) {

						Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

						// TODO: ask user for meta data

						String entry = "<?xml version=\"1.0\"?>" + "<entry xmlns=\"http://www.w3.org/2005/Atom\""
								+ " xmlns:media=\"http://search.yahoo.com/mrss/\"" + " xmlns:yt=\"http://gdata.youtube.com/schemas/2007\">" + "<media:group>"
								+ "<media:title type=\"plain\">" + videoTitle + "</media:title>" + "<media:description type=\"plain\">" + videoDescription
								+ "</media:description>" + "<media:category" + " scheme=\"http://gdata.youtube.com/schemas/2007/categories.cat\">"
								+ videoCategory + "</media:category>" + "<media:keywords>" + videoKeywords + "</media:keywords>" + "</media:group>"
								+ "</entry>";

						String boundary = "f93dcbA3";
						String endLine = "\r\n";

						StringBuilder sb = new StringBuilder();
						sb.append("--");
						sb.append(boundary);
						sb.append(endLine);
						sb.append("Content-Type: application/atom+xml; charset=UTF-8");
						sb.append(endLine);
						sb.append(endLine);
						sb.append(entry);
						sb.append(endLine);
						sb.append("--");
						sb.append(boundary);
						sb.append(endLine);
						sb.append("Content-Type: video/3gpp");
						sb.append(endLine);
						sb.append("Content-Transfer-Encoding: binary");
						sb.append(endLine);
						sb.append(endLine);

						String bodyStart = sb.toString();

						sb = new StringBuilder();
						sb.append(endLine);
						sb.append("--");
						sb.append(boundary);
						sb.append("--");

						String bodyEnd = sb.toString();

						HttpURLConnection conn;
						try {
							inputStream = getContentResolver().openInputStream(uri);
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();

							int nRead;
							byte[] data = new byte[16384];

							while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
								buffer.write(data, 0, nRead);
							}

							buffer.flush();

							byte fileBytes[] = buffer.toByteArray();

							conn = (HttpURLConnection) new URL(YOUTUBE_UPLOAD_URL).openConnection();

							conn.setRequestMethod("POST");
							conn.setRequestProperty("Content-Type", "multipart/related; boundary=\"" + boundary + "\"");
							conn.setRequestProperty("Host", "uploads.gdata.youtube.com");
							conn.setRequestProperty("Authorization", "GoogleLogin auth=" + accessToken);
							conn.setRequestProperty("GData-Version", "2");
							conn.setRequestProperty("X-GData-Key", "key=" + YOUTUBE_DEV_KEY);
							conn.setRequestProperty("Slug", "video.3gp");
							conn.setRequestProperty("Content-Length", "" + (bodyStart.getBytes().length + fileBytes.length + bodyEnd.getBytes().length));
							conn.setRequestProperty("Connection", "close");

							conn.setDoOutput(true);
							conn.setDoInput(true);
							conn.setUseCaches(false);
							try {
								conn.connect();

								try {
									OutputStream os = new BufferedOutputStream(conn.getOutputStream());

									os.write(bodyStart.getBytes());
									os.write(fileBytes);
									os.write(bodyEnd.getBytes());
									os.flush();

									try {
										// TODO: find nicer way to extract yt video URL!
										String youtubeXml = CustomHttpClient.getString(new InputStreamReader(conn.getInputStream()));
										int start = youtubeXml.indexOf("<media:player url='");
										String urlPart = youtubeXml.substring(start);
										String youtubeUrl = (String) urlPart.subSequence(urlPart.indexOf("http"), urlPart.indexOf(";feature"));
										result.put(RESULT_OK, youtubeUrl);
									} catch (IOException e) {
										exErr = "Error uploading video! " + e.getMessage();
									}

								} catch (FileNotFoundException e) {
									exErr = "Error uploading video! " + e.getMessage();
								} catch (IOException e) {
									exErr = "Error uploading video! " + e.getMessage();
								}
							} catch (IOException e) {
								exErr = "Error uploading video! " + e.getMessage();
							}
						} catch (MalformedURLException e) {
							exErr = "Error uploading video! " + e.getMessage();
						} catch (IOException e) {
							exErr = "Error uploading video! " + e.getMessage();
						}

					}
				}
			} finally {
				if (result.isEmpty()) {
					Log.e(TAG, exErr);
					result.put(RESULT_FIRST_USER, exErr);
				}

				closeInputStream(inputStream);
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
					// share via janrain
					shareViaJanrain(entry.getValue());
				} else {

					Util.showToast(entry.getValue(), VideoShareActivity.this);
				}
			} else {
				Log.e(TAG, "Unexpected error..");
			}

		}

		private void closeInputStream(InputStream inputStream) {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// input stream already closed
				}
			}
		}

	}

	private void shareViaJanrain(String videoUri) {

		App.shareViaJanrain(this, null, null, videoUri);

		isActivityDone = true;
	}

}
