package com.megadevs.socialwrapper.theflickr;

import java.io.IOException;
import java.net.URL;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.FlickrException;
import com.gmail.yuyang226.flickr.auth.Permission;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthInterface;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.megadevs.socialwrapper.R;

public class TheFlickrWebView extends Activity {

	private final Uri OAUTH_CALLBACK_URI = Uri.parse("flickr://oauth"); 
	private OAuth aToken;
	
	private String oauthTokenSecret;
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.flickr_webview);
		
		Flickr f = TheFlickrHelper.getInstance().getFlickr();
		OAuthToken oauthToken;
		try {
			oauthToken = f.getOAuthInterface().getRequestToken(OAUTH_CALLBACK_URI.toString());
			oauthTokenSecret = oauthToken.getOauthTokenSecret();
			URL oauthUrl = f.getOAuthInterface().buildAuthenticationUrl(Permission.WRITE, oauthToken);
			
			WebView webview = (WebView) findViewById(R.id.webview);
			webview.loadUrl(oauthUrl.toString());
			WebSettings webSettings = webview.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webview.setWebViewClient(new WebViewClient() {

				/* (non-Javadoc)
				 * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
				 */
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.startsWith("http")) {				
						view.loadUrl(url);
						return super.shouldOverrideUrlLoading(view, url);
					} else {
						String token = Uri.parse(url).getQueryParameter("oauth_token");
						String verifier = Uri.parse(url).getQueryParameter("oauth_verifier");
						
						finalizeOAuth(token, verifier);
						
						finish();
						return super.shouldOverrideUrlLoading(view, url);
					}
				}
				
			});
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlickrException e) {
			e.printStackTrace();
		}
	}
	
		
	private void finalizeOAuth(String oauthToken, String oauthVerifier) {
		if (oauthToken == null || oauthVerifier == null)
			;//TODO handle exception
			
		OAuth oauth = new OAuth();
		OAuthToken token = new OAuthToken();
        token.setOauthToken(null);
        token.setOauthTokenSecret(oauthTokenSecret);
        oauth.setToken(token);

		if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
			Flickr f = TheFlickrHelper.getInstance().getFlickr();
			OAuthInterface oauthApi = f.getOAuthInterface();
			aToken = null;
			try {
				aToken = oauthApi.getAccessToken(oauthToken, oauthTokenSecret, oauthVerifier);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FlickrException e) {
				e.printStackTrace();
			}
			
			if (aToken != null)
				TheFlickr.setAccessToken(aToken.getToken().getOauthToken(), aToken.getToken().getOauthTokenSecret());
			
			finish();
		}

	}
}
	
