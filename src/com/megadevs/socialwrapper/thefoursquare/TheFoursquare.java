package com.megadevs.socialwrapper.thefoursquare;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jiramot.foursquare.android.Foursquare;
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
	
	public final String clientIDKey = "client_id";
	public final String accessTokenKey = "access_token";
	public final String callbackURLKey = "callback_url";
	
	private ArrayList<SocialFriend> mFoursquareFriends;
	
	public TheFoursquare(Activity a) {
		mActivity = a;
		
		SocialNetwork.tag = "SocialWrapper-Foursquare";
	}

	@Override
	public void authenticate() {
		Log.i(tag, "dopo activity");
		
		if (mFoursquare.isSessionValid())
			Log.i(tag, "session valid, use it wisely :P");
		else {
			Intent i = new Intent(mActivity, TheFoursquareActivity.class);
			Bundle b = new Bundle();
			b.putString(clientIDKey, clientID);
			b.putString(callbackURLKey, callbackURL);
			i.putExtras(b);
			mActivity.startActivity(i);
		}
			
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
	
//	public void setActionResult(String result) {
//		actionResult = result;
//	}

	public void setFoursquare(Foursquare obj) {
		mFoursquare = obj;
		
		Log.i(tag, "session validation: "+mFoursquare.isSessionValid());
		
		SocialSessionStore.save(SocialWrapper.FOURSQUARE, this, mActivity);
	}
}
