package com.megadevs.socialwrapper.exceptions;

@SuppressWarnings("serial")
public class InvalidSocialRequestException extends SocialExceptions {

	public InvalidSocialRequestException(String msg, Exception e) {
		message = msg;
		exception = e;
	}

}
