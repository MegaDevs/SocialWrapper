package com.megadevs.socialwrapper.thetwitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import twitter4j.IDs;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;
import com.megadevs.socialwrapper.exceptions.InvalidAuthenticationException;
import com.megadevs.socialwrapper.exceptions.InvalidSocialRequestException;

public class TheTwitter extends SocialNetwork {

	private String consumerKey = "";
	private String consumerSecret = "";

	private String callbackURL = "T4JOAuth://main";

	private final String accessTokenKey = "accessTokenKey";
	private final String accessTokenSecretKey = "accessTokenSecretKey";

	private static TheTwitter myTwitter = null;

	private static Twitter twitter;
	private static TwitterFactory twitterFactory;
	private AccessToken accessToken;

	private Activity mActivity;

	// static callback refs
	private TheTwitterLoginCallback loginCallback;
	private TheTwitterPostCallback postCallback;
	private TheTwitterFriendListCallback friendslistCallback;

	private Handler theHandler = new Handler();	

	/**
	 * You can set the consumerSecret and consumerKey later (set them equal "")
	 * 
	 * @param id
	 * @param activity
	 * @param secret
	 * @param key
	 */
	public TheTwitter(String id, Activity activity) {
		myTwitter = this;
		mActivity = activity;
		tag = "Corso12-Social-Twitter";
	}

	public void setParameters(String key, String secret, String callback) {
		consumerKey = key;
		consumerSecret = secret;
		callbackURL = callback;
		
		if(secret != "" && key != "")
			setConsumerSecretAndKey(secret, key);

	}
	
	/**
	 * Set the consumerSecret and consumerKey and init the twitterFactory
	 * 
	 * @param secret
	 * @param key
	 */
	public void setConsumerSecretAndKey(String secret, String key) {
		consumerSecret = secret;
		consumerKey = key;
		SocialSessionStore.restore(SocialWrapper.TWITTER, this, mActivity);

		accessToken = getAccessTokenInternal();

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		System.out.println("Before the twitterFactory's creation");
		System.out.println(consumerKey + "    "  + consumerSecret);
		Configuration configuration = configurationBuilder.build();
		twitterFactory = new TwitterFactory(configuration);
		if(accessToken != null) {
			twitter = twitterFactory.getInstance(accessToken);
		}
	}

	/**
	 * If available, retrieves the accessToken, otherwise return null
	 * 
	 * @return
	 */
	private AccessToken getAccessTokenInternal() {
		if (connectionData == null)
			connectionData = new HashMap<String, String>();
		String key = connectionData.get(accessTokenKey);
		String secret = connectionData.get(accessTokenSecretKey);
		if(key == null && secret == null) {
			Log.i(tag, "AccessToken null");
			return null;
		}
		Log.i(tag, "AccessToken valido, lo ritorno");
		return new AccessToken(key, secret);
	} 

	public static TheTwitter getTwitter() {
		return myTwitter;
	}

	private void setPropers() {
		connectionData.put(accessTokenKey, accessToken.getToken());
		connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
		SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
	}

	public void setPropersAccessToken(AccessToken accessTokenTemp) {
		accessToken = accessTokenTemp;
		setPropers();
		twitter = twitterFactory.getInstance(accessToken);
		try {
			System.out.println("verifyCredentials     " + twitter.verifyCredentials().getId());
		} catch (TwitterException e) {
			loginCallback.onLoginCallback(SocialNetwork.SOCIAL_NETWORK_ERROR);
			e.printStackTrace();
		}
		loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
	}

	public static void deletePropers() {
		//Log.i(tag, "elimino l'istanza di twitter che ho istanziato non autenticata");
		twitter = null;
	}

	private void removeAccessToken() {
		connectionData.remove(accessTokenKey);
		connectionData.remove(accessTokenSecretKey);
		accessToken = null;
		SocialSessionStore.clear(SocialWrapper.TWITTER, mActivity);
	}

	/**
	 * Check the accessToken status (null or set)
	 * @return
	 */
	private Boolean checkIstanceTwitter() {
		accessToken = getAccessTokenInternal();
		if(accessToken == null)
			return false;
		else 
			return true;
	}

