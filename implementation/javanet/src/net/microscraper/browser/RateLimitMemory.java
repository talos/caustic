package net.microscraper.browser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Keep track of how many bytes are loaded from hosts, and when.  Used to calculate rate
 * limits.
 * @see #rememberResponse(String, int)
 * @see #getRateSinceLastResponseFrom(String)
 * @author realest
 *
 */
class RateLimitMemory {
	private final Map<String, Integer> hostBytesLoaded = new HashMap<String, Integer>();
	private final Map<String, Date> hostLastLoad = new HashMap<String, Date>();
	
	/**
	 * Try to get the host from a {@link String} url.
	 * @param urlStr The {@link String} url.
	 * @return The host of the url, if it is valid; the entire url, otherwise.
	 */
	private String getHost(String urlStr) {
		try {
			return new URL(urlStr).getHost();
		} catch(MalformedURLException e) {
			return urlStr;
		}
	}
	
	/**
	 * Remember that a response of a certain size was pulled from a URL.
	 * @param urlStr The {@link String} url that supplied the response.
	 * @param bytesLoaded The size of the response, in bytes.
	 */
	public void rememberResponse(String urlStr, int responseByteSize) {
		String host = getHost(urlStr);
		hostBytesLoaded.put(host, responseByteSize);
		hostLastLoad.put(host, new Date());
	}
	
	/**
	 * Returns the number of bytes of the last response from the host of
	 * <code>urlStr</code> divided by the number of milliseconds since that
	 * response.
	 * @param urlStr The {@link String} url whose host's rate should be calculated.
	 * @return This rate.
	 */
	public float getRateSinceLastResponseFrom(String urlStr) {
		Date now = new Date();
		String host = getHost(urlStr);
		float rate = 0;
		if(hostLastLoad.containsKey(host)) {
			long millisecondsSince = now.getTime() - hostLastLoad.get(host).getTime() + 1;
			int bytesLastLoaded = hostBytesLoaded.get(host);
			rate = bytesLastLoaded / millisecondsSince;
		}
		return rate;
	}
}