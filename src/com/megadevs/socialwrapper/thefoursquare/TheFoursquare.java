package com.megadevs.socialwrapper.thefoursquare;

import java.io.File;
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

import com.jiramot.foursquare.android.Foursquare;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;
import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;
import com.megadevs.socialwrapper.utils.HTTPPostParameters;
import com.megadevs.socialwrapper.utils.HTTPResult;
import com.megadevs.socialwrapper.utils.Utils;

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

	private String clientID;
	private String clientSecret;
	private String callbackURL;
	private String accessToken;
	
	public final String clientIDKey = "client_id";
	public final String clientSecretKey = "client_secret";
	public final String callbackURLKey = "callback_url";
	public final String accessTokenKey = "access_token";
	
	// static callback refs
	private TheFoursquareLoginCallback loginCallback;
	private TheFoursquareFriendListCallback friendslistCallback;
	private TheFoursquareSearchCallback searchCallback;
	private TheFoursquareCheckinCallback checkinCallback;
	private TheFoursquarePostPictureCallback postPictureCallback;
	
	private ArrayList<SocialFriend> mFoursquareFriends;
	/**
	 * Defaul constructor for the TheFoursquare class.
	 * @param a the main activity
	 */
	public TheFoursquare(String id, Activity a) {
		this.id = id;
		this.mActivity = a;
		iAmTheFoursquare = this;
		
		tag = "SocialWrapper-Foursquare";
	}

	/**
	 * !-NOTE: THIS METHOD SHOULD NOT BE USED BY THE END USER!-! 
	 * This method is used within the callback procedure to
	 * set the authenticated Foursquare object.
	 * @return the existing instance of TheFoursquare
	 */
	public static TheFoursquare getInstance() {
		return iAmTheFoursquare;
	}
	
	/**
	 * This method is called from outside the wrapper and it is used to
	 * set the application ID and the callback URL; the 
	 * instanciation of the mFoursquare object can be done 
	 * only after this action is completed.
	 * @param id the application id provided by Foursquare
	 */
	public void setAuthParams(String id, String secret, String url) {
		clientID = id;
		clientSecret = secret;
		callbackURL = url;
		
		mFoursquare = new Foursquare(clientID, callbackURL);
		
		SocialSessionStore.restore(SocialWrapper.THEFOURSQUARE, this, mActivity);
	}
	
	/**
	 * !-NOTE: THIS METHOD SHOULD NOT BE USED BY THE END USER!-!
	 * @param obj
	 */
	public void setFoursquare(Foursquare obj) {
		mFoursquare = obj;
		// setting the newly-received access token
		accessToken = mFoursquare.getAccessToken();
		
		Log.i(tag, "session validation: "+mFoursquare.isSessionValid());

		SocialSessionStore.save(SocialWrapper.THEFOURSQUARE, this, mActivity);
	}
	
	@Override
	public void authenticate(SocialBaseCallback r) {
		loginCallback = (TheFoursquareLoginCallback) r;
		if (mFoursquare.isSessionValid()) {
			Log.i(tag, "session valid, use it wisely :P");

			if (loginCallback != null) {
				loginCallback.onLoginCallback(actionResult);
				loginCallback = null;
			}
		}
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
		accessToken = "";
		SocialSessionStore.clear(SocialWrapper.THEFOURSQUARE, mActivity);
	}

	@Override
	protected Vector<String[]> getConnectionData() {
		Vector<String[]> data = new Vector<String[]>();
		data.add(new String[] {clientIDKey, clientID});
		data.add(new String[] {clientSecretKey, clientSecret});
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
			clientSecret = connectionData.get(clientSecretKey);
			callbackURL = connectionData.get(callbackURLKey);
			accessToken = connectionData.get(accessTokenKey);
			
			mFoursquare.setAccessToken(accessToken);
		}
	}
	
	/**
	 * !-NOTE: THIS METHOD SHOULD NOT BE USED BY THE END USER!-!
	 */
	public void forwardErrorResult(Exception e) {
		if (loginCallback != null)
			loginCallback.onErrorCallback(actionResult, e);
		else if (friendslistCallback != null)
			friendslistCallback.onErrorCallback(actionResult, e);
		else if (searchCallback != null)
			searchCallback.onErrorCallback(actionResult, e);
		else if (checkinCallback != null)
			checkinCallback.onErrorCallback(actionResult, e);
		else if (postPictureCallback != null)
			postPictureCallback.onErrorCallback(actionResult, e);
	}

	
	/**
	 * This method is used to seach the nearby venues from the 
	 * current position. Each venue is then encapsulated in a 
	 * TheFoursquareVenue object, which has the latitude/longitude
	 * coordinates, the distance of the venue from the current 
	 * position, the name and the id of the venue (these two are
	 * assigned by Foursquare).
	 * 
	 * @param position the current position
	 * @return an ArrayList of nearby venues
	 * @throws InvalidSocialRequestException
	 */
	public ArrayList<TheFoursquareVenue> searchVenues(double latitude, double longitude, String venue) throws InvalidSocialRequestException {
		String ll = String.valueOf(latitude) + "," + String.valueOf(longitude);
		
		Bundle b = new Bundle();
		b.putString("ll", ll);
		b.putString("v", Utils.getCurrentDate());
		
		// eventually a query name is passed, along with the geoposition
		if (venue != null)
			b.putString("query", venue);
		
		// venues are searchable even if no user is logged in
		if (!isAuthenticated()) {
			b.putString(clientIDKey, clientID);
			b.putString(clientSecretKey, clientSecret);
			b.putString("v", Utils.getCurrentDate());
		}

		ArrayList<TheFoursquareVenue> foursquareVenues = null;
		try {
			String result = mFoursquare.request("venues/search", b);
			System.out.println(result);
			
			// parsing the request result
			JSONObject obj = new JSONObject(result);
			JSONObject response = obj.getJSONObject("response");
			JSONArray venues = response.getJSONArray("venues");
			
			foursquareVenues = new ArrayList<TheFoursquareVenue>(venues.length());
			for (int i=0; i<venues.length(); i++) {
				JSONObject item = venues.getJSONObject(i);
				String id = item.getString("id");
				String name = item.getString("name");
				
				JSONObject location = item.getJSONObject("location");
				String lat = location.getString("lat");
				String lon = location.getString("lng");
				String dist = location.getString("distance");
				
				foursquareVenues.add(new TheFoursquareVenue(
						Float.valueOf(lat).intValue(),
						Float.valueOf(lon).intValue(),
						id,
						name, 
						Integer.valueOf(dist).intValue()));
			}
			
		} catch (MalformedURLException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		} catch (IOException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		} catch (JSONException e) {
			throw new InvalidSocialRequestException("Could not retrieve the nearby venues", e);
		}
		return foursquareVenues;
	}
	
	
	/**
	 * Method that uploads a picture to Foursquare, given an image file (must be a JPG < 5MB)
	 * and a valid checkin ID. Since the endpoint is a POST-only, a support HTTP library
	 * is used in order to properly process the multipart request and the post parameters.
	 * 
	 * @param image JPG image to upload
	 * @param id a valid checkin ID
	 * @throws InvalidSocialRequestException 
	 */
	public void postPicture(File image, String id, SocialBaseCallback s) {
		postPictureCallback = (TheFoursquarePostPictureCallback) s;
		String endpoint = "https://api.foursquare.com/v2/photos/add?";
		
		HTTPPostParameters params = new HTTPPostParameters();
		params.addParam("checkinId", id);
		params.addParam("file", image);

		// manually adding access token and date (for API verification)
		endpoint += "&oauth_token="+getAccessToken()+"&v="+Utils.getCurrentDate();
		
		try {
			// parsing server response
			HTTPResult r = Utils.executeHTTPUrlPost(endpoint, params, null);
			Log.i(tag, "performed server call, parsing response..");

			if (r != null && r.getData() != null) {
				JSONObject obj = new JSONObject(r.getData());
				obj = obj.getJSONObject("meta");
				
				if (obj.getString("code").contains("200")) {
					Log.i(tag, "picture uploaded");
					actionResult = ACTION_SUCCESSFUL;
					postPictureCallback.onPostPictureCallback(actionResult);
				} else {
					actionResult = SOCIAL_NETWORK_ERROR;
					forwardErrorResult(null);
				}

				postPictureCallback = null;
			}
		} catch (IOException e) {
			postPictureCallback.onErrorCallback(GENERAL_ERROR, e);
			postPictureCallback = null;
		} catch (JSONException e) {
			postPictureCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
			postPictureCallback = null;
		}
	}
	
	
	/**
	 * Method that performs a checkin in the current venue and may post a shout message
	 * (if set). As for the postPicture() method, this is also a POST-only, so the
	 * HTTP support library must be used again.
	 * 
	 * @param id
	 * @return
	 * @throws InvalidSocialRequestException 
	 */
	public void checkIn(String id, String message, SocialBaseCallback s) {
		checkinCallback = (TheFoursquareCheckinCallback) s;
		Log.i(tag, "checkin");
		String endpoint = "https://api.foursquare.com/v2/checkins/add?";
		
		HTTPPostParameters params = new HTTPPostParameters();
		params.addParam("venueId", id);
		params.addParam("shout", message);
		
		endpoint += "&oauth_token="+getAccessToken()+"&v="+Utils.getCurrentDate();
		
		try {
			// parsing server response
			HTTPResult r = Utils.executeHTTPUrlPost(endpoint, params, null);
			Log.i(tag, "performed server call, parsing response..");
			
			// check if the checkin was successful			
			JSONObject obj = new JSONObject(r.getData());
			JSONArray notifications = obj.getJSONArray("notifications");
			obj = notifications.getJSONObject(1);
			obj = obj.getJSONObject("item");
			String result = obj.getString("message");
			
			if (result.contains("OK!")) {
				Log.i(tag, "checkin performed, returning its ID");

				// retrieve checkin id
				obj = new JSONObject(r.getData());
				obj = obj.getJSONObject("response").getJSONObject("checkin");
				String checkinId = obj.getString("id");
				actionResult = ACTION_SUCCESSFUL;
				checkinCallback.onCheckinCallback(result, checkinId);

			} else {
				actionResult = SOCIAL_NETWORK_ERROR;
				forwardErrorResult(null);
			}

			checkinCallback = null;
		
		} catch (MalformedURLException e) {
			checkinCallback.onErrorCallback(GENERAL_ERROR, e);
			checkinCallback = null;
		} catch (IOException e) {
			checkinCallback.onErrorCallback(GENERAL_ERROR, e);
			checkinCallback = null;
		} catch (JSONException e) {
			checkinCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
			checkinCallback = null;
		}
	}
	
	
	@Override
	public void getFriendsList(SocialBaseCallback s) {
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
			checkinCallback.onErrorCallback(GENERAL_ERROR, e);
		}
	}

	@Override
	public String getAccessToken() {
		if (accessToken != null)
			return accessToken;
		
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		if (accessToken != null && accessToken != "") return true;
		else return false;
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	public void setActionResult(String result) {actionResult = result;}
	
	///
	///	CALLBACK ADAPTER CLASSES
	///
	
	public static abstract class TheFoursquareLoginCallback implements SocialBaseCallback {
		public abstract void onLoginCallback(String result);
		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
		public void onCheckinCallback(String result, String checkinId) {};
		public void onPostPictureCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e); 
	}

	public static abstract class TheFoursquareSearchCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public abstract void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list);
		public void onCheckinCallback(String result, String checkinId) {};		public void onPostPictureCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e);
	}

	public static abstract class TheFoursquareFriendListCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
		public void onCheckinCallback(String result, String checkinId) {};		public void onPostPictureCallback(String result) {};
		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
		public abstract void onErrorCallback(String error, Exception e);
	}

	public static abstract class TheFoursquarePostPictureCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
		public void onCheckinCallback(String result, String checkinId) {};
		public abstract void onPostPictureCallback(String result);
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e); 
	}

	public static abstract class TheFoursquareCheckinCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onSearchVenuesCallback(String result, ArrayList<TheFoursquareVenue> list) {};
		public abstract void onCheckinCallback(String result, String checkinId);
		public void onPostPictureCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e); 
	}

}
