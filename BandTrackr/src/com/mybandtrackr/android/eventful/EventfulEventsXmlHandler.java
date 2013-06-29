package com.mybandtrackr.android.eventful;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mybandtrackr.android.domain.Artist;
import com.mybandtrackr.android.domain.Event;
import com.mybandtrackr.android.domain.Events;
import com.mybandtrackr.android.domain.Venue;


public class EventfulEventsXmlHandler extends DefaultHandler {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private boolean finished = false;
	private boolean doParseChar = false;
	private StringBuilder builder;

	private Events events;
	private Event event;
	private Artist artist;
	private Venue venue;

	private boolean inEvents = false;
	private boolean inEvent = false;
	private boolean inPerformers = false;
	private boolean inPerformer = false;

	private static final String SEARCH = "search";
	private static final String EVENTS = "events";
	private static final String EVENT = "event";
	private static final String TITLE = "title";
	private static final String URL = "url";
	private static final String START_TIME = "start_time";
	private static final String VENUE_URL = "venue_url";
	private static final String VENUE_NAME = "venue_name";
	private static final String VENUE_ADDRESS = "venue_address";
	private static final String CITY_NAME = "city_name";
	private static final String REGION_NAME = "region_name";
	private static final String COUNTRY_NAME = "country_name";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String PERFORMERS = "performers";
	private static final String PERFORMER = "performer";
	private static final String NAME = "name";

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
			if (localName.equalsIgnoreCase(SEARCH)) {
				finished = true;
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(EVENTS)) {
				inEvents = false;
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(EVENT)) {
				if (inEvents) {
					event.setVenue(venue);
					events.getEvents().add(event);
					inEvent = false;
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(TITLE)) {
				if (inEvent) {
					event.setName(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(URL)) {
				if (inEvent) {
					event.setUrl(getString());
				}
				if (inPerformer) {
					artist.setUrl(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(START_TIME)) {
				if (inEvent) {
					event.setDatetime(getCalendar());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(VENUE_URL)) {
				if (inEvent) {
					venue.setUrl(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(VENUE_NAME)) {
				if (inEvent) {
					venue.setName(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(VENUE_ADDRESS)) {
				if (inEvent) {
					venue.setRegion(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(CITY_NAME)) {
				if (inEvent) {
					venue.setCity(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LATITUDE)) {
				if (inEvent) {
					venue.setLatitude(getDouble());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LONGITUDE)) {
				if (inEvent) {
					venue.setLongitude(getDouble());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(PERFORMERS)) {
				if (inEvent) {
					inPerformers = false;
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(PERFORMER)) {
				if (inPerformers) {
					event.getArtists().add(artist);
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(NAME)) {
				if (inPerformer) {
					artist.setName(getString());
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
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);

		if (localName.equalsIgnoreCase(SEARCH)) {
			finished = false;
			doParseChar = false;
		}
		if (localName.equalsIgnoreCase(EVENTS)) {
			inEvents = true;
			events = new Events();
			doParseChar = false;
		}
		if (localName.equalsIgnoreCase(EVENT)) {
			event = new Event();
			venue = new Venue();
			inEvent = true;
			doParseChar = false;
		}
		if (localName.equalsIgnoreCase(TITLE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(URL)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(START_TIME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(VENUE_URL)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(VENUE_NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(VENUE_ADDRESS)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(CITY_NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(REGION_NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(COUNTRY_NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LATITUDE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LONGITUDE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(PERFORMERS)) {
			inPerformers = true;
			doParseChar = false;
		}
		if (localName.equalsIgnoreCase(PERFORMER)) {
			artist = new Artist();
			inPerformer = true;
			doParseChar = false;
		}
		if (localName.equalsIgnoreCase(NAME)) {
			doParseChar = true;
		}

	}

	private String getString() {
		if (builder.length() > 0) {
			return builder.toString();
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
				cal.setTime(sdf.parse(getString()));
				return cal;
			} catch (ParseException e) {
				System.err.println("Wrong date format [" + getString() + "]" + e.getMessage());
			}
		}
		return null;
	}

	public Events getEvents() {
		return events;
	}

}
