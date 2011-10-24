package com.megadevs.socialwrapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import android.content.Context;

/**
 * This class tries to model a social network by abstracting its concept. Since every social
 * network differs from the others in many ways, it is hard to define a common model for them all.
 *  
 * @author dextor
 *
 */
public abstract class SocialNetwork {
	
	protected Map<String, String> connectionData;
	protected Context context;
	protected String actionResult;
	
	public static String tag;

	public static final String ACTION_SUCCESSFUL = "Action successfully performed";
	public static final String GENERAL_ERROR = "General error, check logs";
	public static final String SOCIAL_NETWORK_ERROR = "Social network error, retry";
	public static final String ACTION_CANCELED = "Action interrupted by user";
	
	/**
	 * General abstract method which performs authentication.
	 */
	public abstract void authenticate();
	
	/**
	 * General abstract method which performs deauthentication. Since it is not possible
	 * to revoke an authorization for an application via OAuth, this method simply must 
	 * clear the saved prefs.
	 */
	public abstract void deauthenticate();
	
	/**
	 * This method retrieves the list of all the actual friends/followers of a social
	 * account.
	 * @return
	 */
	public abstract ArrayList<SocialFriend> getFriendsList();
	
	/**
	 * This method retrieves all the friends/followers of a social account which are using the
	 * Corso12 application.
	 * @return
	 */
	public abstract ArrayList<String> getFriendsUsingCorso12();

	/**
	 * This method is used to save the connection parameters of a SocialNetwork instance in the prefs.
	 * @return vector of String[key,value] of the connection parameters that will be saved in prefs.
	 */
	protected abstract Vector<String[]> getConnectionData();
	
	/**
	 * This method is used to restore the connection parameters, retrieved from the prefs, to the 
	 * SocialNetwork instance.
	 * @param connectionData vector of String[key,value] of the connection parameters 
	 * that will be restored
	 */
	protected abstract void setConnectionData(Map<String, String> connectionData);
	
}
