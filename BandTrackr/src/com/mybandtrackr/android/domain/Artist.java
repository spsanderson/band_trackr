package com.mybandtrackr.android.domain;

import java.io.Serializable;

public class Artist implements Serializable {

	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -9111435548332236704L;
	
	private String name;
	private String url;
	private String mbid;
	
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
	public String getMbid() {
		return mbid;
	}
	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
	
}