	private void OAuthLogin() throws InvalidAuthenticationException {
		try {				
			accessToken = getAccessTokenInternal();

			if (accessToken != null) {
				Log.i(tag, "accessToken già salvato, istanzio twitter");
				twitter = twitterFactory.getInstance(accessToken);
				Log.i(tag, "Effettuo il verifyCredentials");
				System.out.println("verifyCredentials     " + twitter.verifyCredentials().getId());
				loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
			} else {
				Log.i(tag, "Effettuo il logIn via WebView");
				twitter = twitterFactory.getInstance();
				System.out.println("ciao ciao ciao ciao ciao ciao");
				System.out.println("CALLBACKURL     " + callbackURL);
				if(twitter == null)
					System.out.println("nnnnuuuuuuuuuuuuuulllllllllllllll");
				else
					System.out.println("sdsovsnvasnsavdsadsdonvpsaduivpasvkpasnciasdvpkasdvisndvkjpsdvpisnvpjasdvasdvpizasvin");
				RequestToken requestToken = twitter.getOAuthRequestToken(); // (CALLBACKURL);
				System.out.println("ehiehiehiehiehiehiehehieiheiheih");
				Intent i = new Intent(mActivity.getApplicationContext(), TheTwitterWebView.class);
				Bundle b = new Bundle();
				b.putString("url", requestToken.getAuthenticationURL());
				b.putSerializable("twitter", twitter);
				b.putSerializable("requestToken", requestToken);
				i.putExtras(b);
				mActivity.startActivity(i);
			}
		} catch (TwitterException ex) {
			throw new InvalidAuthenticationException("Authentication could not be performed", ex);
		}
	}

