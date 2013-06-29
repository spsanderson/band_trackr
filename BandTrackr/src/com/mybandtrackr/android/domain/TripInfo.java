package com.mybandtrackr.android.domain;

import java.util.ArrayList;
import java.util.List;

public class TripInfo {

	private List<Trip> trips = new ArrayList<Trip>();

	public List<Trip> getTrips() {
		return trips;
	}

	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}

}
