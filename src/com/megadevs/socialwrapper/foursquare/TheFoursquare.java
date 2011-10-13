package com.megadevs.socialwrapper.foursquare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.jiramot.foursquare.android.DialogError;
import com.jiramot.foursquare.android.Foursquare;
import com.jiramot.foursquare.android.FoursquareError;
import com.jiramot.foursquare.android.Foursquare.DialogListener;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;

public class TheFoursquare extends SocialNetwork {

	private Foursquare mFoursquare;
	private Activity mActivity;

	private String clientID;
	private String accessToken;
	private String callbackURL;
	
	private final String clientIDKey = "client_id";
	private final String accessTokenKey = "access_token";
	private final String callbackURLKey = "callback_url";
	
	private ArrayList<SocialFriend> mFoursquareFriends;
	
	public TheFoursquare(Activity a) {
		mActivity = a;
		
		SocialNetwork.tag = "SocialWrapper-Foursquare";
	}

	@Override
	public void authenticate() {
		if (mFoursquare.isSessionValid())
			Log.i(tag, "session valid, use it wisely :P");
		else
			mFoursquare.authorize(mActivity, new AuthDialogListener());
	}

	@Override
	public void deauthenticate() {
		SocialSessionStore.clear(SocialWrapper.FOURSQUARE, mActivity);
	}

	@Override
	protected Vector<String[]> getConnectionData() {
		Vector<String[]> data = new Vector<String[]>();
		data.add(new String[] {clientIDKey, clientID});
		data.add(new String[] {accessTokenKey, accessToken});
		data.add(new String[] {callbackURLKey, callbackURL});
		
		return data;
	}

	@Override
	protected void setConnectionData(Map<String, String> connectionData) {
		if (connectionData.size()==0) {
			this.connectionData = null;
		}
		else {
			clientID = connectionData.get(clientIDKey);
			accessToken = connectionData.get(accessTokenKey);
			callbackURL = connectionData.get(callbackURLKey);
			
			mFoursquare.setAccessToken(accessToken);
		}
	}
	
	@Override
	public ArrayList<SocialFriend> getFriendsList() {
		String result = mFoursquare.request("users/self/friends");
		if (result == null) {
			actionResult = SOCIAL_NETWORK_ERROR;
			return null;
		}
		
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject response = obj.getJSONObject("response"); 
			JSONObject friends = response.getJSONObject("friends");
			JSONArray items = friends.getJSONArray("items");
			
			for (int i=0; i<items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				
				String id = item.getString("id");
				String name = item.getString("firstName");
				String surname = item.getString("lastName");
				
				mFoursquareFriends.add(new SocialFriend(id, name+' '+surname, null));
			}

		} catch (JSONException e) {
			Log.e(tag, "JSON error", e);
		}
		
		return mFoursquareFriends;
	}
 
	@Override
	public ArrayList<String> getFriendsUsingCorso12() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFSParams(String id, String url) {
		clientID = id;
		callbackURL = url;
		
		mFoursquare = new Foursquare(clientID, callbackURL);
		
		SocialSessionStore.restore(SocialWrapper.FOURSQUARE, this, mActivity);
	}

	private class AuthDialogListener implements DialogListener {
		@Override
		public void onCancel() {
			actionResult = SocialNetwork.ACTION_CANCELED;
			Log.d(SocialNetwork.tag, SocialNetwork.ACTION_CANCELED);
		}

		@Override 
		public void onComplete(Bundle values) {
			Log.i(tag, "login performed");
			
			actionResult = SocialNetwork.ACTION_SUCCESSFUL;
			accessToken = mFoursquare.getAccessToken();

			connectionData = new HashMap<String, String>();
			connectionData.put(clientIDKey, clientID);
			connectionData.put(callbackURLKey, callbackURL);
			connectionData.put(accessTokenKey, accessToken);
			
			SocialSessionStore.save(SocialWrapper.FOURSQUARE, TheFoursquare.this, mActivity);
		}

		@Override
		public void onError(DialogError e) {
			actionResult = SocialNetwork.GENERAL_ERROR;
			Log.d(SocialNetwork.tag, SocialNetwork.GENERAL_ERROR, e);
		}

		@Override
		public void onFoursquareError(FoursquareError e) {
			actionResult = SocialNetwork.SOCIAL_NETWORK_ERROR;
			Log.d(TheFoursquare.tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
		}
		
	}
	
}
