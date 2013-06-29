package com.mybandtrackr.android.bit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mybandtrackr.android.App;
import com.mybandtrackr.android.domain.Events;
import com.mybandtrackr.android.protocol.CustomHttpClient;

public class BitClient {

	private static final String TAG = BitClient.class.getSimpleName();
	private final Activity context;
	private final Location location;

	public BitClient(Activity context, Location location) {
		this.context = context;
		this.location = location;
	}

	public Events getEvents(Calendar startDate, Calendar endDate) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		String bitRequest = generateBitRequest(location, startDate, endDate);
		Log.d(TAG, "BandsInTown GET request: " + bitRequest);
		try {
			InputSource source = (InputSource) CustomHttpClient.executeGet(App.getHttpClient(), bitRequest, CustomHttpClient.ReturnType.InputSource);

			if (null != source) {
				parser = factory.newSAXParser();
				XMLReader xr = parser.getXMLReader();

				BitEventsXmlHandler handler = new BitEventsXmlHandler();
				xr.setContentHandler(handler);
				xr.parse(source);

				return handler.getEvents();
			}

		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Error instantiating SAX Parser! " + e.getMessage());
		} catch (SAXException e) {
			Log.e(TAG, "Error instantiating SAX Parser! " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "Error getting events or parsing XML document! " + e.getMessage());
		} catch (URISyntaxException e) {
			Log.e(TAG, "Error, wrong HTTP BiT request generated (" + bitRequest + ")! " + e.getMessage());
		}

		return null;
	}

	private String generateBitRequest(Location currentLocation, Calendar startDate, Calendar endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://api.bandsintown.com/events/search?location=");
		sb.append(currentLocation.getLatitude());
		sb.append(",");
		sb.append(currentLocation.getLongitude());

		int distKm = App.getMaxDistance(context);
		long distMiles = Math.round(distKm * 0.621371192);
		if (distMiles > 150) {
			distMiles = 150;
		}
		sb.append("&radius=" + distMiles);
		if (null != startDate && null != endDate) {
			sb.append("&date=");
			sb.append(App.sdfDateStandard.format(startDate.getTime()));
			sb.append(",");
			sb.append(App.sdfDateStandard.format(endDate.getTime()));
		}
		sb.append("&format=xml&app_id=mybandtrackr");
		return sb.toString();
	}

}
