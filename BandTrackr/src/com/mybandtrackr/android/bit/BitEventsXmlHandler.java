package com.mybandtrackr.android.bit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mybandtrackr.android.domain.Artist;
import com.mybandtrackr.android.domain.Artists;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.domain.Events;
import com.mybandtrackr.android.domain.Venue;
import com.mybandtrackr.android.domain.Event.TicketStatus;


public class BitEventsXmlHandler extends DefaultHandler {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private boolean finished = false;
	private boolean doParseChar = false;
	private StringBuilder builder;

	private Events events;
	private Event event;
	private Artists artists;
	private Artist artist;
	private Venue venue;

	private boolean inEvent = false;
	private boolean inArtists = false;
	private boolean inArtist = false;
	private boolean inVenue = false;

	private static final String EVENTS = "events";
	private static final String EVENT = "event";
	private static final String ID = "id";
	private static final String URL = "url";
	private static final String DATETIME = "datetime";
	private static final String TICKETURL = "ticket_url";
	private static final String ARTISTS = "artists";
	private static final String ARTIST = "artist";
	private static final String NAME = "name";
	private static final String MBID = "mbid";
	private static final String VENUE = "venue";
	private static final String CITY = "city";
	private static final String REGION = "region";
	private static final String COUNTRY = "country";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String TICKETSTATUS = "ticket_status";
	private static final String ONSALEDATETIME = "on_sale_datetime";

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		if (this.doParseChar && !this.finished) {
			if ((ch[start] != '\n') && (ch[start] != ' ')) {
				builder.append(ch, start, length);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);

		if (!this.finished) {
			if (localName.equalsIgnoreCase(EVENTS)) {
				finished = true;
			}
			if (localName.equalsIgnoreCase(EVENT)) {
				events.getEvents().add(event);
				inEvent = false;
			}
			if (localName.equalsIgnoreCase(ID)) {
				if (inEvent) {
					event.setId(getLong());
				}
				if (inVenue) {
					venue.setId(getLong());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(URL)) {
				if (inEvent) {
					event.setUrl(getString());
				}
				if (inArtist) {
					artist.setUrl(getString());
				}
				if (inVenue) {
					venue.setUrl(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(DATETIME)) {
				if (inEvent) {
					event.setDatetime(getCalendar());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(TICKETURL)) {
				if (inEvent) {
					event.setTicketUrl(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(ARTISTS)) {
				if (inEvent) {
					event.getArtists().addAll(artists.getArtists());
				}
				inArtists = false;
			}
			if (localName.equalsIgnoreCase(ARTIST)) {
				if (inArtists) {
					artists.getArtists().add(artist);
				}
				inArtist = false;
			}
			if (localName.equalsIgnoreCase(NAME)) {
				if (inArtist) {
					artist.setName(getString());
				}
				if (inVenue) {
					venue.setName(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(MBID)) {
				if (inArtist) {
					artist.setMbid(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(VENUE)) {
				if (inEvent) {
					event.setVenue(venue);
				}
				inVenue = false;
			}
			if (localName.equalsIgnoreCase(CITY)) {
				if (inVenue) {
					venue.setCity(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(REGION)) {
				if (inVenue) {
					venue.setRegion(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(COUNTRY)) {
				if (inVenue) {
					venue.setCountry(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LATITUDE)) {
				if (inVenue) {
					venue.setLatitude(getDouble());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LONGITUDE)) {
				if (inVenue) {
					venue.setLongitude(getDouble());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(TICKETSTATUS)) {
				if (inEvent) {
					event.setTicketStatus(getTicketstatus());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(ONSALEDATETIME)) {
				if (inEvent) {
					event.setOnSaleDatetime(getCalendar());
				}
				doParseChar = false;
			}

			if (builder != null) {
				builder.setLength(0);
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		builder = new StringBuilder();
		finished = false;
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);

		if (localName.equalsIgnoreCase(EVENTS)) {
			events = new Events();
		}
		if (localName.equalsIgnoreCase(EVENT)) {
			event = new Event();
			inEvent = true;
		}
		if (localName.equalsIgnoreCase(ID)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(URL)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(DATETIME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(TICKETURL)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(ARTISTS)) {
			artists = new Artists();
			inArtists = true;
		}
		if (localName.equalsIgnoreCase(ARTIST)) {
			artist = new Artist();
			inArtist = true;
		}
		if (localName.equalsIgnoreCase(NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(MBID)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(VENUE)) {
			venue = new Venue();
			inVenue = true;
		}
		if (localName.equalsIgnoreCase(CITY)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(REGION)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(COUNTRY)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LATITUDE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LONGITUDE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(TICKETSTATUS)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(ONSALEDATETIME)) {
			doParseChar = true;
		}

	}

	private String getString(String replace, String with) {
		if (builder.length() > 0) {
			return builder.toString().replace(replace, with);
			// TODO: fix this with builder
			// return builder.replace(builder.indexOf(replace),
			// builder.indexOf(replace)+with.length(), with).toString();
		}
		return null;
	}

	private String getString() {
		if (builder.length() > 0) {
			return builder.toString();
		}
		return null;
	}

	private Long getLong() {
		if (builder.length() > 0) {
			try {
				return Long.valueOf(builder.toString());
			} catch (NumberFormatException e) {
				System.err.println("Wrong number format [" + getString() + "]! " + e.getMessage());
			}
		}
		return null;
	}

	private Double getDouble() {
		if (builder.length() > 0) {
			try {
				return Double.valueOf(getString());
			} catch (NumberFormatException e) {
				System.err.println("Wrong number format [" + getString() + "]! " + e.getMessage());
			}
		}
		return null;
	}

	private Calendar getCalendar() {
		if (builder.length() > 0) {
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(sdf.parse(getString("T", " ")));
				return cal;
			} catch (ParseException e) {
				System.err.println("Wrong date format [" + getString() + "]" + e.getMessage());
			}
		}
		return null;
	}

	private TicketStatus getTicketstatus() {
		if (builder.length() > 0) {
			try {
				return TicketStatus.valueOf(getString());
			} catch (IllegalArgumentException e) {
				System.err.println("Wrong ticket status value [" + getString() + "]" + e.getMessage());
			}
		}
		return null;
	}

	public Events getEvents() {
		return events;
	}

}
