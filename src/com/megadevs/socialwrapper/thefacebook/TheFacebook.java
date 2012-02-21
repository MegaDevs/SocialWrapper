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
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;

/**
 * This class models a personal Facebook object. With an instance of 
 * TheFacebook it is possible to authenticate, post messages and retrieve
 * various informations.
 * @author dextor
 *
 */
public class TheFacebook extends SocialNetwork {

	private static TheFacebook iAmTheFacebook;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

	private ArrayList<SocialFriend> mFacebookFriends;

	private String appID;

	// keys for data storing
	private final String appIDKey = "app_id";
	private final String accessTokenKey = "access_token";
	private final String accessExpiresKey = "access_token_expires";

	// statics for image size
	public static final String FACEBOOK_PHOTO_SMALL 	= "small";
	public static final String FACEBOOK_PHOTO_MEDIUM 	= "medium";
	public static final String FACEBOOK_PHOTO_BIG 		= "big";
	
	private String facebookPhotoSize;
	
	// static callback refs
	private TheFacebookLoginCallback loginCallback;
	private TheFacebookPostCallback postCallback;
	private TheFacebookFriendListCallback friendslistCallback;
	private TheFacebookPictureCallback pictureCallback;

	/**
	 * Default constructor for TheFacebook class. A context is
	 * required in order to perform authentication and display
	 * Facebook dialogs (if the official app is installed).
	 * @param context
	 */
	public TheFacebook(String id, Activity a) {
		this.id = id;
		this.mActivity = a;
		iAmTheFacebook = this;

		tag = "[SW-THEFACEBOOK]";
	}

	/**
	 * This method is used within the callback procedure to
	 * set the authenticated Facebook object.
	 * @return the existing instance of TheFacebook
	 */
	public static TheFacebook getInstance() {
		return iAmTheFacebook;
	}

	/**
	 * This method is called from outside the wrapper and it is used to
	 * set the application ID; the instanciation of the other objects 
	 * (mFacebook and mAsyncRunner) can be done only after this action 
	 * is completed.
	 * @param id the application id provided by Facebook
	 */
	public void init(String id) {
		appID = id;
		mFacebook = new Facebook(appID);

		System.out.println(mActivity == null);
		
		// try to restore a previously saved session
		SocialSessionStore.restore(this.id, this, mActivity);

		// must be done after the restore, otherwise it would be created
		// on a Facebook object which has no (potentially) valid access token
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
	}

