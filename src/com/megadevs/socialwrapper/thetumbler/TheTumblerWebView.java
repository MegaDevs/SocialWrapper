package com.megadevs.socialwrapper.thetumbler;

import com.megadevs.socialwrapper.R;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TheTumblerWebView extends Activity {

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.tumblr_webview);
		WebView webViewTest = (WebView)findViewById(R.id.tumblrauth);
		webViewTest.loadUrl(getIntent().getExtras().getString("url"));
		webViewTest.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webViewTest.getSettings().setJavaScriptEnabled(true);
		webViewTest.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println("shouldOverrideUrlLoading : = : " + url);
				if (url.startsWith("http")) {
					view.loadUrl(url);
					return super.shouldOverrideUrlLoading(view, url);
				} else {
					Bundle b = new Bundle();
					b.putString("verifier", Uri.parse(url).getQueryParameter(OAuth.OAUTH_VERIFIER));
					Intent i = new Intent();
					i.putExtras(b);
					TheTumblr.setPropers(1, i);
					TheTumblr.getTumblr().authenticate();

					TheTumblerWebView.this.finish();			
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			TheTumblr.setPropers(0, null); 
			finish();
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
}
