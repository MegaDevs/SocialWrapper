package com.megadevs.socialwrapper;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.megadevs.socialwrapper.exceptions.SocialNetworkNotFoundException;
import com.megadevs.socialwrapper.thefacebook.TheFacebook;
import com.megadevs.socialwrapper.theflickr.TheFlickr;
import com.megadevs.socialwrapper.thefoursquare.TheFoursquare;
import com.megadevs.socialwrapper.thetwitter.TheTwitter;

public class SocialWrapper {
	
	public static String THEFACEBOOK = "Facebook";
	public static String THETWITTER = "Twitter";
	public static String THEFOURSQUARE = "Foursquare";
	public static String THETUMBLR = "Tumblr";
	public static String THEFLICKR = "Flickr";
	
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
	 * @throws SocialNetworkNotFoundException when no socialnetwork with the provided id was found
	 */
	public SocialNetwork getSocialNetwork(String id) throws SocialNetworkNotFoundException {
		if (socialNetworks.get(id) == null) {
			if (id.equals(THEFACEBOOK)) {
				TheFacebook fb = new TheFacebook(id, mActivity);
				socialNetworks.put(THEFACEBOOK, fb);
				return fb;
			}
			if (id.equals(THETWITTER)) {
				TheTwitter tw = new TheTwitter(id, mActivity);
				socialNetworks.put(THETWITTER, tw);
				return tw;
			}
			if (id.equals(THEFOURSQUARE)) {
				TheFoursquare fs = new TheFoursquare(id, mActivity);
				socialNetworks.put(THEFOURSQUARE, fs);
				return fs;
			}
			if (id.equals(THEFLICKR)) {
				TheFlickr fl = new TheFlickr(id, mActivity);
				socialNetworks.put(THEFLICKR, fl);
				return fl;
			}
			
			// nothing was found, so an exception must be risen
			throw new SocialNetworkNotFoundException("The selected SocialNetwork could not be found", null);
		}
		else
			return socialNetworks.get(id);
	}
}