	@Override
	public void authenticate(SocialBaseCallback r) throws InvalidAuthenticationException {
		System.out.println("Ciao amicoooooooooooooooooooooooooooo");
		loginCallback = (TheTwitterLoginCallback)r;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					OAuthLogin();
				} catch (InvalidAuthenticationException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void deauthenticate() {
		deletePropers();
		removeAccessToken();
	}

	public void selfPost(final String msg, SocialBaseCallback s) throws InvalidAuthenticationException {

		postCallback = (TheTwitterPostCallback) s;

		new Thread(new Runnable() {

			@Override
			public void run() {
				Boolean callBack = false;
				try {

					if(twitter == null)
						if(checkIstanceTwitter()) 
							twitter = twitterFactory.getInstance(accessToken);
						else {
							removeAccessToken();
							postCallback.onPostCallback(SOCIAL_NETWORK_ERROR);
							throw new InvalidAuthenticationException("Tweet could not be performed, try to reauthenticate", null);
						}
					try {
						System.out.println(twitter.verifyCredentials().getId());
					} catch (TwitterException e) {
						removeAccessToken();
						Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR);
						try {
							throw new InvalidAuthenticationException("Tweet could not be performed, try to reauthenticate", null);
						} catch (InvalidAuthenticationException e1) {
							e1.printStackTrace();
						}
					}

					try {
						twitter.updateStatus(msg + new Random());
					} catch (TwitterException e) {
						Toast.makeText(mActivity, "Check your tweet!! You can't post twice the same message!!", 1000);
						postCallback.onErrorCallback(GENERAL_ERROR);
						callBack = true;
						e.printStackTrace();
					}
					if(callBack == false)
						postCallback.onPostCallback(ACTION_SUCCESSFUL);
				} catch (InvalidAuthenticationException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void postToFriend(final String friendID, final String msg, SocialBaseCallback s) throws InvalidSocialRequestException {
		postCallback = (TheTwitterPostCallback) s;

		new Thread(new Runnable() {

			@Override
			public void run() {
				Boolean callBack = false;

				try {

					if(twitter == null)
						if(checkIstanceTwitter()) 
							twitter = twitterFactory.getInstance(accessToken);
						else {
							removeAccessToken();
							postCallback.onPostCallback(SOCIAL_NETWORK_ERROR);
							Toast.makeText(mActivity, "Check your tweet!! You can't post twice the same message!!", 1000);
							throw new InvalidSocialRequestException("Tweet could not be performed, try to reauthenticate", null);
						}

					try {
						System.out.println(twitter.verifyCredentials().getId());
					} catch (TwitterException e) {
						removeAccessToken();
						Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR);
						try {
							throw new InvalidSocialRequestException("Tweet could not be performed, try to reauthenticate", e);
						} catch (InvalidSocialRequestException e1) {
							e1.printStackTrace();
						}
					}

					try {
						twitter.updateStatus("@" + friendID + " " + msg + new Random());
					} catch (TwitterException e) {
						removeAccessToken();
						Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						callBack = true;
						postCallback.onErrorCallback(GENERAL_ERROR);
					}
					// check the status
					if(callBack == false)
						postCallback.onPostCallback(ACTION_SUCCESSFUL);	
				} catch (InvalidSocialRequestException e) {
					e.printStackTrace();
				}
			}
		}).start();

		System.out.println("Ehi man!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	@Override
	public void getFriendsList(SocialBaseCallback s) throws InvalidSocialRequestException {

		friendslistCallback = (TheTwitterFriendListCallback) s;

		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<SocialFriend> friendList = new ArrayList<SocialFriend>();

				try {

					if(twitter == null) {
						System.out.println("twitter == null");
						if(checkIstanceTwitter())
							twitter = twitterFactory.getInstance(accessToken);
						else {
							System.out.println("removeAccessToken()");
							removeAccessToken();
							friendslistCallback.onErrorCallback(SOCIAL_NETWORK_ERROR);
							throw new InvalidSocialRequestException("Tweet could not be performed, try to reauthenticate", null);
						}
					}
					long cursor = -1;
					IDs ids;
					try {
						do {
							ids = twitter.getFollowersIDs(cursor);
							for (long id : ids.getIDs()) {
								friendList.add(
										new SocialFriend(
												Long.toString(twitter.showUser(id).getId()),
												twitter.showUser(id).getName(),
												twitter.showUser(id).getProfileImageURL().toString()));
							}
						} while ((cursor = ids.getNextCursor()) != 0);
						friendslistCallback.onFriendsListCallback(ACTION_SUCCESSFUL, friendList);
					} catch (TwitterException e) {
						removeAccessToken();
						Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						friendslistCallback.onErrorCallback(SOCIAL_NETWORK_ERROR);
						try {
							throw new InvalidSocialRequestException("Could not retrive the friends list, try to reauthenticate", e);
						} catch (InvalidSocialRequestException e1) {
							e1.printStackTrace();
						}
					}
				} catch (InvalidSocialRequestException e) {
					e.printStackTrace();
				}
			}
		}).start();
		System.out.println("Ehi man!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	///
	///	LISTENERS >IMPLEMENTATIONS< CLASSES, MAY HAVE PRIVATE ACCESS
	///

	public static abstract class TheTwitterLoginCallback implements SocialBaseCallback {
		public abstract void onLoginCallback(String result);
		public void onPostCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error); 
	}

	public static abstract class TheTwitterPostCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public abstract void onPostCallback(String result);
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error);
	}

	public static abstract class TheTwitterFriendListCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onPostCallback(String result) {};
		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
		public abstract void onErrorCallback(String error);
	}

	@Override
	public Vector<String[]> getConnectionData() {
		Vector<String[]> connList = new Vector<String[]>();
		connList.add(new String[] {accessTokenKey, connectionData.get(accessTokenKey)});
		connList.add(new String[] {accessTokenSecretKey, connectionData.get(accessTokenSecretKey)});
		return connList;
	}

	@Override
	protected void setConnectionData(Map<String, String> connectionData) {
		this.connectionData = connectionData;
	}

	@Override
	public String getAccessToken() {
		if (accessToken != null)
			return accessToken.getToken()+';'+accessToken.getTokenSecret();

		return null;
	}

	@Override
	public boolean isAuthenticated() {
		if(accessToken != null) 
			if (!accessToken.getToken().equals("") && (!accessToken.getTokenSecret().equals("")))
				return true;
		return false;
	}


/*
 * ===== OLD BUT USEFULL ====
 * 
if(twitter == null) {
	Log.i(tag, "twitter non autenticato. twitter == null");
	if(accessToken == null) {
		Log.i(tag, "Non hai l'accessToken");
		return twitter = null;
	} else {
		Log.i(tag, "Mi autentico con l'accessToken");
		connectionData.put(accessTokenKey, accessToken.getToken());
		connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
		SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
		return twitter = twitterFactory.getInstance(accessToken);
	}
} else {
	setPropers();
	Log.i(tag, "twitter già autenticato");
	return twitter;	
}
 */
	
	@Override
	public String getId() {
		return this.id;
	}
}