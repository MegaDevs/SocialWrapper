package com.megadevs.socialwrapper.thetwitter;

import java.util.ArrayList;
import java.util.Random;
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

import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;

public class TheTwitter extends SocialNetwork {

	public final static String consumerKey = "lfG3DKCO5sXrpuBQx2FQ";
	public final static String consumerSecret = "z7FJ2HDuVxxd3yIXmFk6k5Vt8C4yVdnCXlW9GUMDF4";
	private final String CALLBACKURL = "T4JOAuth://main";

	private final String accessTokenKey = "accessTokenKey";
	private final String accessTokenSecretKey = "accessTokenSecretKey";

	private static Twitter twitter;
	private static TwitterFactory twitterFactory;
	private static AccessToken accessToken;

	private Activity mActivity;

	public TheTwitter(Activity activity) {
		mActivity = activity;
		connectionData = SocialSessionStore.restore(SocialWrapper.TWITTER, mActivity);	
		accessToken = getAccessToken();

		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		Configuration configuration = configurationBuilder.build();
		twitterFactory = new TwitterFactory(configuration);

		logTag = "Corso12 - Social - Facebook";
	}

	public Twitter getTwitter() {
		if(twitter == null) {
			Log.i(logTag, "twitter non autenticato twitter == null");
			if(accessToken == null) {
				Log.i(logTag, "Non hai l'accessToken");
				return twitter = null;
			} else {
				Log.i(logTag, "Mi autentico con l'accessToken");
				connectionData.put(accessTokenKey, accessToken.getToken());
				connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
				Log.i(logTag, "------ TEST DI INSERIMENTO ------");
				String s0 = connectionData.get(accessTokenKey);
				String s1 = connectionData.get(accessTokenSecretKey);
				Log.i(logTag, "------ " + s0 + " " + s1 + "------");
				Log.i(logTag, "---------------------------------");
				SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
				return twitter = twitterFactory.getInstance(accessToken);
			}
		} else {
			
			setPropers();
			
			Log.i(logTag, "twitter già autenticato");
			return twitter;	
		}
	}

	private void setPropers() {
		Log.i(logTag, "Mi autentico con l'accessToken");
		connectionData.put(accessTokenKey, accessToken.getToken());
		connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
		Log.i(logTag, "------ TEST DI INSERIMENTO ------");
		String s0 = connectionData.get(accessTokenKey);
		String s1 = connectionData.get(accessTokenSecretKey);
		Log.i(logTag, "------ " + s0 + " " + s1 + "------");
		Log.i(logTag, "---------------------------------");
		SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
	}
	
	public static void setPropersAccessToken(AccessToken accessTokenTemp) throws TwitterException {
		accessToken = accessTokenTemp;
		
		twitter = twitterFactory.getInstance(accessToken);
		Log.i(logTag, "-------------------------------------_");
		Log.i(logTag, "accessToken.getToken() " + accessToken.getToken());
		Log.i(logTag, "twitter.verifyCredentials().getScreenName() " + twitter.verifyCredentials().getScreenName());
		Log.i(logTag, "Ok, tutto fatto!!!");
		Log.i(logTag, "-------------------------------------_");
		twitter.updateStatus("1sfanculo cazzo " + new Random());
		Log.i(logTag, "-------------------------------------_");
	}
	
	public static void deletePropers() {
		Log.i(logTag, "elimino l'istanza di twitter che ho istanziato non autenticata");
		twitter = null;
	}

	private AccessToken getAccessToken() {
		Log.i(logTag, "------ getAccessToken ------");
		String s0 = connectionData.get(accessTokenKey);
		String s1 = connectionData.get(accessTokenSecretKey);
		Log.i(logTag, "------ " + s0 + " " + s1 + "------");
		Log.i(logTag, "---------------------------------");
		if(s0 == null && s1 == null) {
			Log.i(logTag, "AccessToken null 1");
			return null;
		}
		Log.i(logTag, "AccessToken valido, lo ritorno");
		return new AccessToken(s0, s1);
	} 

	private void removeAccessToken() {
		connectionData.remove(accessTokenKey);
		connectionData.remove(accessTokenSecretKey);

		SocialSessionStore.clear(SocialWrapper.TWITTER, mActivity);
	}

