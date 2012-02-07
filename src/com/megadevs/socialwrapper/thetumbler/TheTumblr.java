package com.megadevs.socialwrapper.thetumbler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.megadevs.socialwrapper.SocialFriend;
import com.megadevs.socialwrapper.SocialNetwork;
import com.megadevs.socialwrapper.SocialSessionStore;
import com.megadevs.socialwrapper.SocialWrapper;

public class TheTumblr extends SocialNetwork {

	private final String accessTokenKey = "accessTokenKey";
	private final String accessTokenSecretKey = "accessTokenSecretKey";

	public static String CONSUMER_KEY = ""; //"vnIBtBEf0VGdBYKlsk0LsMsKYsLGazq0AaK1zOuRjPni75SIL5";
	public static  String CONSUMER_SECRET = ""; //"jSHDiUUU3BRPeCIaSv6t6dQKCpwsJM1Y1H4Iv07OhiyYx0zRuJ";

	public static final String REQUEST_URL = "http://www.tumblr.com/oauth/request_token";
	public static final String ACCESS_URL = "http://www.tumblr.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://www.tumblr.com/oauth/authorize";

	public static String	OAUTH_CALLBACK_SCHEME	= "";//"oauthflow-tumblr";
	public static String	OAUTH_CALLBACK_HOST		= "";//"callback";
	public static String	OAUTH_CALLBACK_URL		= "";//OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

