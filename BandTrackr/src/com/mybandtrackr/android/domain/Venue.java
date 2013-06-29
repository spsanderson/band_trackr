package com.mybandtrackr.android.domain;

import java.io.Serializable;

import android.location.Address;
import android.location.Location;

import com.mybandtrackr.android.Util;
import com.mybandtrackr.android.ticketleap.TicketLeapClient.TicketleapEvent;

public class Venue implements Serializable {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -8145924377770730180L;

	private Long id;
	private String url;
	private String name;
	private String city;
	private String region;
	private String country;
	private Double latitude;
	private Double longitude;

	public Venue() {

	}

	public Venue(TicketleapEvent eventDetail) {
		this.url = eventDetail.url;
		this.name = eventDetail.venueName;
		this.city = eventDetail.venueCity;
		this.region = eventDetail.venueRegionName;
		this.country = eventDetail.venueCountryCode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String string) {
		this.region = string;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public boolean isNearByLocation(Address another, Location location, int maxDistance, int maxLsed) {
		Double latitude = null;
		Double longitude = null;
		if (null != another && another.hasLatitude() && another.hasLongitude()) {
			latitude = another.getLatitude();
			longitude = another.getLongitude();
		} else if (null != location) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
		if (null != this.latitude && null != this.longitude && null != latitude && null != longitude) {

			// try to compare by point locaion
			float[] results = new float[1];
			Location.distanceBetween(this.latitude, this.longitude, latitude, longitude, results);
			if (results.length > 0) {
				if (results[0] < maxDistance * 1000) {
					return true;
				}
			}
		} else if (null != another) {

			// try to compare with address info
			boolean countryNameMatch = Util.isSimilar(this.getCountry(), another.getCountryName(), maxLsed);
			boolean countryCodeMatche = Util.isSimilar(this.getCountry(), another.getCountryCode(), maxLsed);
			boolean regionAdminAreaMatch = Util.isSimilar(this.getRegion(), another.getAdminArea(), maxLsed);
			boolean regionSubAdminAreaMatch = Util.isSimilar(this.getRegion(), another.getSubAdminArea(), maxLsed);
			boolean cityLocalityMatch = Util.isSimilar(this.getCity(), another.getLocality(), maxLsed);
			boolean citySubLocalityMatch = Util.isSimilar(this.getCity(), another.getSubLocality(), maxLsed);
			boolean cityFeatureNameMatch = Util.isSimilar(this.getCity(), another.getFeatureName(), maxLsed);
			boolean cityThoroughfareMatch = Util.isSimilar(this.getCity(), another.getThoroughfare(), maxLsed);

			if (countryCodeMatche || countryNameMatch) {

				if (regionAdminAreaMatch || regionSubAdminAreaMatch) {

					if (cityLocalityMatch || citySubLocalityMatch || cityFeatureNameMatch || cityThoroughfareMatch) {

						return true;
					}
				}
			}

		}

		return false;
	}

	public boolean isNearByLocation(Venue another, int maxDistance, int maxLsed) {
		if (null != this.latitude && null != this.longitude && null != another.latitude && null != another.longitude) {

			// try to compare by point locaion
			float[] results = new float[] {};
			Location.distanceBetween(this.latitude, this.longitude, another.getLatitude(), another.getLongitude(), results);
			if (results.length > 0) {
				if (results[0] < maxDistance) {
					return true;
				}
			}
		} else {

			// try to compare with address info
			boolean countryMatch = Util.isSimilar(this.getCountry(), another.getCountry(), maxLsed);
			boolean regionMatch = Util.isSimilar(this.getRegion(), another.getRegion(), maxLsed);
			boolean cityMatch = Util.isSimilar(this.getCity(), another.getCity(), maxLsed);

			if (countryMatch && regionMatch && cityMatch) {
				return true;
			}

		}

		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Venue) {
			Venue another = (Venue) o;
			isNearByLocation(another, 100, 0);
		}
		return super.equals(o);
	}
}
