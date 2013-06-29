package com.mybandtrackr.android.domain;

import java.util.Calendar;

public class Trip {
	
	private String displayName;
	private Calendar startDate;
	private Calendar endDate;
	private String primaryLocation;
	private String city;
	private Double latitude;
	private Double longitude;
	
	@Override
	public String toString() {
		if(null != displayName && !"".equals(displayName)){
			return displayName;
		}
		else{
			return city;
		}
	}
	

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public String getPrimaryLocation() {
		return primaryLocation;
	}

	public void setPrimaryLocation(String primaryLocation) {
		this.primaryLocation = primaryLocation;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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
	
}
