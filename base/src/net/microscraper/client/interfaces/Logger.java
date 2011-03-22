package net.microscraper.client.interfaces;

public interface Logger {
	/**
	 * Provide the ability to log errors.
	 */
	public abstract void e(String errorText, Throwable e);
	
	/**
	 * Provide the ability to log information.
	 */
	public abstract void i(String infoText);
}
