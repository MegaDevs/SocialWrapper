package com.megadevs.socialwrapper.thefoursquare;

import java.io.IOException;
import java.net.MalformedURLException;
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

import com.google.android.maps.GeoPoint;
import com.jiramot.foursquare.android.Foursquare;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;
import com.megadevs.socialwrapper.exceptions.InvalidAuthenticationException;
import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;

/**
 * This class models a personal Foursquare object. With an instance of 
 * TheFoursquare it is possible to authenticate, search venues and retrieve
 * various informations.
 * @author dextor
 *
 */
public class TheFoursquare extends SocialNetwork {

	private Foursquare mFoursquare;
	private static TheFoursquare iAmTheFoursquare;
	private Activity mActivity;

	private String clientID;
	private String callbackURL;
	private String accessToken;
	
	public final String clientIDKey = "client_id";
	public final String callbackURLKey = "callback_url";
	public final String accessTokenKey = "access_token";
	
	private ArrayList<SocialFriend> mFoursquareFriends;
	/**
	 * Defaul constructor for the TheFoursquare class.
	 * @param a the main activity
	 */
	public TheFoursquare(Activity a) {
		mActivity = a;
		iAmTheFoursquare = this;
		
		SocialNetwork.tag = "SocialWrapper-Foursquare";
	}

	public static TheFoursquare getInstance() {
		return iAmTheFoursquare;
	}
	
	@Override
	public void authenticate() throws InvalidAuthenticationException {
		if (mFoursquare.isSessionValid())
			Log.i(tag, "session valid, use it wisely :P");
		else {
			Intent i = new Intent(mActivity, TheFoursquareActivity.class);
			Bundle b = new Bundle();
			b.putString(clientIDKey, clientID);
			b.putString(callbackURLKey, callbackURL);
			i.putExtras(b);
			mActivity.startActivity(i);
			
			if (!mFoursquare.isSessionValid())
				throw new InvalidAuthenticationException("Could not login on Fourquare", null);
				
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
			callbackURL = connectionData.get(callbackURLKey);
			accessToken = connectionData.get(accessTokenKey);
			
			mFoursquare.setAccessToken(accessToken);
		}
	}
	
	public ArrayList<TheFoursquareVenue> searchVenues(GeoPoint position) throws InvalidSocialRequestException {
		int longitude = position.getLongitudeE6();
		int latitude = position.getLatitudeE6();
		String ll = String.valueOf(longitude) + "," + String.valueOf(latitude);
		
		Bundle b = new Bundle();
		b.putString("ll", ll);

		ArrayList<TheFoursquareVenue> venues = null;
		try {
			String result = mFoursquare.request("venues/search", b);
			
			// parsing the request result
			JSONObject obj = new JSONObject(result);
			JSONObject response = obj.getJSONObject("response");
			JSONArray groups = response.getJSONArray("groups");
			JSONObject element = groups.getJSONObject(0);
			JSONArray items = element.getJSONArray("items");
			
			venues = new ArrayList<TheFoursquareVenue>(items.length());
			for (int i=0; i<items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				String id = item.getString("id");
				String name = item.getString("name");
				
				JSONObject location = item.getJSONObject("location");
				String lat = location.getString("lat");
				String lon = location.getString("lng");
				String dist = location.getString("distance");
				
				venues.add(new TheFoursquareVenue(
						Float.valueOf(lat).intValue(),
						Float.valueOf(lon).intValue(),
						name,
						id, 
						Integer.valueOf(dist).intValue()));
			}
			
		} catch (MalformedURLException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		} catch (IOException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		} catch (JSONException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		}
		return venues;
	}
	
	@Override
	public ArrayList<SocialFriend> getFriendsList() throws InvalidSocialRequestException {
		mFoursquareFriends = new ArrayList<SocialFriend>();

		try {
			String result = mFoursquare.request("users/self/friends");
			
			JSONObject obj;
			obj = new JSONObject(result);
			// corresponds to the JSON response structure; see official API documentation
			// for more informations
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
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
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
	
	public void setFoursquare(Foursquare obj) {
		mFoursquare = obj;
		// setting the newly-received access token
		accessToken = mFoursquare.getAccessToken();
		
		Log.i(tag, "session validation: "+mFoursquare.isSessionValid());
		
		SocialSessionStore.save(SocialWrapper.FOURSQUARE, this, mActivity);
	}
}
