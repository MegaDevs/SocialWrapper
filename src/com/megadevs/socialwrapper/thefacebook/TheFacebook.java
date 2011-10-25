package com.megadevs.socialwrapper.thefacebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;
import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;

public class TheFacebook extends SocialNetwork {

	private static TheFacebook iAmTheFacebook;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

	private ArrayList<SocialFriend> mFacebookFriends;
	private volatile boolean updateRunning;

	private Activity mActivity;
	private String appID;
	private String accessToken;
	private long accessExpires;

	private final String appIDKey = "app_id";
	private final String accessTokenKey = "access_token";
	private final String accessExpiresKey = "access_token_expires";

	/**
	 * Default constructor for TheFacebook class. A context is
	 * required in order to perform authentication and display
	 * Facebook dialogs (if the official app is installed).
	 * @param context
	 */
	public TheFacebook(Activity a) {
		iAmTheFacebook = this;
		mActivity = a;
		context = mActivity.getApplicationContext();
		updateRunning = false;

		SocialNetwork.tag = "SocialWrapper-Facebook";
	}

	public static TheFacebook getInstance() {
		return iAmTheFacebook;
	}

	public void setAppID(String id) {
		appID = id;
		mFacebook = new Facebook(appID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

		SocialSessionStore.restore(SocialWrapper.FACEBOOK, this, context);
	}

	@Override
	public Vector<String[]> getConnectionData() {
		Vector<String[]> data = new Vector<String[]>();
		data.add(new String[] {appIDKey, accessTokenKey});
		data.add(new String[] {accessTokenKey, mFacebook.getAccessToken()});
		data.add(new String[] {accessExpiresKey, String.valueOf(mFacebook.getAccessExpires())});
		return data;
	}

	@Override
	protected void setConnectionData(Map<String, String> connectionData) {
		if (connectionData.size()==0) {
			this.connectionData = null;
		}
		else {
			appID = connectionData.get(appIDKey);
			accessToken = connectionData.get(accessTokenKey);
			accessExpires = (Long.valueOf(connectionData.get(accessExpiresKey)).longValue());
			mFacebook.setAccessToken(accessToken);
			mFacebook.setAccessExpires(accessExpires);
		}
	}

	@Override
	public void authenticate() {
		if (mFacebook.isSessionValid())
			Log.i(tag, "session valid, use it wisely ;)");
		else {
			mActivity.startActivity(new Intent(mActivity, TheFacebookActivity.class));
			Log.i("corso", "valid session: " + mFacebook.isSessionValid());
		}
	}

	@Override
	public void deauthenticate() {
		SocialSessionStore.clear(SocialWrapper.FACEBOOK, context);
	}

	/**
	 * This method is used to post on the user's wall. There is no message, since a dialog
	 * will appear and let the user insert a custom message to post.
	 * @return a string containing the result of the operation
	 */
	public String postOnMyWall() {
		Bundle parameters = new Bundle();
		this.mFacebook.dialog(mActivity,
				"stream.publish",
				parameters,
				new PostOnWallDialogListener());
		
		return actionResult;
	}

//	private void handleResult() {
//		if (actionResult.equals(SocialNetwork.GENERAL_ERROR))
//			throw new 
//	}
//	
	public String postToFriendsWall(String friendID, String msg) {
		Bundle parameters = new Bundle();
		parameters.putString("to", friendID);
		mFacebook.dialog(mActivity,
				"stream.publish",
				parameters,
				new PostOnWallDialogListener());
		return actionResult;
	}

	@Override
	public ArrayList<SocialFriend> getFriendsList() throws InvalidSocialRequestException {
		getFriendsAsync();
		return mFacebookFriends;
	}

	/**
	 * This method is actually used to invoke the asynchronous request for retrieving the 
	 * list of friends and is only called by the public method getFriendsList(). It waits until
	 * the response is obtained, parsed and properly processed, then ends and lets the 
	 * getFriendsList() method return the whole data.
	 * @throws InvalidSocialRequestException
	 */
	private void getFriendsAsync() throws InvalidSocialRequestException {
		mAsyncRunner.request("me/friends", new Bundle(), new FriendListRequestListener());
		mFacebookFriends = new ArrayList<SocialFriend>();
		updateRunning = true;
		try {
			synchronized (mFacebookFriends) {
				while (updateRunning) {
					mFacebookFriends.wait();
				}
				
				// once here, the mFacebookFriends object has been updated, so this method
				// can terminate
			}
		}
		catch(InterruptedException e) {
			throw new InvalidSocialRequestException("The friends list could not be retrieved", e);
		}
	}

	@Override
	public ArrayList<String> getFriendsUsingCorso12() {
		//TODO metodo messi a disposizione da corso12, si spera
		return null;
	}

	/**
	 * This method is used to let the listeners' implementations
	 * post their result message: the 'actionResult' field will be
	 * returned by the proper methods.
	 * @param r
	 */
	public void setActionResult(String r) {
		actionResult = r;
	}

	/**
	 * @return the mFacebook
	 */
	public Facebook getmFacebook() {
		return mFacebook;
	}

	///
	///	LISTENERS >IMPLEMENTATIONS< CLASSES, PRIVATE ACCESS
	///

	public class AuthDialogListener extends TheFacebookBaseDialogListener {
		@Override
		public void onComplete(Bundle values) {
			Log.i(tag, "login performed");

			connectionData = new HashMap<String, String>();
			connectionData.put(appIDKey, appID);
			connectionData.put(accessTokenKey, mFacebook.getAccessToken());
			connectionData.put(accessExpiresKey, String.valueOf(mFacebook.getAccessExpires()));

			// the valid session is saved in the app prefs
			SocialSessionStore.save(SocialWrapper.FACEBOOK, iAmTheFacebook, context);
			actionResult = SocialNetwork.ACTION_SUCCESSFUL;
		}
	}

	public class FriendListRequestListener extends TheFacebookBaseRequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {
				synchronized (mFacebookFriends) {
					// now the answer gets parsed (according to Facebook's APIs)
					JSONObject json;
					json = Util.parseJson(response);
					final JSONArray friends = json.getJSONArray("data");
					
					for (int i=0; i<friends.length(); i++) {
						JSONObject j = friends.getJSONObject(i);
						
						String id = j.getString("id");
						String name = j.getString("name");
						String img = "http://graph.facebook.com/"+id+"/picture?type=small"; // <-- image size!
						
						mFacebookFriends.add(new SocialFriend(id, name, img));
					}
					
					// the getFriendsAsync() method is still waiting, better
					// to wake it up :P
					updateRunning = false;
					mFacebookFriends.notifyAll();
				}	
			}
			catch (JSONException e) {
				Log.e(tag, "JSON error", e);
			} catch (FacebookError e) {
				Log.e(tag, "Facebook error", e);
			}
		}			
	}

	private class PostOnWallDialogListener extends TheFacebookBaseDialogListener {

		@Override
		public void onComplete(Bundle values) {
			actionResult = ACTION_SUCCESSFUL;
			Log.d(tag, actionResult);
		}
	}

	///
	///	LISTENERS >ABSTRACT< CLASSES, PRIVATE ACCESS
	///

	private abstract class TheFacebookBaseDialogListener implements DialogListener {

		public void onFacebookError(FacebookError e) {
			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
		}

		public void onError(DialogError e) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.GENERAL_ERROR, e);   
		}

		public void onCancel() {
			TheFacebook.this.setActionResult(SocialNetwork.ACTION_CANCELED);
			Log.d(SocialNetwork.tag, SocialNetwork.ACTION_CANCELED);
		}
	}

	private abstract class TheFacebookBaseRequestListener implements RequestListener {

		public void onFacebookError(FacebookError e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
		}

		public void onFileNotFoundException(FileNotFoundException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.GENERAL_ERROR, e);
		}

		public void onIOException(IOException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.GENERAL_ERROR, e);
		}

		public void onMalformedURLException(MalformedURLException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR);
			Log.d(SocialNetwork.tag, SocialNetwork.GENERAL_ERROR, e);
		}
	}
}