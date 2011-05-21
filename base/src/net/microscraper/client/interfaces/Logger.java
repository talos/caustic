package net.microscraper.client.interfaces;

public interface Logger {
	public static final int MAX_ENTRY_LENGTH = 512;
	
	/**
	 * Provide the ability to log throwables as errors.
	 */
	public abstract void e(Throwable e);
	
	/**
	 * Provide the ability to log throwables as warnings.
	 */
	public abstract void w(Throwable w);
	
	/**
	 * Provide the ability to log information.
	 */
	public abstract void i(String infoText);
	
}