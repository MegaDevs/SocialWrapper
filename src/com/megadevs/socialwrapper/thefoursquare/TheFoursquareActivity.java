package com.megadevs.socialwrapper.thefoursquare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jiramot.foursquare.android.DialogError;
import com.jiramot.foursquare.android.Foursquare;
import com.jiramot.foursquare.android.Foursquare.DialogListener;
import com.jiramot.foursquare.android.FoursquareError;
import com.megadevs.socialwrapper.SocialNetwork;

public class TheFoursquareActivity extends Activity {
	
	private Foursquare foursquare;
	private TheFoursquare iAmTheFoursquare;
	
	private String clientID;
	private String callbackURL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		iAmTheFoursquare = TheFoursquare.getInstance();
		
		Bundle b = getIntent().getExtras();
		String clientIDKey = TheFoursquare.getInstance().clientIDKey;
		String callbackURLKey = TheFoursquare.getInstance().callbackURLKey;
		
		clientID = b.getString(clientIDKey);
		callbackURL = b.getString(callbackURLKey);
		
		init(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		init(intent);
	}
	
	private void init(Intent intent) {
		if (intent.getScheme() == null) {
			foursquare = new Foursquare(clientID, callbackURL);
			foursquare.authorize(this, new AuthDialogListener());
		}
	}
	
	private class AuthDialogListener implements DialogListener {
		@Override
		public void onCancel() {
			Log.d(iAmTheFoursquare.tag, SocialNetwork.ACTION_CANCELED);
			iAmTheFoursquare.setActionResult(SocialNetwork.ACTION_CANCELED);
			iAmTheFoursquare.forwardErrorResult(null);
			
			finish();
		}

		@Override 
		public void onComplete(Bundle values) {
			Log.i(iAmTheFoursquare.tag, "login performed");
			iAmTheFoursquare.setActionResult(SocialNetwork.ACTION_SUCCESSFUL);
			iAmTheFoursquare.setFoursquare(foursquare);
			
			finish();
		}

		@Override
		public void onError(DialogError e) {
			Log.d(iAmTheFoursquare.tag, SocialNetwork.GENERAL_ERROR, e);
			iAmTheFoursquare.setActionResult(SocialNetwork.GENERAL_ERROR);
			iAmTheFoursquare.forwardErrorResult(null);
			
			finish();
		}

		@Override
		public void onFoursquareError(FoursquareError e) {
			Log.d(iAmTheFoursquare.tag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
			iAmTheFoursquare.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR);
			iAmTheFoursquare.forwardErrorResult(null);
			
			finish();
		}
		
	}
}
