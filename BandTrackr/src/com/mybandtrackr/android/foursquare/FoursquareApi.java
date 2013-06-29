package com.mybandtrackr.android.foursquare;

import fi.foyt.foursquare.api.io.IOHandler;

public class FoursquareApi extends fi.foyt.foursquare.api.FoursquareApi {

	public static final String OAUTH_PROVIDER = "foursquare";

	private static final String CLIENT_ID = "AOFQTIHSMWQEAHHWIHAACEFA5RRRQHKPUHOZRACEPTPSYVGH";
	private static final String CLIENT_SECRET = "5CRYJT11FED03MPWI3JRA1LDQ1L23U11M5WVOL3FZXYG1UYG";

	private static final String REDIRECT_URL = "bandtrackr-oauth-foursquare://oauth";

	public FoursquareApi() {
		super(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL);
	}

	public FoursquareApi(IOHandler ioHandler) {
		super(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL, ioHandler);
	}

	public FoursquareApi(String oAuthToken, IOHandler ioHandler) {
		super(CLIENT_ID, CLIENT_SECRET, REDIRECT_URL, oAuthToken, ioHandler);
	}
	
	public String getAjaxAuthenticationUrl() {
		return new StringBuilder(
				"https://foursquare.com/oauth2/authenticate?client_id=")
				.append(CLIENT_ID).append("&response_type=token")
				.append("&redirect_uri=").append(REDIRECT_URL)
				.append("&display=touch").toString();
	}

}
