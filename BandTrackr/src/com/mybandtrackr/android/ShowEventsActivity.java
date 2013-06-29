package com.mybandtrackr.android;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.acra.ErrorReporter;
import org.apache.http.client.ClientProtocolException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mybandtrackr.android.bit.BitClient;
import com.mybandtrackr.android.bit.BitTicketUrlActivity;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.domain.Event.TicketStatus;
import com.mybandtrackr.android.domain.Events;
import com.mybandtrackr.android.domain.Venue;
import com.mybandtrackr.android.eventful.EventfulEventsXmlHandler;
import com.mybandtrackr.android.lastfm.LastFmViewEventActivity;
import com.mybandtrackr.android.location.LocationUpdateActivity;
import com.mybandtrackr.android.maps.GoogleMapsActivity;
import com.mybandtrackr.android.protocol.CustomHttpClient;
import com.mybandtrackr.android.protocol.CustomHttpClient.ReturnType;
import com.mybandtrackr.android.ticketleap.TicketLeapClient;

import de.umass.lastfm.Artist;
import de.umass.lastfm.User;

public class ShowEventsActivity extends AbstractActivity {

	private Calendar startDate;
	private Calendar endDate;

	private ProgressDialog pd;
	private ListView listView;

	private QuickAction quickAction;
	private int selectedRow;

	private Typeface droidSans;
	private Typeface droidSansBold;

	private Events events;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		TAG = ShowEventsActivity.class.getSimpleName();

		droidSans = Typeface.createFromAsset(getAssets(), "fonts/DroidSans.ttf");
		droidSansBold = Typeface.createFromAsset(getAssets(), "fonts/DroidSans-Bold.ttf");

		// MoreInfo action item
		ActionItem moreInfoAction = new ActionItem();
		moreInfoAction.setTitle("Info");
		moreInfoAction.setIcon(getResources().getDrawable(R.drawable.ic_list_info));

		// BuyTicket action item
		ActionItem buyTicketAction = new ActionItem();
		buyTicketAction.setTitle("Buy Ticket");
		buyTicketAction.setIcon(getResources().getDrawable(R.drawable.ic_list_ticket));

