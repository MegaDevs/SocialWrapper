package com.megadevs.socialwrapper.exceptions;

@SuppressWarnings("serial")
public class MissingLoginCredentialsException extends SocialExceptions {

	public MissingLoginCredentialsException(String msg, Exception e) {
		message = msg;
		exception = e;
	}
}
