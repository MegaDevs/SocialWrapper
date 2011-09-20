package com.megadevs.socialwrapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import android.content.Context;

public abstract class SocialNetwork {
	
	protected Map<String, String> connectionData;
	protected boolean connected;
	protected Context context;
	protected String actionResult;
	
	public static String logTag;

	public static final String ACTION_SUCCESSFUL = "Action successfully performed";
	public static final String GENERAL_ERROR = "General error, check logs";
	public static final String SOCIAL_NETWORK_ERROR = "Social network error, retry";
	public static final String ACTION_CANCELED = "Action interrupted by user";
	
	public abstract void authenticate();
	
	public abstract String selfPost(String msg);
	public abstract String postToFriend(String friendID, String msg);
	
	public abstract ArrayList<SocialFriend> getFriendsList();
	public abstract ArrayList<String> getFriendsUsingCorso12();

	protected void setConnectionData(Map<String, String> connectionData) {
		this.connectionData = connectionData;
	}

	protected abstract Vector<String[]> getConnectionData();
	
	protected boolean isConnected() {
		return this.connected;
	}
}