		// Maps action item
		ActionItem googleMapsAction = new ActionItem();
		googleMapsAction.setTitle("Location");
		googleMapsAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_location));

		// ShareJanrain action item
		ActionItem shareJanrainAction = new ActionItem();
		shareJanrainAction.setTitle("Share");
		shareJanrainAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_share));

		// CheckIn 4sq action item
		ActionItem checkin4sqAction = new ActionItem();
		checkin4sqAction.setTitle("Foursquare");
		checkin4sqAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_4sq));

		quickAction = new QuickAction(this);
		quickAction.addActionItem(moreInfoAction);
		quickAction.addActionItem(buyTicketAction);
		quickAction.addActionItem(googleMapsAction);
		quickAction.addActionItem(shareJanrainAction);
		quickAction.addActionItem(checkin4sqAction);

		// setup the action item click listener
		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

			public void onItemClick(int pos) {

				if (pos == 0) { // View info selected
					viewEvent(selectedRow);
				} else if (pos == 1) { // Buy ticket selected
					viewEventTicketUrl(selectedRow);
				} else if (pos == 2) { // Google Maps selected
					showInGoogleMaps(selectedRow);
				} else if (pos == 3) { // share
					shareEvent(selectedRow);
				} else if (pos == 4) { // checkin 4sq

					String locationName = null;
					if (null != events.getEvents().get(selectedRow).getVenue()) {
						locationName = events.getEvents().get(selectedRow).getVenue().getName();
					}

					onFoursquareClick(locationName);
				}

			}
		});

		Intent intent = getIntent();
		if (null != intent) {

			// in case caller activity set location
			bestLocation = intent.getParcelableExtra(getString(R.string.EXTRA_LOCATION));

			try {
				String startDate = intent.getStringExtra(getString(R.string.EXTRA_START_TIME));
				String endDate = intent.getStringExtra(getString(R.string.EXTRA_END_TIME));

				if (null != startDate) {
					this.startDate = Calendar.getInstance();
					this.startDate.setTime(App.sdfEventMoreInfo.parse(startDate));
				}
				if (null != endDate) {
					this.endDate = Calendar.getInstance();
					this.endDate.setTime(App.sdfEventMoreInfo.parse(endDate));
				}
			} catch (ParseException e) {
				Log.e(TAG, "Error parsing Date from Intent! " + e.getMessage());
			}

		}

		if (null != bestLocation) {
			onRefreshClick(false);
		} else {
			onRefreshClick(true);
		}
	}

	@Override
	protected void onRefreshClick(boolean askForLocationFix) {
		Log.d(TAG, "refresh");

		// check for last.fm settings
		String lastFmUser = getPreferences(MODE_PRIVATE).getString(App.KEY_LASTFM_NAME, null);
		if (null == lastFmUser) {
			// last.fm neither set nor disabled
			showLastFmAlertDialog(true);

		} else {
			if (null == bestLocation) {
				// get location fix for first time
				startLocationUpdateActivity();

			} else if (askForLocationFix) {

				// last location fix failed, ask user to retry
				final AlertDialog.Builder alert = new AlertDialog.Builder(ShowEventsActivity.this);
				alert.setMessage("Do you want to update your location? Please make sure your device's location services are turned on!");
				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// get location fix
						startLocationUpdateActivity();
					}
				});

				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();

						// show events for location
						new RetrieveEvents().execute(new Location[] { bestLocation });
					}
				});

				alert.show();

			} else {
				// show events for location
				new RetrieveEvents().execute(new Location[] { bestLocation });
			}

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(TAG, "onActivityResult");

		if (requestCode == RequestCodes.LocationHandler.ordinal() && null != intent) {
			if (resultCode == RESULT_OK) {

				Location location = intent.<Location> getParcelableExtra(getString(R.string.EXTRA_LOCATION));
				if (null != location) {
					bestLocation = location;
					geocodedAddress = intent.<Address> getParcelableExtra(getString(R.string.EXTRA_ADDRESS));
					onRefreshClick(false);
				}

			} else {
				Util.showToast("No location found. Please make sure your device's location services are turned on and try again.", this, Toast.LENGTH_LONG);
			}
		}
	}

	private void startLocationUpdateActivity() {
		Intent intent = new Intent(this, LocationUpdateActivity.class);
		intent.putExtra(App.KEY_GPS_ENABLED, getPreferences(MODE_PRIVATE).getBoolean(App.KEY_GPS_ENABLED, true));
		startActivityForResult(intent, RequestCodes.LocationHandler.ordinal());
	}

	/**
	 * QuickActionItem click event
	 * 
	 * @param eventListIdx
	 */
	public void viewEvent(int eventListIdx) {
		Log.v(TAG, "vieEvent");
		Intent intent = new Intent(ShowEventsActivity.this, LastFmViewEventActivity.class);
		App.event = events.getEvents().get(eventListIdx);
		intent.putExtra(getString(R.string.EXTRA_LOCATION), bestLocation);
		Log.v(TAG, "starting " + LastFmViewEventActivity.class + " activity...");
		startActivity(intent);
	}

	/**
	 * QuickActionItem click event
	 * 
	 * @param eventListIdx
	 */
	public void viewEventTicketUrl(int eventListIdx) {

		Event selected = events.getEvents().get(selectedRow);

		if (null != selected.getTicketStatus() && selected.getTicketStatus().equals(TicketStatus.available)) {
			Intent intent = new Intent(ShowEventsActivity.this, BitTicketUrlActivity.class);
			intent.putExtra(getString(R.string.EXTRA_EVENT_TICKET_URL), selected.getTicketUrl());
			startActivity(intent);
		} else {
			Util.showToast("No tickets available for this Event. Sorry!", this, Toast.LENGTH_LONG);
		}
	}

	/**
	 * QuickActionItem click event
	 * 
	 * @param eventListIdx
	 */
	public void showInGoogleMaps(int eventListIdx) {
		Event selected = events.getEvents().get(selectedRow);

		Intent intent = new Intent(this, GoogleMapsActivity.class);

		intent.putExtra(getString(R.string.EXTRA_LOCATION_CURRENT), bestLocation);

		if (null != selected.getVenue()) {
			Venue venue = selected.getVenue();
			if (null != venue.getLatitude() && null != venue.getLongitude()) {
				Location location = new Location(LocationManager.GPS_PROVIDER);
				location.setLatitude(venue.getLatitude());
				location.setLongitude(venue.getLongitude());
				intent.putExtra(getString(R.string.EXTRA_LOCATION), location);
			}

			StringBuffer addressSb = new StringBuffer();
			// if (null != venue.getName()) {
			// addressSb.append(venue.getName()).append(" ");
			// }
			if (null != venue.getCity()) {
				addressSb.append(venue.getCity()).append(" ");
			}
			if (null != venue.getCountry()) {
				addressSb.append(venue.getCountry()).append(" ");
			}
			if (addressSb.length() > 0) {
				intent.putExtra(getString(R.string.EXTRA_ADDRESS_STRING), addressSb.toString());
			}
		}

		startActivity(intent);
	}

	/**
	 * QuickActionItem click event
	 * 
	 * @param eventListIdx
	 */
	private void shareEvent(int mSelectedRow) {
		Event event = events.getEvents().get(mSelectedRow);
		String info = event.getName();
		if (null == info) {
			info = null != event.getArtists() && !event.getArtists().isEmpty() ? event.getArtists().get(0).getName() : null;
			if (null == info) {
				info = event.getVenue().getName();
			}
		}

		App.shareViaJanrain(this, "I'm going to " + info, "Will you join me?", event.getUrl());
	}

	private OnItemClickListener onListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectedRow = position; // set the selected row
			quickAction.show(view);
		}
	};

	private class RetrieveEvents extends AsyncTask<Location, Integer, Map<Integer, Events>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = App.startProgressDialog(ShowEventsActivity.this, "Getting events...", true, new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					RetrieveEvents.this.cancel(true);
					Util.showToast("Please click Refresh in the Options Menu to find Events in your area", ShowEventsActivity.this);
				}
			});
		}

		@Override
		protected Map<Integer, Events> doInBackground(Location... locations) {

			Map<Integer, Events> result = new HashMap<Integer, Events>();

			// remove location custom data
			ErrorReporter.getInstance().removeCustomData(App.ERR_CUST_DAT_LOCATION_KEY);

			Events events = new Events();

			Location location = locations[0];

			// log location custom data in case of crash
			ErrorReporter.getInstance().putCustomData(App.ERR_CUST_DAT_LOCATION_KEY, location.toString());

			// check for events
			if (!location.getProvider().equals(getString(R.string.LOC_PROVIDER_PASSIVE))) {

				Log.d(TAG, "location = [" + location.getLatitude() + ";" + location.getLongitude() + "]");
				String errorMsg = "No events found for location...";
				// get events for location
				switch (eventServiceId) {
				case 3:
					events = searchEventful(location, startDate, endDate);
					break;
				case 2:
					events = new BitClient(ShowEventsActivity.this, location).getEvents(startDate, endDate);
					break;

				case 1:
					events = new TicketLeapClient(ShowEventsActivity.this, location, geocodedAddress).getEvents(startDate, endDate);

					if (events == null) {
						errorMsg = "Sorry, the Geocoder could not resolve your location to an address to be able to search the TicketLeap service. Please choose another service from the settings!";
					}

					break;
				case 0:
				default:

					// default BandTrackr search behavior

					// 1) TicketLeap
					events = new TicketLeapClient(ShowEventsActivity.this, location, geocodedAddress).getEvents(startDate, endDate);
					if (null != events && !events.getEvents().isEmpty()) {
						// filter by location
						events.setEvents(filterEventsByRadius(events.getEvents()));
					}

					// 2) Bands in Town
					if (null == events || events.getEvents().isEmpty()) {
						events = new BitClient(ShowEventsActivity.this, location).getEvents(startDate, endDate);
						if (null != events) {
							// filter by location
							events.setEvents(filterEventsByRadius(events.getEvents()));
						}
					}

					// 3) Eventful
					if (null == events || events.getEvents().isEmpty()) {
						events = searchEventful(location, startDate, endDate);
						if (null != events) {
							// filter by location
							events.setEvents(filterEventsByRadius(events.getEvents()));
						}
					}
				}

				// no events found
				if (null == events || events.getEvents().isEmpty()) {
					events = new Events();
					events.setEvents(getEventsUserMsg(errorMsg, true));
					result.put(RESULT_FIRST_USER, events);

				} else {
					// lookup for last.fm favorites
					lookupForLastFmFavorites(events);
					result.put(RESULT_OK, events);
				}

			} else {
				// no valid location found or gps/network disabled
				events.setEvents(getEventsUserMsg("No location found. Please make sure your device's location services are turned on and try again.", true));
				result.put(RESULT_FIRST_USER, events);
			}

			return result;

		}

		private List<Event> filterEventsByRadius(List<Event> list) {
			List<Event> result = new ArrayList<Event>();

			int maxDistance = App.getMaxDistance(ShowEventsActivity.this);
			for (Event event : list) {
				if (null != event.getVenue() && event.getVenue().isNearByLocation(geocodedAddress, bestLocation, maxDistance, App.DEF_MAX_LSED)) {
					result.add(event);
				}
			}
			return result;
		}

		private List<Event> getEventsUserMsg(String name, boolean isPlaceholder) {
			List<Event> eventList = new ArrayList<Event>();
			eventList.add(getEvent(name, isPlaceholder));
			return eventList;
		}

		private Event getEvent(String name, boolean isPlaceholder) {
			Event event = new Event();
			Venue venue = new Venue();
			venue.setName(name);
			event.setVenue(venue);
			if (isPlaceholder) {
				event.setPlaceholder(true);
			}
			return event;
		}

		private boolean isEnabledLastfm() {
			String lastfmName = getPreferences(MODE_PRIVATE).getString(App.KEY_LASTFM_NAME, null);
			return null != lastfmName && !App.CONST_DISABLED.equals(lastfmName) && !"".equals(lastfmName);
		}

		private void lookupForLastFmFavorites(Events events) {
			if (isEnabledLastfm()) {
				int topEventCount = 0;
				Events topEvents = new Events();
				Collection<Artist> topArtists = User.getTopArtists(getPreferences(MODE_PRIVATE).getString(App.KEY_LASTFM_NAME, ""), App.LAST_FM_API_KEY);

				boolean done = false;
				for (Event event : events.getEvents()) {
					boolean eventChecked = false;
					for (com.mybandtrackr.android.domain.Artist artist : event.getArtists()) {
						for (Artist topArtist : topArtists) {
							if (Util.isSimilar(artist.getName(), topArtist.getName(), 3)) {
								topEventCount++;
								topEvents.getEvents().add(event);
								if (topEventCount == 10) {
									// max top events reached
									done = true;
								}
								eventChecked = true;
								break;
							}
							if (eventChecked || done) {
								break;
							}
						}
						if (eventChecked || done) {
							break;
						}
					}
					if (done) {
						break;
					}
				}

				if (done || !topEvents.getEvents().isEmpty()) {
					// enrich events list with
					events.getEvents().removeAll(topEvents.getEvents());
					events.getEvents().add(0, getEvent("last.fm top artists", true));
					events.getEvents().addAll(1, topEvents.getEvents());
					events.getEvents().add(topEvents.getEvents().size() + 1, getEvent("all the rest", true));
				}
			}
		}

		@Override
		protected void onPostExecute(Map<Integer, Events> result) {
			super.onPostExecute(result);

			pd.dismiss();

			if (null == listView) {
				listView = new ListView(ShowEventsActivity.this);
				listView.setCacheColorHint(0);
				ColorDrawable listDivider = new ColorDrawable(ShowEventsActivity.this.getResources().getColor(R.color.list_divider));
				listView.setDivider(listDivider);
				listView.setDividerHeight(3);
			} else {
				listView.invalidateViews();
			}

			Entry<Integer, Events> entry = result.entrySet().iterator().next();
			events = entry.getValue();

			listView.setAdapter(new EventListItemAdapter(ShowEventsActivity.this, R.layout.event_list_item, events.getEvents()));

			LinearLayout contentLayout = (LinearLayout) ShowEventsActivity.this.findViewById(R.id.content_linear);
			contentLayout.removeAllViews();

			if (entry.getKey() == RESULT_OK) {

				listView.setOnItemClickListener(onListViewClickListener);
			}

			contentLayout.addView(listView);
		}

	}

	public class EventListItemAdapter extends ArrayAdapter<Event> {

		private List<Event> items;

		public EventListItemAdapter(Context context, int textViewResourceId, List<Event> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public boolean isEnabled(int position) {
			super.isEnabled(position);
			return !items.get(position).isPlaceholder();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.event_list_item, null);
			}
			Event event = items.get(position);
			if (event != null) {

				TextView upperText = (TextView) v.findViewById(R.id.event_text_upper);

				if (upperText != null) {
					upperText.setTypeface(droidSansBold);

					if (event.isPlaceholder()) {
						upperText.setVisibility(View.GONE);

					} else {
						upperText.setVisibility(View.VISIBLE);

						if (null != event.getName() && !"".equals(event.getName())) {
							upperText.setText(event.getName());
						} else if (null != event.getArtists() && event.getArtists().size() > 0) {
							upperText.setText(event.getArtists().get(0).getName());
						} else {
							upperText.setText("Unknown event");
						}
					}
				}

				TextView lowerText = (TextView) v.findViewById(R.id.event_text_lower);

				if (lowerText != null) {
					lowerText.setTypeface(droidSans);

					if (event.isPlaceholder()) {
						lowerText.setTextColor(getResources().getColor(R.color.event_info_link));
					} else {
						lowerText.setTextColor(getResources().getColor(R.color.event_info));
					}
					lowerText.setText((null != event.getVenue() ? event.getVenue().getName() : "Unknown venue")
							+ (null != event.getDatetime() ? ", " + App.sdfEventListItem.format(event.getDatetime().getTime()) : ""));
				}
			}
			return v;
		}
	}

	public Events searchEventful(Location location, Calendar startDate, Calendar endDate) {
		Events events = null;

		int maxDistKm = App.getMaxDistance(this);

		// build request URI
		String uri = "http://api.eventful.com/rest/events/search?app_key="
				+ App.EVENTFUL_API_KEY
				+ "&category=music&page_size=30&location="
				+ location.getLatitude()
				+ ","
				+ location.getLongitude()
				+ (null != startDate && null != endDate ? "&date=" + App.sdfDateTight.format(startDate.getTime()) + "00-"
						+ App.sdfDateTight.format(endDate.getTime()) + "00" : "") + "&within=" + maxDistKm + "&units=km";

		Log.d(TAG, "Eventful GET request: " + uri);

		try {
			InputSource is = (InputSource) CustomHttpClient.executeGet(App.getHttpClient(), uri, ReturnType.InputSource);

			if (null != is) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				XMLReader xr = parser.getXMLReader();

				EventfulEventsXmlHandler handler = new EventfulEventsXmlHandler();
				xr.setContentHandler(handler);
				xr.parse(is);

				events = handler.getEvents();
			}

		} catch (ClientProtocolException e) {
			Log.e(TAG, "Exception execuging HTTP GET to Eventful! " + e.getMessage());
		} catch (URISyntaxException e) {
			Log.e(TAG, "Exception execuging HTTP GET to Eventful! " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "Exception execuging HTTP GET to Eventful! " + e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Exception parsing Eventful XML response! " + e.getMessage());
		} catch (SAXException e) {
			Log.e(TAG, "Exception parsing Eventful XML response! " + e.getMessage());
		}

		return events;
	}

	private enum RequestCodes {
		LocationHandler
	}

}
