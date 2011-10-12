package com.megadevs.socialwrapper;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.megadevs.socialwrapper.foursquare.TheFoursquare;
import com.megadevs.socialwrapper.thefacebook.TheFacebook;
import com.megadevs.socialwrapper.thetwitter.TheTwitter;

public class SocialWrapper {
	
	public static String FACEBOOK = "facebook";
	public static String TWITTER = "twitter";
	public static String FOURSQUARE = "foursquare";
	public static String TUMBLR = "tumblr";
	public static String FLICKR = "flickr";
	
	private static SocialWrapper socialWrapper;
	private static Map<String, SocialNetwork> socialNetworks;

	private static Activity mActivity;
	
	public static SocialWrapper getInstance() {
		if(socialWrapper == null)
			socialWrapper = new SocialWrapper();
		
		return socialWrapper;
	}
	
	/**
	 * Sets the activity which asked for a SocialWrapper instance.
	 * The activity reference is needed by some SocialNetwork objects
	 * and therefore passed to their respective constructors.
	 * @param a
	 */
	public void setActivity(Activity a) {
		mActivity = a;
	}
	
	/**
	 * Private constuctor for the SocialWrapper class, goes along
	 * with the singleton implementation.
	 */
	private SocialWrapper() {
		socialNetworks = new HashMap<String, SocialNetwork>();
	}

	/**
	 * This method returns a SocialNetwork object with the specified
	 * 'id', if it already exists; otherwise, it creates a new one
	 * (similar to a singleton implementation).
	 * @param id the SocialNetwork id that must be returned
	 * @return the proper SocialNetwork instance
	 */
	public SocialNetwork getSocialNetwork(String id) {
		if (socialNetworks.get(id) == null) {
			if (id.equals(FACEBOOK)) {
				TheFacebook fb = new TheFacebook(mActivity);
				socialNetworks.put(FACEBOOK, fb);
				return fb;
			}
			if (id.equals(TWITTER)) {
				TheTwitter tw = new TheTwitter(mActivity);
				socialNetworks.put(TWITTER, tw);
				return tw;
			}
			if (id.equals(FOURSQUARE)) {
				TheFoursquare fs = new TheFoursquare(mActivity);
				socialNetworks.put(FOURSQUARE, fs);
				return fs;
			}
			return null;
		}
		else
			return socialNetworks.get(id);
	}
}
