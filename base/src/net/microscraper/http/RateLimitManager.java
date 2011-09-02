package net.microscraper.http;

import java.util.Date;
import java.util.Hashtable;

import net.microscraper.log.Logger;
import net.microscraper.util.HttpUtils;
import net.microscraper.util.StringUtils;

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
	 * How many milliseconds a {@link RateLimitManager} will sleep before
	 * considering trying a host again, if when it last considered requesting
	 * from a host it would have exceeded its rate limit.
	 */
	public static final int DEFAULT_SLEEP_TIME = 500;
	
	/**
	 * The default rate limit a {@link RateLimitManager} interface imposes upon itself for
	 * a single host.
	 */
	public static final int DEFAULT_RATE_LIMIT = 30;
	
	private final Hashtable hostBytesLoaded = new Hashtable();
	private final Hashtable hostLastLoad = new Hashtable();
	private final HttpUtils httpUtils;
	private int rateLimitKBps;
	
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
		this.rateLimitKBps = DEFAULT_RATE_LIMIT;
	}
	
	/**
	 * Remember that a response of a certain size was pulled from a URL.
	 * @param urlStr The {@link String} url that supplied the response.
	 * @param bytesLoaded The size of the response, in bytes.
	 */
	public void rememberResponse(String urlStr, int responseByteSize) {
		String host = getHost(urlStr);
		hostBytesLoaded.put(host, Integer.valueOf(responseByteSize));
		hostLastLoad.put(host, new Date());
	}
	
	/**
	 * Check to see whether a request to <code>urlStr</code> should be delayed,
	 * and hold the thread accordingly.
	 * @param urlStr The {@link String} url whose host's rate should be calculated.
	 * @param log The {@link Logger} to log delay information to.
	 * @throws InterruptedException If the user interrupts during a delay.
	 */
	public void obeyRateLimit(String urlStr, Logger log) throws InterruptedException {
		Date now = new Date();
		String host = getHost(urlStr);
		float rate = 0;
		if(hostLastLoad.containsKey(host)) {
			long millisecondsSince = now.getTime() - ((Date) hostLastLoad.get(host)).getTime() + 1;
			int bytesLastLoaded = ((Integer) hostBytesLoaded.get(host)).intValue();
			rate = bytesLastLoaded / millisecondsSince;
		}
		if(rate > rateLimitKBps) {
			log.i("Delaying load of " + StringUtils.quote(urlStr) + ", current KBPS " +
					StringUtils.quote(Float.toString(rate)));
			Thread.sleep(DEFAULT_SLEEP_TIME);
			obeyRateLimit(urlStr, log);
		}
	}
	
	/**
	 * Change this {@link RateLimitManager}'s enforced rate limit.
	 * @param rateLimitKBps The rate limit to use, in kilobytes per second.
	 */
	public void setRateLimit(int rateLimitKBps) {
		this.rateLimitKBps = rateLimitKBps;
	}
}