package net.caustic.http;


public interface HttpUtils {

	/**
	 * Try to get the host from a {@link String} URL.
	 * @param urlStr The {@link String} URL.
	 * @return The host of the URL.
	 * @throws BadURLException If the URL is invalid.
	 */
	public abstract String getHost(String urlStr) throws BadURLException;
}
