package com.invisiblearchitecture.scraper;

public interface LogInterface {
	/**
	 * Provide the ability to log errors.
	 */
	public abstract void e(String errorText, Throwable e);
	
	/**
	 * Provide the ability to log information.
	 */
	public abstract void i(String infoText);
}
