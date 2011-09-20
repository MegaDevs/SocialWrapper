package com.megadevs.socialwrapper.thetwitter;

import java.util.ArrayList;
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

	static Twitter twitter;
	static TwitterFactory twitterFactory;
	private static AccessToken accessToken;

	private Activity mActivity;

	public TheTwitter(Activity activity) {
		mActivity = activity;
		connectionData = SocialSessionStore.restore(SocialWrapper.TWITTER, mActivity);	
		accessToken = getAccessToken();
	}

	public Twitter getTwitter() {
		if(twitterFactory == null) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.setOAuthConsumerKey(consumerKey);
			configurationBuilder.setOAuthConsumerSecret(consumerSecret);
			Configuration configuration = configurationBuilder.build();
			twitterFactory = new TwitterFactory(configuration);
		}
		if(twitter == null) {
			connectionData.put(accessTokenKey, accessToken.getToken());
			connectionData.put(accessTokenSecretKey, accessToken.getTokenSecret());
			SocialSessionStore.save(SocialWrapper.TWITTER, this, mActivity);
			twitter = twitterFactory.getInstance(accessToken);
		}
		return twitter;
	}

	public static void setPropersAccessToken(AccessToken accessTokenTemp) {
		accessToken = accessTokenTemp;
	}

	private AccessToken getAccessToken() {
		String s0 = connectionData.get(accessTokenKey);
		String s1 = connectionData.get(accessTokenSecretKey);
		if(s0 == null && s1 == null)
			return null;
		return new AccessToken(s0, s1);
	} 

	private void removeAccessToken() {
		connectionData.remove(accessTokenKey);
		connectionData.remove(accessTokenSecretKey);
		SocialSessionStore.clear(accessTokenKey, mActivity);
		SocialSessionStore.clear(accessTokenSecretKey, mActivity);
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
				twitter = twitterFactory.getInstance(accessToken);
			} else {
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

		}

	}

	@Override
	public String selfPost(String msg) {
		try {
			getTwitter().updateStatus(msg);
			actionResult = ACTION_SUCCESSFUL;
		} catch (TwitterException e) {
			removeAccessToken();
			e.printStackTrace();
			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
		}
		return actionResult;
	}

	@Override
	public String postToFriend(String friendID, String msg) {
		try {
			getTwitter().updateStatus("@" + friendID + " " + msg);
			actionResult = ACTION_SUCCESSFUL;
		} catch (TwitterException e) {
			removeAccessToken();
			e.printStackTrace();
			actionResult = SOCIAL_NETWORK_ERROR + ": bisogna riautenticarsi";
		}
		return actionResult;
	}

	@Override
	public ArrayList<SocialFriend> getFriendsList() {
		ArrayList<SocialFriend> friendList = new ArrayList<SocialFriend>();
		Twitter tw = getTwitter();
		long cursor = -1;
		IDs ids;
		try {
			do {
				ids = tw.getFollowersIDs(cursor);
				for (long id : ids.getIDs()) {
					friendList.add(
							new SocialFriend(
							Long.toString(tw.showUser(id).getId()),
							tw.showUser(id).getScreenName(),
							tw.showUser(id).getProfileImageURL().toString()));
				}
			} while ((cursor = ids.getNextCursor()) != 0);
			actionResult = ACTION_SUCCESSFUL;
			return friendList;
		} catch (TwitterException e) {
			actionResult = SOCIAL_NETWORK_ERROR;
			e.printStackTrace();
			return null;
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