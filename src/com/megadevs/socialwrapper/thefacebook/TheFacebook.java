package com.megadevs.socialwrapper.thefacebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;

public class TheFacebook extends SocialNetwork {
	
	private TheFacebook iAmTheFacebook;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

	private ArrayList<SocialFriend> mFacebookFriends;
	
	private Activity mActivity;
	private final String appID = "210912465637288";
	
	private final String appIDKey = "app_id";
	private final String accessTokenKey = "access_token";

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
		
		connected = false;
		connectionData = new HashMap<String, String>();
		connectionData.put(appIDKey, appID);
		
		mFacebook = new Facebook(connectionData.get(appIDKey));
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

		SocialNetwork.logTag = "Corso12 - Social - Facebook";
		
		// restoring an eventual saved session
		SocialSessionStore.restore(SocialWrapper.FACEBOOK, context);
	}

	@Override
	public void authenticate() {
		this.mFacebook.authorize(mActivity,
				new String[] {"publish_stream", "read_stream", "offline_access"}, 
				new AuthDialogListener(this));
	}

	@Override
	public String selfPost(String msg) {
        Bundle parameters = new Bundle();
        mFacebook.dialog(mActivity,
        		"stream.publish",
        		parameters,
        		new PostOnWallDialogListener(this));
        return actionResult;
	}

	@Override
	public Vector<String[]> getConnectionData() {
		Vector<String[]> data = new Vector<String[]>();
		data.add(new String[] {appIDKey, accessTokenKey});
		data.add(new String[] {accessTokenKey, mFacebook.getAccessToken()});
		return data;
	}
	
	@Override
	public String postToFriend(String friendID, String msg) {
        Bundle parameters = new Bundle();
        parameters.putString("to", friendID);
        mFacebook.dialog(mActivity,
        		"stream.publish",
        		parameters,
        		new PostOnWallDialogListener(this));
        return actionResult;
	}
	
	@Override
	public ArrayList<SocialFriend> getFriendsList() {
		mAsyncRunner.request("me/friends", new Bundle(), new FriendListRequestListener(this));
		return mFacebookFriends;
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

	
	
	///
	///	LISTENERS CLASSES, PRIVATE ACCESS
	///
	
	private class AuthDialogListener extends TheFacebookBaseDialogListener {
		public AuthDialogListener(TheFacebook f) {super(f);}

		@Override
		public void onComplete(Bundle values) {
			Log.i(logTag, "login performed");
			SocialSessionStore.save(SocialWrapper.FACEBOOK, iAmTheFacebook, context);
		}
	}

	public class FriendListRequestListener extends TheFacebookBaseRequestListener {

		public FriendListRequestListener(TheFacebook f) {super(f);}

		@Override
		public void onComplete(String response, Object state) {
	        
			JSONObject json;
			try {
				json = Util.parseJson(response);
				final JSONArray friends = json.getJSONArray("data");
				mFacebookFriends = new ArrayList<SocialFriend>(friends.length());
				
				for (int i=0; i<friends.length(); i++) {
					JSONObject j = friends.getJSONObject(i);
					String id = j.getString("id");
					String name = j.getString("name");
					String img = "http://graph.facebook.com/"+id+"/picture?type=small"; // <-- image size!
					mFacebookFriends.add(new SocialFriend(id, name, img));
				}
			} catch (JSONException e) {
				Log.e(logTag, "JSON error", e);
			} catch (FacebookError e) {
				Log.e(logTag, "Facebook error", e);
			}
		}
	}

	private class PostOnWallDialogListener extends TheFacebookBaseDialogListener {
		
		public PostOnWallDialogListener(TheFacebook f) {super(f);}

		@Override
		public void onComplete(Bundle values) {
			actionResult = ACTION_SUCCESSFUL;
			Log.d(logTag, actionResult);
		}
	}
}
