package com.mybandtrackr.android.ticketleap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mybandtrackr.android.App;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.domain.Events;

public class TicketLeapClient {

	public static final String TAG = TicketLeapClient.class.getSimpleName();

	private Activity context;
	private final Location location;
	private Address address;

	public TicketLeapClient(Activity context, Location location, Address address) {

		this.context = context;
		this.location = location;
		this.address = address;

	}

	/**
	 * To be called in parallel thread to main UI thread!
	 * 
	 * @param endDate
	 * @param startDate
	 * @return events
	 * 
	 * @throws IllegalArgumentException
	 *             in case Geocoder returned no address for location
	 * 
	 */
	public Events getEvents(Calendar startDate, Calendar endDate) {

		Events events = null;

		if (null == address) {
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			try {
				List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (null != addresses && !addresses.isEmpty()) {
					address = addresses.get(0);
				}

			} catch (IOException e) {
				Log.w(TAG, "Location cannot be reverse geocoded! " + e.getMessage());
			}
		}

		if (null != address) {
			events = new Events();
			// first search with city info if available
			SearchResponse response = executeSearch(startDate, endDate, false);

			if (null == response || response.totalCount <= 0) {
				// second search, country code onnly
				response = executeSearch(startDate, endDate, true);
			}

			// refill result into Events deomain class
			if (null != response && response.totalCount > 0) {
				for (TicketleapEvent eventDetail : response.events) {
					events.getEvents().add(new Event(eventDetail));
				}
			}
		}

		return events;
	}

	private SearchResponse executeSearch(Calendar startDate, Calendar endDate, boolean countryCodeOnly) {

		SearchResponse response = null;

		if (null != address.getCountryCode()) {

			HttpGet getRequest = null;
			URI uri = null;

			try {

				// build request URI
				String uriLocation = "/events/by/location/" + new Locale("en", address.getCountryCode()).getISO3Country();
				if (!countryCodeOnly && null != address.getAdminArea() && null != address.getLocality()) {
					uriLocation += "/" + address.getAdminArea() + "/" + address.getLocality();
				}
				uri = new URI("http", "public-api.ticketleap.com", uriLocation, "key=5902975139023074"
						+ (null != startDate ? "&dates_after=" + App.sdfDateStandard.format(startDate.getTime()) : "")
						+ (null != endDate ? "&dates_before=" + App.sdfDateStandard.format(endDate.getTime()) : ""), null);
				getRequest = new HttpGet(uri);

				Log.d(TAG, "TicketLeap GET request: " + uri.toASCIIString());

				// execute request
				HttpResponse getResponse = new DefaultHttpClient().execute(getRequest);
				final int statusCode = getResponse.getStatusLine().getStatusCode();

				// check for status code
				if (statusCode != HttpStatus.SC_OK) {
					Log.w(getClass().getSimpleName(), "Error " + statusCode + " for URL " + uri);
					return null;
				}

				// parse response
				HttpEntity getResponseEntity = getResponse.getEntity();
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(getResponseEntity.getContent());
				response = gson.fromJson(reader, SearchResponse.class);

			} catch (IOException e) {
				Log.w(getClass().getSimpleName(), "Error for URL " + uri, e);
			} catch (URISyntaxException e) {
				Log.e(TAG, "Invalid TicketLeap request URI! " + e.getMessage());
			} finally {
				if (null != getRequest && !getRequest.isAborted()) {
					getRequest.abort();
				}
			}

		}

		return response;
	}

	public static class SearchResponse {

		public String query;

		@SerializedName("note")
		public String note;

		@SerializedName("total_count")
		public int totalCount;

		@SerializedName("page_num")
		public int pageNum;

		public List<TicketleapEvent> events;

		@SerializedName("page_size")
		public int pageSize;

	}

	public static class TicketleapEvent {

		@SerializedName("venue_city")
		public String venueCity;

		@SerializedName("venue_region_name")
		public String venueRegionName;

		@SerializedName("venue_street")
		public String venueStreet;

		@SerializedName("venue_name")
		public String venueName;

		@SerializedName("organization_name")
		public String organizationName;

		@SerializedName("venue_postal_code")
		public String venuePostalCode;

		@SerializedName("venue_country_code")
		public String venueCountryCode;

		@SerializedName("description")
		public String description;

		@SerializedName("image_url_small")
		public String imageUrlSmall;

		@SerializedName("performance_count")
		public int performanceCount;

		@SerializedName("venue_timezone")
		public String venueTimezone;

		@SerializedName("slug")
		public String slug;

		@SerializedName("earliest_start_local")
		public String earliestStartLocal;

		@SerializedName("earliest_start_utc")
		public String earliestStartUtc;

		@SerializedName("name")
		public String name;

		@SerializedName("url")
		public String url;

		public List<TicketleapPerformance> performances;

	}

	public static class TicketleapPerformance {
		public String url;

		@SerializedName("start_utc")
		public String startUtc;

		public String slug;

		@SerializedName("start_local")
		public String startLocal;

	}

}
