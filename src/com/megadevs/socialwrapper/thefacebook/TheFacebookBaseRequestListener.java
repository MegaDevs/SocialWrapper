package com.megadevs.socialwrapper.thefacebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.util.Log;

import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.megadevs.socialwrapper.SocialNetwork;

/**
 * Skeleton base class for RequestListeners, providing default error 
 * handling. Applications should handle these error conditions.
 *
 */
public abstract class TheFacebookBaseRequestListener implements RequestListener {
	
	private TheFacebook mFacebook;
	
	public TheFacebookBaseRequestListener(TheFacebook f) {mFacebook = f;}
	
    public void onFacebookError(FacebookError e, final Object state) {
    	mFacebook.setActionResult(SocialNetwork.SOCIAL_NETWORK_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.SOCIAL_NETWORK_ERROR, e);
    }

    public void onFileNotFoundException(FileNotFoundException e,
                                        final Object state) {
    	mFacebook.setActionResult(SocialNetwork.GENERAL_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.GENERAL_ERROR, e);
    }

    public void onIOException(IOException e, final Object state) {
    	mFacebook.setActionResult(SocialNetwork.GENERAL_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.GENERAL_ERROR, e);
    }

    public void onMalformedURLException(MalformedURLException e,
                                        final Object state) {
    	mFacebook.setActionResult(SocialNetwork.GENERAL_ERROR);
		Log.d(SocialNetwork.logTag, SocialNetwork.GENERAL_ERROR, e);
    }
    
}
