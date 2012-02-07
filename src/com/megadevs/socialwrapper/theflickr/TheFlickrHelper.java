package com.megadevs.socialwrapper.theflickr;

import javax.xml.parsers.ParserConfigurationException;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.REST;
import com.gmail.yuyang226.flickr.RequestContext;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;

public final class TheFlickrHelper {

	private static TheFlickrHelper instance = null;
	private static String API_KEY;
	private static String API_SEC;
	
	private TheFlickrHelper() {}

	public static TheFlickrHelper getInstance() {
		if (instance == null) {
			instance = new TheFlickrHelper();
		}

		return instance;
	}

	public Flickr getFlickr() {
		try {
			Flickr f = new Flickr(API_KEY, API_SEC, new REST());
			return f;
		} catch (ParserConfigurationException e) {
			return null;
		}
	}

	public Flickr getFlickrAuthed(String token, String secret) {
		Flickr f = getFlickr();
		RequestContext requestContext = RequestContext.getRequestContext();
		OAuth auth = new OAuth();
		auth.setToken(new OAuthToken(token, secret));
		requestContext.setOAuth(auth);
		return f;
	}

	/**
	 * @param key the aPI_KEY to set
	 */
	public static void setAPIKey(String key) {
		API_KEY = key;
	}

	/**
	 * @param secret the aPI_SEC to set
	 */
	public static void setAPISec(String secret) {
		API_SEC = secret;
	}

}
