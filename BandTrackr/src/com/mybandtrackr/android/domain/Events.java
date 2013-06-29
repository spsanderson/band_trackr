package com.mybandtrackr.android.domain;

import java.util.ArrayList;
import java.util.List;

public class Events {
	
	private List<Event> events = null;
	
	public Events(){
		events = new ArrayList<Event>();
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
}
