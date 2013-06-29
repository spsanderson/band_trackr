package com.mybandtrackr.android.lastfm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mybandtrackr.android.AbstractActivity;
import com.mybandtrackr.android.App;
import com.mybandtrackr.android.R;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.domain.Event.TicketStatus;

import de.umass.lastfm.Artist;

public class LastFmViewEventActivity extends AbstractActivity {

	private ProgressDialog pd;
	private String artistName;

	private Event mEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		TAG = LastFmViewEventActivity.class.getSimpleName();
		Log.v(TAG, "onCreate");

		mEvent = App.event;

		if (null != mEvent) {
			onRefreshClick(false);
		} else {
			// unexpected behavior!
			Log.e(TAG, "Unexpected behavior! No Event set in " + LastFmViewEventActivity.class.getSimpleName());
			finish();
		}

	}

	@Override
	protected void onRefreshClick(boolean askForLocationUpdate) {
		Log.d(TAG, "refresh");

		new ShowEventInfo().execute(new Void[] { null });
	}

	private class ShowEventInfo extends AsyncTask<Void, Integer, View> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pd = App.startProgressDialog(LastFmViewEventActivity.this, "Getting event info...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					ShowEventInfo.this.cancel(true);
				}
			});
		}

		@Override
		protected View doInBackground(Void... eventIds) {
			StringBuilder eventStringBuilder = new StringBuilder();

			// TODO: refactor following code!

			/*
			 * Build Event Info content
			 */
			if (null != mEvent.getName()) {
				eventStringBuilder.append(mEvent.getName() + "<br/><br/>");
			}
			if (null != mEvent.getArtists() && !mEvent.getArtists().isEmpty()) {
				artistName = mEvent.getArtists().get(0).getName();
				eventStringBuilder.append(artistName).append("<br/>");
			}
			if (null != mEvent.getUrl()) {
				eventStringBuilder.append(mEvent.getUrl() + " <br/>");
			}
			if (null != mEvent.getVenue()) {
				if (null != mEvent.getVenue().getName()) {
					eventStringBuilder.append(mEvent.getVenue().getName()).append("<br/>");
				}
				if (null != mEvent.getVenue().getCity()) {
					eventStringBuilder.append(mEvent.getVenue().getCity() + (null != mEvent.getVenue().getRegion() ? ", " + mEvent.getVenue().getRegion() : "")
							+ "<br/>");
				}
			}
			if (null != mEvent.getDatetime()) {
				eventStringBuilder.append(App.sdfEventMoreInfo.format(mEvent.getDatetime().getTime()));
			}
			eventStringBuilder.append("<br/>").append("<br/>");
			if (null != mEvent.getTicketStatus() && mEvent.getTicketStatus().equals(TicketStatus.available)) {
				eventStringBuilder.append("Get tickets here: " + mEvent.getTicketUrl());
			} else {
				eventStringBuilder.append("No tickets available");
			}

			eventStringBuilder.append("<br/><br/>");
			eventStringBuilder.append("Check In to Foursquare:");

			if (null != mEvent.getDescription()) {
				eventStringBuilder.append("<br/>");
			}

			/*
			 * Build Artist Info content (last.fm WIKI)
			 */
			Artist info = null;

			StringBuilder lastfmStringBuilder = new StringBuilder().append("<br/>");

			boolean timeout = false;
			try {
				if (null != artistName && !"".equals(artistName)) {
					info = Artist.getInfo(artistName, App.LAST_FM_API_KEY);
					Log.v(TAG, "got artist info..");
				}
			} catch (Exception e) {
				timeout = true;
				Log.w(TAG, "Problem while getting info for aritst " + artistName + "! " + e.getMessage());
			}

			if (timeout) {
				lastfmStringBuilder.append("Operation timeout getting Last.fm WIKI info for " + artistName + ".");
			} else if (null == info || null == info.getWikiSummary() || "".equals(info.getWikiSummary())) {
				if (null != artistName) {
					lastfmStringBuilder.append("There is no Last.fm WIKI info for " + artistName + ".");
				} else {
					if (null != mEvent.getDescription() && !"".equals(mEvent.getDescription())) {
						lastfmStringBuilder.append("No artists listed for this event");
					}
				}
			} else {
				lastfmStringBuilder.append("WIKI info for " + artistName + ":<br/>");
				lastfmStringBuilder.append("<br/>");
				lastfmStringBuilder.append(info.getWikiSummary());
			}

			/*
			 * Build Views for content
			 */

			// Event content
			TextView eventInfoText = App.getStandardTextView(LastFmViewEventActivity.this, getAssets(), Linkify.ALL);
			eventInfoText.setText(Html.fromHtml(eventStringBuilder.toString()));

			// last.fm WIKI content
			TextView lastfmText = App.getStandardTextView(LastFmViewEventActivity.this, getAssets(), Linkify.ALL);
			lastfmText.setText(Html.fromHtml(lastfmStringBuilder.toString()));

			// last.fm powered by logo
			ImageView powerdByLastfm = new ImageView(LastFmViewEventActivity.this);
			powerdByLastfm.setImageResource(R.drawable.powered_by_lastfm);

			// Foursquare checkIn
			ImageView foursquareCheckIn = new ImageView(LastFmViewEventActivity.this);
			foursquareCheckIn.setImageResource(R.drawable.ic_menu_4sq);
			foursquareCheckIn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onFoursquareClick(null != mEvent.getVenue() ? mEvent.getVenue().getName() : null);
				}
			});

			// Put all views together into ScrollView
			ScrollView scrollView = new ScrollView(LastFmViewEventActivity.this);
			LinearLayout linearLayout = new LinearLayout(LastFmViewEventActivity.this);
			LayoutParams paramsFillParent = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			linearLayout.setLayoutParams(paramsFillParent);
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			linearLayout.addView(eventInfoText);
			linearLayout.addView(foursquareCheckIn);
			if (null != mEvent.getDescription() && !"".equals(mEvent.getDescription())) {
				TextView descriptionText = App.getStandardTextView(LastFmViewEventActivity.this, getAssets(), null);
				descriptionText.setText(mEvent.getDescription());
				linearLayout.addView(descriptionText);
			}
			linearLayout.addView(lastfmText);
			linearLayout.addView(powerdByLastfm);

			scrollView.setLayoutParams(paramsFillParent);
			scrollView.addView(linearLayout);

			return scrollView;
		}

		@Override
		protected void onPostExecute(View result) {
			super.onPostExecute(result);

			pd.dismiss();

			LinearLayout rl = (LinearLayout) findViewById(R.id.content_linear);

			rl.removeAllViews();
			rl.addView(result);

		}

	}

}
