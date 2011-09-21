package com.megadevs.socialwrapper.thetwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.megadevs.socialwrapper.R;

public class TheTwitterWebView extends Activity {	
	
	Twitter twitter;
	RequestToken requestToken;

	private static String logTag = "Corso12 - Social - Facebook";
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.twitter_webview);

		twitter = (Twitter) getIntent().getSerializableExtra("twitter");
		requestToken = (RequestToken) getIntent().getSerializableExtra("requestToken");
		
		WebView webViewTest = (WebView)findViewById(R.id.webview);
		webViewTest.loadUrl(getIntent().getExtras().getString("url"));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if(intent.getScheme().equals("T4JOAuth")) {
			Uri uri = intent.getData();
			try {
				String verifier = uri.getQueryParameter("oauth_verifier");
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				TheTwitter.setPropersAccessToken(accessToken);
				
				Log.i(logTag, "Ok1");
				finish();			
				Log.i(logTag, "Ok2");
			} catch (TwitterException ex) {
				Log.i(logTag, "Mah :-(", ex);
			}
			Log.i(logTag, "ehiehiehieehieiheieheih");
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i(logTag, "Hai premuto back amico!!!! Ma sei stupido?!?!?!?");
			TheTwitter.deletePropers();
			finish();
			return true;
		} else {
			Log.i(logTag, "PROSEGUI");
			return super.onKeyUp(keyCode, event);
		}
	}
}
