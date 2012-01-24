package com.megadevs.socialwrapper.thefacebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


/**
 * This activity is intended to be used only for the authentication sequence. It is
 * needed because of the callback mechanism of the Facebook-Android-SDK which is
 * based on the onActivityResult() method.
 * @author dextor
 *
 */
public class TheFacebookActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(new View(this));
		TheFacebook f = TheFacebook.getInstance();
		f.getmFacebook().authorize(this,
				new String[] {"publish_stream", "read_stream"}, //"offline_access" 
				f.new AuthDialogListener());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// retrieves the existing instance of TheFacebook, gets the Facebook object and authorizes
		// the callback on THAT particular object (so that it can be used from there on)
		TheFacebook.getInstance().getmFacebook().authorizeCallback(requestCode, resultCode, data);

		finish();
	}
}