	private static CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	private static CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);

	private static TheTumblr mTumblr;

	// static callback refs
	private TheTumblrLoginCallback loginCallback;
	private TheTumblrPostCallback postCallback;

	private static String authURL;

	private Activity mActivity;

	private String token = "";
	private String secret = "";
	private static String verifier = "";

	private String nickName = "";
	private ArrayList<String> blogsName = null;
	private Integer numOfBlogs = 0;

	Integer numOfFollowers = 0;
	private String[][] usersInfo = null;

	private Intent newIntent = null;

	private Boolean authenticated = false;

	public TheTumblr(String id, Activity activity) {
		System.out.println("Costruttore!!");
		mActivity = activity;
		mTumblr = this;
		this.id = id;
	}

	public static TheTumblr getTumblr() {
		return mTumblr;
	}

	public void setParameters(String key, String secret, String callback) {
		System.out.println("setParameters public");

		CONSUMER_KEY = key;
		CONSUMER_SECRET = secret;
		OAUTH_CALLBACK_URL = callback;

		SocialSessionStore.restore(SocialWrapper.THETUMBLR, this, mActivity);

		token = connectionData.get("TUMBLR_OAUTH_TOKEN");
		secret = connectionData.get("TUMBLR_OAUTH_TOKEN_SECRET");
		verifier = connectionData.get("TUMBLR_VERIFIER");
		nickName = connectionData.get("TUMBLR_NICKNAME");
		numOfBlogs = Integer.getInteger(connectionData.get("TUMBLR_NUM_BLOGS"));
		blogsName = new ArrayList<String>();

		for(int i = 0; i < numOfBlogs; i++) {
			blogsName.add(connectionData.get("TUMBLR_BLOG" + i));
		}

		if(token.equals("") && secret.equals("")) {
			System.out.println("Utente da autenticare");
			authenticated = false;
		} else {
			System.out.println("Utente gia' autenticato");
			consumer.setTokenWithSecret(token, secret);
			authenticated = true;
		}

		if(authenticated == true) {
			try {
				provider.retrieveAccessToken(consumer, verifier);
				token = consumer.getToken();
				secret = consumer.getTokenSecret();
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			}
		}
	}

	public static void setPropers(int resultCode, Intent data) {
		System.out.println("I'm back");
		if(resultCode == 0) {
			System.out.println("Male, ritorno non corretto");
			verifier = "";
		} else if(resultCode == 1) {
			System.out.println("Bene, ritorno corretto");
			verifier = data.getExtras().getString("verifier");
		}
	}

	//recupero l'URL per l'autenticazione
	private void setAuthURL()
	{
		try {
			authURL = provider.retrieveRequestToken(consumer, OAUTH_CALLBACK_URL);
			System.out.println("authURL " + authURL);
		} catch (OAuthMessageSignerException e) { e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) { e.printStackTrace();
		} catch (OAuthExpectationFailedException e) { e.printStackTrace();
		} catch (OAuthCommunicationException e) { e.printStackTrace(); }
	}

	public void deauthenticate() {
		System.out.println("Reset del token, del verifier e dei blog, sia su app che su pref");
		token = verifier = secret = "";

		blogsName = null;
		numOfBlogs = 0;

		SocialSessionStore.clear(SocialWrapper.THETUMBLR, mActivity);
		authenticated = false;
	}

	public void authenticate() {
		System.out.println("authenticate()");
		if(authenticated == false) {
			if(verifier.equals("")) {
				System.out.println("Devi prima recuperare il verifier per autenticare l'utente");
				setAuthURL();
				newIntent = new Intent(mActivity, TheTumblerWebView.class);
				Bundle b = new Bundle();
				b.putString("url", authURL);
				newIntent.putExtras(b);
				mActivity.startActivity(newIntent);
			} else {
				System.out.println("Recupero l'accessToken grazie al verifier");
				try {
					provider.retrieveAccessToken(consumer, verifier);
				} catch (OAuthMessageSignerException e) { e.printStackTrace();
				} catch (OAuthNotAuthorizedException e) { e.printStackTrace();
				} catch (OAuthExpectationFailedException e) { e.printStackTrace();
				} catch (OAuthCommunicationException e) { e.printStackTrace(); }

				token = consumer.getToken();
				secret = consumer.getTokenSecret();

				connectionData.put("TUMBLR_OAUTH_TOKEN", token);
				connectionData.put("TUMBLR_OAUTH_TOKEN_SECRET", secret);
				connectionData.put("TUMBLR_VERIFIER", verifier);

				// Retrive blogs' user information
				HttpPost hpost = new HttpPost("http://api.tumblr.com/v2/user/info");

				consumer.setTokenWithSecret(token, secret);
				try {
					consumer.sign(hpost);
				} catch (OAuthMessageSignerException e) { e.printStackTrace();
				} catch (OAuthExpectationFailedException e) { e.printStackTrace();
				} catch (OAuthCommunicationException e) { e.printStackTrace(); }

				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse resp = null;

				try {
					resp = client.execute(hpost);
				} catch (ClientProtocolException e) { e.printStackTrace();
				} catch (IOException e) { e.printStackTrace(); }

				try {
					String result = EntityUtils.toString(resp.getEntity());
					System.out.println("result " + result);
					blogsName = getBlogs(result);
				} catch (ParseException e) { e.printStackTrace();
				} catch (IOException e) { e.printStackTrace(); }
				SocialSessionStore.save(SocialWrapper.THETUMBLR, this, mActivity);
				authenticated = true;
				loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
			}
		} else {
			System.out.println("Sei gia' autenticato");
			loginCallback.onLoginCallback(SocialNetwork.ACTION_SUCCESSFUL);
		}
	}

	public ArrayList<String> choiceBlog() {
		if(verifier.equals("")) {
			authenticate();
			return null;
		} else {
			authenticate();
			return blogsName;
		}
	}

	public void sendPost(String blog, String title, String body, SocialBaseCallback s) 
	{
		postCallback = (TheTumblrPostCallback) s;
		boolean callback = false;
		authenticate();
		if(!verifier.equals("")) {
			if(blog.equals("")) 
				blog = blogsName.get(0); // In the first position there is the default blog
			HttpPost hpost = new HttpPost("http://api.tumblr.com/v2/blog/" +  blog + ".tumblr.com/post"); 

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("type", "text"));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("body", body));

			consumer.setTokenWithSecret(token, secret);

			try {
				hpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) { System.out.println("ERROR1");e.printStackTrace();	}
			try {
				consumer.sign(hpost);
			} catch (OAuthMessageSignerException e) {  
				callback = true; deauthenticate(); postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR); e.printStackTrace();
			} catch (OAuthExpectationFailedException e) { 
				callback = true; deauthenticate(); postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR); e.printStackTrace();
			} catch (OAuthCommunicationException e) { 
				callback = true; deauthenticate(); postCallback.onErrorCallback(SOCIAL_NETWORK_ERROR); e.printStackTrace(); }

			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;

			try {
				resp = client.execute(hpost);
			} catch (ClientProtocolException e) { 
				callback = true; postCallback.onErrorCallback(GENERAL_ERROR); e.printStackTrace();
			} catch (IOException e) { 
				callback = true; postCallback.onErrorCallback(GENERAL_ERROR); e.printStackTrace(); }

			try {
				String result = EntityUtils.toString(resp.getEntity());
				System.out.println("Post Result " + result);
			} catch (ParseException e) { postCallback.onErrorCallback(GENERAL_ERROR); e.printStackTrace();
			} catch (IOException e) { postCallback.onErrorCallback(GENERAL_ERROR); e.printStackTrace(); }
			if(callback == false)
				postCallback.onPostCallback(ACTION_SUCCESSFUL);
		}
	}

	public void uploadImage(String blog) {

		if(verifier.equals("")) {
			authenticate();
			System.out.println("cip e ciop fa ...");

		} else {
			authenticate();
			HttpPost hpost = new HttpPost("http://api.tumblr.com/v2/blog/" +  blog + ".tumblr.com/post"); 

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

			nameValuePairs.add(new BasicNameValuePair("type", "photo"));
			//nameValuePairs.add(new BasicNameValuePair("source", new File("/sdcard/foto.jpg")));
			nameValuePairs.add(new BasicNameValuePair("caption", "caption de sto cazzo"));
			//nameValuePairs.add(new BasicNameValuePair("options", "mPostOptions"));

			System.out.println("token = " + token);
			System.out.println("secret = " + secret);
			consumer.setTokenWithSecret(token, secret);

			try {
				hpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) { System.out.println("ERROR1");e.printStackTrace();	}
			try {
				consumer.sign(hpost);
			} catch (OAuthMessageSignerException e) { e.printStackTrace();
			} catch (OAuthExpectationFailedException e) { e.printStackTrace();
			} catch (OAuthCommunicationException e) { e.printStackTrace(); }

			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;

			try {
				resp = client.execute(hpost);
			} catch (ClientProtocolException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }

			try {
				String result = EntityUtils.toString(resp.getEntity());
				System.out.println("Post Result " + result);
			} catch (ParseException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }

		}

	}

	public String[] getFriendsList() {
		if(verifier.equals("")) {
			authenticate();
			return null;
		} else {
			authenticate();
			System.out.println("getFriendsList()");
			HttpPost hpost = new HttpPost("http://api.tumblr.com/v2/blog/" + nickName + ".tumblr.com/followers");
			System.out.println("hpost -> " + "http://api.tumblr.com/v2/blog/" + nickName + ".tumblr.com/followers");

			consumer.setTokenWithSecret(token, secret);
			try {
				consumer.sign(hpost);
			} catch (OAuthMessageSignerException e) { e.printStackTrace();
			} catch (OAuthExpectationFailedException e) { e.printStackTrace();
			} catch (OAuthCommunicationException e) { e.printStackTrace(); }

			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;

			try {
				resp = client.execute(hpost);
			} catch (ClientProtocolException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }

			try {
				String result = EntityUtils.toString(resp.getEntity());
				System.out.println( "GetFriendList Result " + result);
				getFriends(result);
			} catch (ParseException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace(); }
			if(numOfFollowers == 0)
				return null;
			else {
				String[] resultFriend = new String[numOfBlogs];
				for(int i = 0; i < numOfFollowers; i++) {
					System.out.println("usersInfo[i][0] " + usersInfo[i][0] + " usersInfo[i][1] " + usersInfo[i][1]);
				}
				for(int i = 0; i < numOfFollowers; i++) {
					resultFriend = usersInfo[i];
				}
				return resultFriend;
			}
		}
	}

	public String[][] getFriends(String result) {
		try {
			JSONObject jObj = new JSONObject(result);

			// --- META ---
			JSONObject jMeta = jObj.getJSONObject("meta");
			String jStatus = jMeta.getString("status"); 
			String jMsg = jMeta.getString("msg");
			System.out.println("status " + jStatus + " msg " + jMsg);

			// Authentication Check!!
			if(jStatus.equals("200")) {
				System.out.println("uguale");
				authenticated = true;
			} else {
				authenticated = false;
				deauthenticate();
				return null;
			}

			JSONObject jResponse = jObj.getJSONObject("response");
			numOfFollowers = jResponse.getInt("total_users");
			System.out.println("numOfFollowers " + numOfFollowers);

			JSONArray jUsers = jResponse.getJSONArray("users");

			usersInfo = new String[numOfFollowers][2];

			for(int i = 0; i < numOfFollowers; i++) {
				JSONObject jUser = jUsers.getJSONObject(i);
				String jUserName = jUser.getString("name");
				String jUserUrl = jUser.getString("url");
				usersInfo[i][0] = jUserName;
				usersInfo[i][1] = jUserUrl;
				System.out.println("jUserName " + jUserName + " jUserUrl " + jUserUrl);
			}
			return usersInfo;
		} catch (JSONException e) { e.printStackTrace(); return null;}
	}

	public ArrayList<String> getBlogs(String result) {
		try {
			JSONObject jObj = new JSONObject(result);

			// --- META ---
			JSONObject jMeta = jObj.getJSONObject("meta");
			String jStatus = jMeta.getString("status"); 
			String jMsg = jMeta.getString("msg");
			System.out.println("status " + jStatus + " msg " + jMsg);

			// Authentication Check!!
			if(jStatus.equals("200")) {
				authenticated = true;
			} else {
				authenticated = false;
				deauthenticate();
				return null;
			}

			// --- RESPONSE --
			JSONObject jResponse = jObj.getJSONObject("response");
			JSONObject jUser = jResponse.getJSONObject("user");
			nickName = jUser.getString("name");
			connectionData.put("TUMBLR_NICKNAME", nickName);
			System.out.println("Nick Name " + nickName);
			JSONArray jBlogS = jUser.getJSONArray("blogs");
			numOfBlogs = jBlogS.length();
			connectionData.put("TUMBLR_NUM_BLOGS", Integer.toString(numOfBlogs));
			System.out.println("JBlogs num " + numOfBlogs);
			ArrayList<String> blogsNameLocal = new ArrayList<String>();
			String blogName = "";
			for(int i = 0; i < jBlogS.length(); i++) {
				blogName = jBlogS.getJSONObject(i).getString("name");
				blogsNameLocal.add(blogName);
				connectionData.put("TUMBLR_BLOG" + i, blogName);
			}
			return blogsNameLocal;
		} catch (JSONException e) { e.printStackTrace(); return null;}
	}

	@Override
	public void authenticate(SocialBaseCallback s) {
		loginCallback = (TheTumblrLoginCallback)s;
		new Thread(new Runnable() {
			@Override
			public void run() {
				authenticate();
			}
		}).start();
	}

	@Override
	public void getFriendsList(SocialBaseCallback s) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Vector<String[]> getConnectionData() {
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
		if (!token.equals("") && !secret.equals(""))
			return token +';' + secret;
		return null;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	///
	///	LISTENERS >IMPLEMENTATIONS< CLASSES, MAY HAVE PRIVATE ACCESS
	///

	public static abstract class TheTumblrLoginCallback implements SocialBaseCallback {
		public abstract void onLoginCallback(String result);
		public void onPostCallback(String result) {};
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error); 
	}

	public static abstract class TheTumblrPostCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public abstract void onPostCallback(String result);
		public void onFriendsListCallback(String result, ArrayList<SocialFriend> list) {};
		public abstract void onErrorCallback(String error);
	}

	public static abstract class TheTumblrFriendListCallback implements SocialBaseCallback {
		public void onLoginCallback(String result) {};
		public void onPostCallback(String result) {};
		public abstract void onFriendsListCallback(String result, ArrayList<SocialFriend> list);
		public abstract void onErrorCallback(String error);
	}
}