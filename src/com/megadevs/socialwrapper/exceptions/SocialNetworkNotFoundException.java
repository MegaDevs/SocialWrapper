package com.megadevs.socialwrapper.exceptions;

@SuppressWarnings("serial")
public class SocialNetworkNotFoundException extends SocialExceptions {
	
	public SocialNetworkNotFoundException(String msg, Exception e) {
		message = msg;
		exception = e;
	}
}
