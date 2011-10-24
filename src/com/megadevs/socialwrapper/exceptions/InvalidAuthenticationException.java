package com.megadevs.socialwrapper.exceptions;

@SuppressWarnings("serial")
public class InvalidAuthenticationException extends SocialExceptions {

	public InvalidAuthenticationException(String msg, Exception e) {
		message = msg;
		exception = e;
	}
}
