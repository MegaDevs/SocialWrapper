package com.megadevs.socialwrapper.thetwitter;

import com.megadevs.socialwrapper.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

public class TheTwitterWebView extends Activity {	
	
	Twitter twitter;
	RequestToken requestToken;

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
				
				finish();			
			} catch (TwitterException ex) {
				// TODO
			}
		}
	}
}
