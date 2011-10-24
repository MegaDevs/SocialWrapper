package com.megadevs.socialwrapper.exceptions;

@SuppressWarnings("serial")
public abstract class SocialExceptions extends Throwable {
	protected Exception exception;
	protected String message;
	
	/**
	 * @return the exception
	 */
	public Exception getException() {
		return exception;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}