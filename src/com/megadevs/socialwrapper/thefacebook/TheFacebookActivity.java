package com.megadevs.socialwrapper.thefacebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

class TheFacebookActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(new View(this));
		
		TheFacebook f = TheFacebook.getInstance();
		f.getmFacebook().authorize(this,
				new String[] {"publish_stream", "read_stream", "offline_access"}, 
				f.new AuthDialogListener());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		TheFacebook.getInstance().getmFacebook().authorizeCallback(requestCode, resultCode, data);

		finish();
	}
}