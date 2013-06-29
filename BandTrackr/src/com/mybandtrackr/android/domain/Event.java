package com.mybandtrackr.android.domain;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.ticketleap.TicketLeapClient.TicketleapEvent;

public class Event {

	private static final String TAG = Event.class.getSimpleName();
	private Long id;
	private String name;
	private String url;
	private String description;
	private Calendar datetime;
	private String ticketUrl;
	private List<Artist> artists;
	private Venue venue;
	private TicketStatus ticketStatus;
	private Calendar onSaleDatetime;

	private boolean isPlaceholder = false;

	public Event() {
		artists = new ArrayList<Artist>();
	}

	public Event(TicketleapEvent eventDetail) {
		this.name = eventDetail.name;
		this.url = eventDetail.url;
		if (null != eventDetail.description && !"".equals(eventDetail.description)) {
			this.description = eventDetail.description;
		}
		try {
			this.datetime = Calendar.getInstance();
			this.datetime.setTime(App.sdfTicketLeapDate.parse(eventDetail.earliestStartLocal));
		} catch (ParseException e) {
			Log.e(TAG, "Error with ticketleap earliset_start_local value format");
		}
		this.ticketUrl = eventDetail.url;
		this.venue = new Venue(eventDetail);

		// always show ticket url for tiketleap
		this.ticketStatus = TicketStatus.available;

		// ticketleap has no artist info
		this.artists = new ArrayList<Artist>(0);
		
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Event) {
			Event obj = (Event) o;

			if (null != this.getId() && null != obj.getId()) {
				return this.getId().equals(obj.getId());
			} else {
				return this.getVenue().getName().equals(obj.getVenue().getName());
			}
		}
		return super.equals(o);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (null != artists && !artists.isEmpty()) {
			sb.append(artists.get(0).getName());
		}

		sb.append(", " + App.sdfEventListItem.format(datetime.getTime()) + "\n");

		if (null != venue && null != venue.getName()) {
			sb.append(venue.getName());
		}

		return sb.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Calendar getDatetime() {
		return datetime;
	}

	public void setDatetime(Calendar datetime) {
		this.datetime = datetime;
	}

	public String getTicketUrl() {
		return ticketUrl;
	}

	public void setTicketUrl(String ticketUrl) {
		this.ticketUrl = ticketUrl;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public TicketStatus getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(TicketStatus ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public Calendar getOnSaleDatetime() {
		return onSaleDatetime;
	}

	public void setOnSaleDatetime(Calendar onSaleDatetime) {
		this.onSaleDatetime = onSaleDatetime;
	}

	public enum TicketStatus {
		available, unavailable
	}

	public boolean isPlaceholder() {
		return isPlaceholder;
	}

	public void setPlaceholder(boolean isPlaceholder) {
		this.isPlaceholder = isPlaceholder;
	}

}
