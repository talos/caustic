package net.microscraper.client;

public interface Cookie {

	/**
	 * 
	 * @return This {@link BasicCookie}'s URL as a {@link String}.
	 */
	public abstract String getUrl();

	/**
	 * 
	 * @return This {@link BasicCookie}'s name.
	 */
	public abstract String getName();

	/**
	 * 
	 * @return This {@link BasicCookie}'s value.
	 */
	public abstract String getValue();

}