	private Boolean checkIstanceTwitter() {
		accessToken = getAccessToken();

		if(accessToken == null)
			return false;
		else 
			return true;
	}

	void OAuthLogin() {
		try {				
			accessToken = getAccessToken();

			if (accessToken != null) {
				Log.i(logTag, "accessToken già salvato, istanzio twitter");
				twitter = twitterFactory.getInstance(accessToken);
			} else {
				Log.i(logTag, "Effettuo il logIn via WebView");
				// Non ho già l'accessToken, devo recuperarmelo
				twitter = twitterFactory.getInstance();
				RequestToken requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
				String authUrl = requestToken.getAuthenticationURL();
				Intent i = new Intent(mActivity.getApplicationContext(), TheTwitterWebView.class);
				Bundle b = new Bundle();
				b.putString("url", authUrl);
				b.putSerializable("twitter", twitter);
				b.putSerializable("requestToken", requestToken);
				i.putExtras(b);
				mActivity.startActivity(i);
			}

		} catch (TwitterException ex) {
			Log.e("in Main.OAuthLogin", ex.getMessage());
		}
	}

	@Override
	public void authenticate() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(consumerKey);
		configurationBuilder.setOAuthConsumerSecret(consumerSecret);
		Configuration configuration = configurationBuilder.build();
		twitterFactory = new TwitterFactory(configuration);

		OAuthLogin();

		if(checkIstanceTwitter()) {
			Log.i(logTag, "Autenticazione avvenuta con successo!");
		} else {
			Log.i(logTag, "Autenticazione non avvenuta!");
		}
	}

	@Override
	public String selfPost(String msg) {
		try {
			if(getTwitter() != null) {
				twitter.updateStatus(msg + " " + new Random());
				Log.i(logTag, ":-)");
				actionResult = ACTION_SUCCESSFUL;
			} else {
				Log.i(logTag, ":-(");
			}
		} catch (TwitterException e) {
			Log.i(logTag, ":-( exception", e);
			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
			removeAccessToken();
		}
		return actionResult;
	}

	@Override
	public String postToFriend(String friendID, String msg) {
		try {
			if(getTwitter() != null) {
				twitter.updateStatus("@" + friendID + " " + msg + " " + new Random());
				Log.i(logTag, ":-)");
				actionResult = ACTION_SUCCESSFUL;
			} else {
				Log.i(logTag, ":-(");
			}
		} catch (TwitterException e) {
			Log.i(logTag, ":-( exception", e);
			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
			removeAccessToken();
		}
		return actionResult;
	}

	@Override
	public ArrayList<SocialFriend> getFriendsList() {
		ArrayList<SocialFriend> friendList = new ArrayList<SocialFriend>();

		if(getTwitter() != null) {
			long cursor = -1;
			IDs ids;
			try {
				do {
					ids = twitter.getFollowersIDs(cursor);
					for (long id : ids.getIDs()) {
						friendList.add(
								new SocialFriend(
										Long.toString(twitter.showUser(id).getId()),
										twitter.showUser(id).getScreenName(),
										twitter.showUser(id).getProfileImageURL().toString()));
					}
				} while ((cursor = ids.getNextCursor()) != 0);
				actionResult = ACTION_SUCCESSFUL;
				Log.i(logTag, ":-)");
				actionResult = ACTION_SUCCESSFUL;
				return friendList;

			} catch (TwitterException e) {
				Log.i(logTag, ":-( exception", e);
				actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
				removeAccessToken();
				return friendList = new ArrayList<SocialFriend>();
			}
		} else {
			Log.i(logTag, ":-( - ritorno una lista vuota");
			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
			removeAccessToken();
			return friendList;
		}
	}

	@Override
	public ArrayList<String> getFriendsUsingCorso12() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String[]> getConnectionData() {
		Vector<String[]> connList = new Vector<String[]>();
		connList.add(new String[] {accessTokenKey, connectionData.get(accessTokenKey)});
		connList.add(new String[] {accessTokenSecretKey, connectionData.get(accessTokenSecretKey)});
		return connList;
	}
}