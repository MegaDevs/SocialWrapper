package com.megadevs.socialwrapper.thetwitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

	/**
	 * You can set the consumerSecret and consumerKey later (set them equal "")
	 * 
	 * @param id
	 * @param a
	 * @param secret
	 * @param key
	 */
	public TheTwitter(String id, Activity a) {
		this.id = id;
		this.mActivity = a;
		myTwitter = this;

		tag = "[SW-THETWITTER]";
	}

	public void setParameters(String key, String secret, String callback) {
		System.out.println("setParameters public");
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
		System.out.println("setConsumerSecretAndKey");
		consumerSecret = secret;
		consumerKey = key;
		SocialSessionStore.restore(SocialWrapper.THETWITTER, this, mActivity);

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
		System.out.println("getAccessTokenInternal");
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
		System.out.println("getTwitter");
		return myTwitter;
	}

	private void setPropers() {
		System.out.println("setPropers");
		connectionData.put(accessTokenKey, accessToken.getToken());
		connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
		SocialSessionStore.save(SocialWrapper.THETWITTER, this, mActivity);
	}

	public void setPropersAccessToken(AccessToken accessTokenTemp) {
		System.out.println("setPropersAccessToken");
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
		System.out.println("deletePropers");
		//Log.i(tag, "elimino l'istanza di twitter che ho istanziato non autenticata");
		twitter = null;
	}

	private void removeAccessToken() {
		System.out.println("removeAccessToken");
		connectionData.remove(accessTokenKey);
		connectionData.remove(accessTokenSecretKey);
		accessToken = null;
		SocialSessionStore.clear(SocialWrapper.THETWITTER, mActivity);
	}

	/**
	 * Check the accessToken status (null or set)
	 * @return
	 */
	private Boolean checkIstanceTwitter() {
		System.out.println("checkIstanceTwitter");
		accessToken = getAccessTokenInternal();
		if(accessToken == null)
			return false;
		else 
			return true;
	}

	private void OAuthLogin() throws InvalidAuthenticationException {
		System.out.println("OAuthLogin");
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
				RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL); // (CALLBACKURL);
				System.out.println(" =====> " + requestToken.getAuthenticationURL());
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
	public void authenticate(SocialBaseCallback r) {
		System.out.println("authenticate");
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
		System.out.println("deauthenticate");
		deletePropers();
		removeAccessToken();
	}

	public void selfPost(final String msg, SocialBaseCallback s) throws InvalidAuthenticationException {
		System.out.println("selfPost");

		postCallback = (TheTwitterPostCallback) s;

		new Thread(new Runnable() {

			@Override
			public void run() {
				Boolean callBack = false;
				try {
					System.out.println("0");
					try {
						if(twitter.verifyCredentials() == null) {
							System.out.println(" user nullo ritornato dalla verifica delle credenzionali");
							System.out.println("1");
							
						}
					} catch (TwitterException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					if(twitter == null)
						if(checkIstanceTwitter()) { 
							twitter = twitterFactory.getInstance(accessToken);
							System.out.println("2");
							
						}
						else {
							removeAccessToken();
							System.out.println("3");							
							postCallback.onPostCallback(SOCIAL_NETWORK_ERROR);
							throw new InvalidAuthenticationException("Tweet could not be performed, try to reauthenticate", null);
						}
					try {
						System.out.println(twitter.verifyCredentials().getId());
						System.out.println("4");						
					} catch (TwitterException e) {
						System.out.println("4.1");
						removeAccessToken();
						System.out.println("5");
						
						//Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
						try {
							throw new InvalidAuthenticationException("Tweet could not be performed, try to reauthenticate", null);
						} catch (InvalidAuthenticationException e1) {
							e1.printStackTrace();
						}
					}

					try {
						twitter.updateStatus(msg);
						System.out.println("6");						
					} catch (TwitterException e) {
						Toast.makeText(mActivity, "Check your tweet!! You can't post twice the same message!!", 1000);
						postCallback.onErrorCallback(GENERAL_ERROR, e);
						System.out.println("7");		
						callBack = true;
						e.printStackTrace();
					}
					if(callBack == false) {
						System.out.println("8");	
						postCallback.onPostCallback(ACTION_SUCCESSFUL);
					}
				} catch (InvalidAuthenticationException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void postToFriend(final String friendID, final String msg, SocialBaseCallback s) throws InvalidSocialRequestException {
		System.out.println("postToFriend");
		postCallback = (TheTwitterPostCallback) s;
System.out.println("1");
		new Thread(new Runnable() {

			@Override
			public void run() {
				Boolean callBack = false;
				System.out.println("2");
					if(twitter == null)
						if(checkIstanceTwitter())  {
							twitter = twitterFactory.getInstance(accessToken);
							System.out.println("3");
						}
						else { 
							System.out.println("remove1");
							removeAccessToken();
							postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, null);
							//Toast.makeText(mActivity, "Check your tweet!! You can't post twice the same message!!", 1000);
						}

					try {
						System.out.println(twitter.verifyCredentials().getId());
						System.out.println("4");
					} catch (TwitterException e) {
						System.out.println("remove2");
						removeAccessToken();
						System.out.println("5");
						//Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, e);
					}

					/*
					 * 401:Authentication credentials 
					 * (https://dev.twitter.com/docs/auth) were missing or incorrect.
					 * Ensure that you have set valid conumer key/secret, access token/secret, and the system clock in in sync.

					 */
					
					try {
						System.out.println("7");
						System.out.println("@" + friendID + " " + msg);
						twitter.updateStatus("@" + friendID + " " + msg);
					} catch (TwitterException e) {
						System.out.println(e);
						System.out.println("remove3");
						removeAccessToken();
						//Toast.makeText(mActivity, "Tweet could not be performed, try to reauthenticate", 1000);
						callBack = true;
						postCallback.onErrorCallback(GENERAL_ERROR, e);
					}
					// check the status
					if(callBack == false)
						postCallback.onPostCallback(ACTION_SUCCESSFUL);	
			}
		}).start();
	}

	@Override
	public void getFriendsList(SocialBaseCallback s) {
		System.out.println("getFriendsList");

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
							friendslistCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, null);
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
						friendslistCallback.onErrorCallback(SOCIAL_NETWORK_ERROR, null);
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
	}

	///
	///	LISTENERS >IMPLEMENTATIONS< CLASSES, MAY HAVE PRIVATE ACCESS
	///

	public static abstract class TheTwitterLoginCallback implements SocialBaseCallback {
		public abstract void onLoginCallback(String result);
		public void onPostCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e); 
	}

	public static abstract class TheTwitterPostCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public abstract void onPostCallback(String result);
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error, Exception e);
	}

	public static abstract class TheTwitterFriendListCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onPostCallback(String result) {};
		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
		public abstract void onErrorCallback(String error, Exception e);
	}

	@Override
	public Vector<String[]> getConnectionData() {
		System.out.println("getConnectionData");
		Vector<String[]> connList = new Vector<String[]>();
		connList.add(new String[] {accessTokenKey, connectionData.get(accessTokenKey)});
		connList.add(new String[] {accessTokenSecretKey, connectionData.get(accessTokenSecretKey)});
		return connList;
	}

	@Override
	protected void setConnectionData(Map<String, String> connectionData) {
		System.out.println("setConnectionData");
		this.connectionData = connectionData;
	}

	@Override
	public String getAccessToken() {
		System.out.println("getAccessToken");
		if (accessToken != null)
			return accessToken.getToken()+';'+accessToken.getTokenSecret();

		return null;
	}

	@Override
	public boolean isAuthenticated() {
		System.out.println("isAuthenticated");
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