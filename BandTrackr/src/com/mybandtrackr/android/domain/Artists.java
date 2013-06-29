package com.mybandtrackr.android.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Artists implements Serializable {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 5758071489042472163L;
	
	private List<Artist> artists;
	
	public Artists(){
		artists = new ArrayList<Artist>();
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}
	
}
