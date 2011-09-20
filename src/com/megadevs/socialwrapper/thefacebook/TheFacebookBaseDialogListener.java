package com.megadevs.socialwrapper.thefacebook;

import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.megadevs.socialwrapper.SocialNetwork;

/**
 * Skeleton base class for RequestListeners, providing default error 
 * handling. Applications should handle these error conditions.
 *
 */
public abstract class TheFacebookBaseDialogListener implements DialogListener {

	private TheFacebook mFacebook;

	public TheFacebookBaseDialogListener(TheFacebook f) {mFacebook = f;}
	
    public void onFacebookError(FacebookError e) {
    	mFacebook.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
    }

    public void onError(DialogError e) {
    	mFacebook.setActionResult(SocialNetwork.GENERAL_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.GENERAL_ERROR, e);   
    }

    public void onCancel() {
    	mFacebook.setActionResult(SocialNetwork.ACTION_CANCELED);
		Log.d(SocialNetwork.logTag, SocialNetwork.ACTION_CANCELED);
    }
    
}
