package com.mybandtrackr.android.tripit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mybandtrackr.android.domain.Trip;
import com.mybandtrackr.android.domain.TripInfo;


public class TripitTravelInfoXmlHandler extends DefaultHandler {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private boolean finished = false;
	private boolean doParseChar = false;
	private StringBuilder builder;

	
	private TripInfo tripInfo;
	private Trip trip;

	private boolean inTrip = false;
	private boolean inPrimaryLocationAddress = false;

	private static final String RESPONSE = "Response";
	private static final String TRIP = "Trip";
	private static final String DISPLAY_NAME = "display_name";
	private static final String START_DATE = "start_date";
	private static final String END_DATE = "end_date";
	private static final String PRIMARY_LOCATION = "primary_location";
	private static final String PRIMARY_LOCATION_ADDRESS = "PrimaryLocationAddress";
	private static final String CITY = "city";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";

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
			if (localName.equalsIgnoreCase(RESPONSE)) {
				finished = true;
			}
			if (localName.equalsIgnoreCase(TRIP)) {
				tripInfo.getTrips().add(trip);
				inTrip = false;
			}
			if (localName.equalsIgnoreCase(DISPLAY_NAME)) {
				if(inTrip){
					trip.setDisplayName(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(START_DATE)) {
				if (inTrip) {
					trip.setStartDate(getCalendar());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(END_DATE)) {
				if (inTrip) {
					trip.setEndDate(getCalendar());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(PRIMARY_LOCATION)) {
				if (inTrip) {
					trip.setPrimaryLocation(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(PRIMARY_LOCATION_ADDRESS)) {
				if (inTrip) {
					inPrimaryLocationAddress = false;
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(CITY)) {
				if (inPrimaryLocationAddress) {
					trip.setCity(getString());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LATITUDE)) {
				if (inPrimaryLocationAddress) {
					trip.setLatitude(getDouble());
				}
				doParseChar = false;
			}
			if (localName.equalsIgnoreCase(LONGITUDE)) {
				if (inPrimaryLocationAddress) {
					trip.setLongitude(getDouble());
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

		if (localName.equalsIgnoreCase(RESPONSE)) {
			tripInfo = new TripInfo();
			finished = false;
		}
		if (localName.equalsIgnoreCase(TRIP)) {
			trip = new Trip();
			inTrip = true;
		}
		if (localName.equalsIgnoreCase(DISPLAY_NAME)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(START_DATE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(END_DATE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(PRIMARY_LOCATION)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(PRIMARY_LOCATION_ADDRESS)) {
			inPrimaryLocationAddress = true;
		}
		if (localName.equalsIgnoreCase(CITY)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LATITUDE)) {
			doParseChar = true;
		}
		if (localName.equalsIgnoreCase(LONGITUDE)) {
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

	public TripInfo getTripInfo() {
		return tripInfo;
	}
}
