package com.megadevs.socialwrapper.thetwitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.megadevs.socialwrapper.R;

//import com.megadevs.socialwrapper.R;

public class TheTwitterWebView extends Activity {	

	Twitter twitter;
	RequestToken requestToken;

	private static Integer count = 0;

	private static String logTag = "Corso12 - Social - Twitter";

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.twitter_webview);

		twitter = (Twitter) getIntent().getSerializableExtra("twitter");
		requestToken = (RequestToken) getIntent().getSerializableExtra("requestToken");

		WebView webViewTest = (WebView)findViewById(R.id.webview);
		webViewTest.loadUrl(getIntent().getExtras().getString("url"));
		WebSettings webSettings = webViewTest.getSettings();
		webSettings.setJavaScriptEnabled(false);
		webViewTest.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				System.out.println("          onPageFinished             ");
				System.out.println(url);
				System.out.println(getHTML(url));
				super.onPageFinished(view, url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println(url);
				System.out.println(getHTML(url));
				System.out.println("                   shouldOverrideUrlLoading (solito)");

				return super.shouldOverrideUrlLoading(view, url);

			}
		});
	}

	public String getHTML(String urlToRead) {		

		URL url; // The URL to read
		HttpURLConnection conn; // The actual connection to the web page
		BufferedReader rd; // Used to read results from the web page
		String line; // An individual line of the web page HTML
		String result = ""; // A long string containing all the HTML
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		File f = new File("/sdcard/test"+count+".txt");
		count += 1;
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("f.exists()        " + f.exists());
		
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(f);	
		} catch (IOException e) {
			System.out.println("");
			System.out.println("                error                    ");
			System.out.println("");
			e.printStackTrace();
		}
		
		BufferedWriter out = new BufferedWriter(fstream);
		try {			
			out.write(result);
		} catch (IOException e) {
			System.out.println("");
			System.out.println("                write error                    ");
			System.out.println("");
			e.printStackTrace();
		}
		
		try {
			out.close();
			fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("File creato!!!!");
		return result;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

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