	@Override
	public Vector<String[]> getConnectionData() {
		Vector<String[]> data = new Vector<String[]>();
		data.add(new String[] {appIDKey, appID});
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
			String accessToken = connectionData.get(accessTokenKey);
			Long accessExpires = (Long.valueOf(connectionData.get(accessExpiresKey)).longValue());

			// restoring data
			mFacebook.setAccessToken(accessToken);
			mFacebook.setAccessExpires(accessExpires);
		}
	}

	@Override
	public void authenticate(SocialBaseCallback r) {
		// check if a valid session is already available, otherwise perform a full login
		loginCallback = (TheFacebookLoginCallback)r;
		if (mFacebook.isSessionValid()) {
			Log.i(tag, "session valid, use it wisely ;)");
			loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
		}
		else {
			Log.i(tag, "valid session: " + mFacebook.isSessionValid());
			Intent i = new Intent(mActivity, TheFacebookActivity.class);
			mActivity.startActivity(i);
		}
	}

	@Override
	public void deauthenticate() {
		mAsyncRunner.logout(mActivity, new LogoutListener());
		SocialSessionStore.clear(id, mActivity);
		
	}

	/**
	 * This method is used to post on the user's wall. There is no message, since a dialog
	 * will appear and let the user insert a custom message to post.
	 * @return a string containing the result of the operation
	 * @throws InvalidSocialRequestException 
	 */
	public void postOnMyWall(String message, String link, String name, String caption, String description, SocialBaseCallback s) {
		actionResult = null;
		postCallback = (TheFacebookPostCallback) s;

		Bundle params = new Bundle();
		
	    params.putString("message", message);
	    params.putString("link", link);
	    params.putString("name", name);
	    params.putString("caption", caption);
	    params.putString("description", description);
		
		mAsyncRunner.request("me/feed", params, "POST", new PostOnWallDialogListener(), null);
	}

	/**
	 * This method is used to post on a friend's wall.
	 * @param friendID the friend's ID provided by Facebook
	 * @return a string containing the result of the operation
	 * @throws InvalidSocialRequestException
	 */
	public void postToFriendsWall(String friendID, String message, String link, String name, String caption, String description, SocialBaseCallback s) throws InvalidSocialRequestException {
		actionResult = null;
		postCallback = (TheFacebookPostCallback) s;

		Bundle params = new Bundle();
		
		params.putString("message", message);
	    params.putString("link", link);
	    params.putString("name", name);
	    params.putString("caption", caption);
	    params.putString("description", description);
		
		mAsyncRunner.request(friendID+"/feed", params, "POST", new PostOnWallDialogListener(), null);
	}

	@Override
	public void getFriendsList(SocialBaseCallback s) {
		actionResult = null;
		
		friendslistCallback = (TheFacebookFriendListCallback) s;
		mFacebookFriends = new ArrayList<SocialFriend>();
		mAsyncRunner.request("me/friends", new Bundle(), new FriendListRequestListener());
	}


	public void postPicture(byte[] image, String description, SocialBaseCallback s) {
		actionResult = null;
		pictureCallback = (TheFacebookPictureCallback) s;
		
		if (image.length != 0) {
			Bundle params = new Bundle();
			params.putString("name", description);
			params.putByteArray("source", image);
			
			mAsyncRunner.request("me/photos", params, "POST", new PostPictureListener(), null);
		}
	}
	
	/**
	 * This method is used to let the listeners' implementations
	 * post their result message: the 'actionResult' field will be
	 * returned by the proper methods.
	 * @param r
	 */
	public void setActionResult(String r, Exception e) {
		actionResult = r;
		actionException = e;
	}

	/**
	 * @return the mFacebook
	 */
	public Facebook getmFacebook() {
		return mFacebook;
	}


	@Override
	public String getAccessToken() {
		if (mFacebook != null)
			return mFacebook.getAccessToken();
		else return null;
	}

	@Override
	public boolean isAuthenticated() {
		if (mFacebook != null)
			return mFacebook.isSessionValid();
		else return false;
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	/**
	 * Sets the profile picture size for each friend returned from the
	 * 'getFriendsList' method.
	 * @param size
	 */
	public void setFacebookPhotoSize(String size) {
		facebookPhotoSize = size;
	}
	
	///
	///	LISTENERS >IMPLEMENTATIONS< CLASSES, MAY HAVE PRIVATE ACCESS
	///

	public static abstract class TheFacebookLoginCallback implements SocialBaseCallback {
		public abstract void onLoginCallback(String result);
		public void onPostCallback(String result) {};
		public void onPostPictureCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e); 
	}

	public static abstract class TheFacebookPostCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public abstract void onPostCallback(String result);
		public void onPostPictureCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e);
	}

	public static abstract class TheFacebookFriendListCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onPostCallback(String result) {};
		public void onPostPictureCallback(String result) {};
		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
		public abstract void onErrorCallback(String error, Exception e);
	}
	
	public static abstract class TheFacebookPictureCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onPostCallback(String result) {};
		public abstract void onPostPictureCallback(String result);
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e);
	}
	
	// needs to have package visibility, otherwise TheFacebookActivity cannot perform the login process
	class AuthDialogListener extends TheFacebookBaseDialogListener {

		@Override
		public void onComplete(Bundle values) {
			Log.i(tag, "login performed");

			connectionData = new HashMap<String, String>();
			connectionData.put(appIDKey, appID);
			connectionData.put(accessTokenKey, mFacebook.getAccessToken());
			connectionData.put(accessExpiresKey, String.valueOf(mFacebook.getAccessExpires()));

			// the valid session is saved in the app prefs
			System.out.println(SocialSessionStore.save(TheFacebook.this.id, TheFacebook.this, mActivity));
			actionResult = SocialNetwork.ACTION_SUCCESSFUL;

			if (loginCallback != null) {
				loginCallback.onLoginCallback(actionResult);
				loginCallback = null;
			}
		}
	}

	private class FriendListRequestListener extends TheFacebookBaseRequestListener {

		@Override
		public void onComplete(String response, Object state) {
			// now the answer gets parsed (according to Facebook's APIs)
			JSONObject json;
			try {
				json = Util.parseJson(response);
				
				final JSONArray friends = json.getJSONArray("data");

				for (int i=0; i<friends.length(); i++) {
					JSONObject j = friends.getJSONObject(i);

					String id = j.getString("id");
					String name = j.getString("name");
					String img = "http://graph.facebook.com/"+id+"/picture?type="+facebookPhotoSize;

					mFacebookFriends.add(new SocialFriend(id, name, img));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (FacebookError e) {
				e.printStackTrace();
			}

			actionResult = ACTION_SUCCESSFUL;
			if (friendslistCallback != null) {
				friendslistCallback.onFriendsListCallback(actionResult, mFacebookFriends);
				friendslistCallback = null;
			}
		}	
	}

	private class PostOnWallDialogListener extends TheFacebookBaseRequestListener {

		@Override
		public void onComplete(String response, Object state) {
			actionResult = ACTION_SUCCESSFUL;

			if (postCallback != null) {
				postCallback.onPostCallback(actionResult);
				postCallback = null;
			}
		}
	}

	
	private class PostPictureListener extends TheFacebookBaseRequestListener {

		@Override
		public void onComplete(String response, Object state) {
			System.out.println("response="+response);
			System.out.println("state="+state);
			actionResult = ACTION_SUCCESSFUL;

			if (pictureCallback != null) {
				pictureCallback.onPostPictureCallback(actionResult);
				pictureCallback = null;
			}
		}

	}
	
	///
	///	LISTENERS >ABSTRACT< CLASSES, PRIVATE ACCESS
	///

	private abstract class TheFacebookBaseDialogListener implements DialogListener {

		private void forwardErrorResult(Exception e) {
			if (loginCallback != null)
				loginCallback.onErrorCallback(actionResult, e);
			else if (postCallback != null)
				postCallback.onErrorCallback(actionResult, e);
			else if (friendslistCallback != null)
				friendslistCallback.onErrorCallback(actionResult, e);
			else if (pictureCallback != null)
				pictureCallback.onErrorCallback(actionResult, e);
		}

		public void onFacebookError(FacebookError e) {
			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR,null);
			Log.d(tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
			forwardErrorResult(null);
		}

		public void onError(DialogError e) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,null);
			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);   
			forwardErrorResult(null);
		}

		public void onCancel() {
			TheFacebook.this.setActionResult(SocialNetwork.ACTION_CANCELED,null);
			Log.d(tag, SocialNetwork.ACTION_CANCELED);
			forwardErrorResult(null);
		}
	}

	private abstract class TheFacebookBaseRequestListener implements RequestListener {

		private void forwardErrorResult(Exception e) {
			if (loginCallback != null)
				loginCallback.onErrorCallback(actionResult, e);
			else if (postCallback != null)
				postCallback.onErrorCallback(actionResult, e);
			else if (friendslistCallback != null)
				friendslistCallback.onErrorCallback(actionResult, e);
			else if (pictureCallback != null)
				pictureCallback.onErrorCallback(actionResult, e);
		}

		public void onFacebookError(FacebookError e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR,null);
			Log.d(tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
			forwardErrorResult(null);
		}

		public void onFileNotFoundException(FileNotFoundException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
			forwardErrorResult(e);
		}

		public void onIOException(IOException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
			forwardErrorResult(e);
		}

		public void onMalformedURLException(MalformedURLException e, final Object state) {
			TheFacebook.this.setActionResult(SocialNetwork.GENERAL_ERROR,e);
			Log.d(tag, SocialNetwork.GENERAL_ERROR, e);
			forwardErrorResult(e);
		}
	}
	
    public class LogoutListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			System.out.println("you have now logged out");
		}

		@Override
		public void onIOException(IOException e, Object state) {
			e.printStackTrace();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			e.printStackTrace();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			e.printStackTrace();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			e.printStackTrace();
		}
    }

}