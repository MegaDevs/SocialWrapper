package com.megadevs.socialwrapper.thetwitter;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.megadevs.socialwrapper.R;

//import com.megadevs.socialwrapper.R;

public class TheTwitterWebView extends Activity {	

	Twitter twitter;
	RequestToken requestToken;

	private static String logTag = "Corso12 - Social - Twitter";

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.twitter_webview);

		twitter = (Twitter) getIntent().getSerializableExtra("twitter");
		requestToken = (RequestToken) getIntent().getSerializableExtra("requestToken");

		WebView webViewTest = (WebView)findViewById(R.id.webview);
		System.out.println(" ====>>>>> " + getIntent().getExtras().getString("url"));
		webViewTest.loadUrl(getIntent().getExtras().getString("url"));
		//webSettings webSettings = webViewTest.getSettings();
		//webSettings.setJavaScriptEnabled(false);
		webViewTest.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println(url);
				if (url.startsWith("http")) {				
					view.loadUrl(url);
					return super.shouldOverrideUrlLoading(view, url);
				} else {

					String verifier = Uri.parse(url).getQueryParameter(OAuth.OAUTH_VERIFIER);
		        
					try {
						AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
						TheTwitter.getTwitter().setPropersAccessToken(accessToken);
					} catch (TwitterException ex) {
						Log.i(logTag, "Mah :-(", ex);
					}
		            
		            TheTwitterWebView.this.finish();			
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			TheTwitter.deletePropers();
			finish();
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
}
/*
@Override
protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	
	System.out.println("----------------00000000000----------------------------");
	System.out.println("--------------00000000000000----------------");
	System.out.println("---------------0000000000000-----------------------------");
	System.out.println("----------------000000000---------------------------");
	
	if(intent.getScheme().equals("T4JOAuth")) {
		Uri uri = intent.getData();
		try {
			String verifier = uri.getQueryParameter("oauth_verifier");
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
			TheTwitter.getTwitter().setPropersAccessToken(accessToken);
			finish();			
		} catch (TwitterException ex) {
			Log.i(logTag, "Mah :-(", ex);
		}
	}
}
*/