package net.microscraper.http;

import java.util.Date;
import java.util.Hashtable;

import net.microscraper.util.HttpUtils;

/**
 * Keep track of how many bytes are loaded from hosts, and when.  Used to calculate rate
 * limits.
 * @see #rememberResponse(String, int)
 * @see #getRateSinceLastResponseFrom(String)
 * @author realest
 *
 */
public class RateLimitManager {

	/**
	 * The default rate limit a {@link RateLimitManager} interface imposes upon itself for
	 * a single host.
	 */
	public static final int DEFAULT_RATE_LIMIT = 30;
	
	/**
	 * How many milliseconds to wait before placing a second request ot
	 * a single host.
	 */
	public static final int DEFAULT_REQUEST_WAIT = 1000;
	
	private final Hashtable hostLastRequest = new Hashtable();
	private final Hashtable hostLastResponse = new Hashtable();
	private final Hashtable hostLastResponseSize = new Hashtable();
	private final HttpUtils httpUtils;
	private int rateLimitKBps = DEFAULT_RATE_LIMIT;
	private int minRequestWaitMilliseconds = DEFAULT_REQUEST_WAIT;
	
	/**
	 * 
	 * @param urlStr The {@link String} URL to extract the host from.
	 * @return The host of <code>urlStr</code> as a {@link String},
	 * if it can be extracted; <code>urlStr</code> itself otherwise.
	 */
	private String getHost(String urlStr) {
		try {
			return httpUtils.getHost(urlStr);
		} catch(BadURLException e) {
			return urlStr;
		}
	}

	public RateLimitManager(HttpUtils httpUtils) {
		this.httpUtils = httpUtils;
	}
	
	
	/**
	 * Remember that a response of a certain size was pulled from a URL.
	 * @param urlStr The {@link String} url that supplied the response.
	 * @param responseByteSize The size of the response, in bytes.
	 */
	public void rememberResponse(String urlStr, int responseByteSize) {
		String host = getHost(urlStr);
		hostLastResponse.put(host, Long.valueOf(new Date().getTime()));
		hostLastResponseSize.put(host, Integer.valueOf(responseByteSize));
	}
	
	/**
	 * Check to see whether a request to <code>urlStr</code> should be delayed.
	 * When this returns <code>true</code>, assumption is that a request has been made --
	 * the {@link #hostLastRequest} table will be set.
	 * @param urlStr The {@link String} url whose host's rate should be calculated.
	 * @return <code>true</code> if the request should be delayed, <code>false</code>
	 * otherwise.
	 */
	public boolean shouldDelay(String urlStr) {
		boolean shouldDelay = false;
		synchronized(this) {
			long now = new Date().getTime();
			String host = getHost(urlStr);
			
			// Check delay due to repeated requests.
			if(hostLastRequest.containsKey(host)) {
				long millisecondsSince = now - ((Long) hostLastRequest.get(host)).longValue();
				if(millisecondsSince < minRequestWaitMilliseconds) {
					shouldDelay = true;
				}
			}
			
			// Check delay due to rate limit.
			if(hostLastResponse.containsKey(host)) {
				long millisecondsSince = now - ((Long) hostLastResponse.get(host)).longValue() + 1;
				int bytesLastLoaded = ((Integer) hostLastResponseSize.get(host)).intValue();
				if(bytesLastLoaded / millisecondsSince > rateLimitKBps) {
					shouldDelay = true;
				}
			}
			
			if(shouldDelay == false) {
				hostLastRequest.put(host, Long.valueOf(now));
			}
		}
		return shouldDelay;
	}
	
	/**
	 * Change this {@link RateLimitManager}'s enforced rate limit.
	 * @param rateLimitKBps The rate limit to use, in kilobytes per second.
	 */
	public void setRateLimit(int rateLimitKBps) {
		this.rateLimitKBps = rateLimitKBps;
	}
	
	/**
	 * Change this {@link RateLimitManager}'s minimum wait before repeated
	 * requests to a single host.
	 * @param minRequestWaitMilliseconds The number of milliseconds to wait
	 * between requests to a single host, in milliseconds.
	 */
	public void setMinRequestWait(int minRequestWaitMilliseconds) {
		this.minRequestWaitMilliseconds = minRequestWaitMilliseconds;
	}
}