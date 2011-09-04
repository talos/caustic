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
	 * Remember that a request has been made of a URL.
	 * @param urlStr
	 */
	public void rememberRequest(String urlStr) {
		synchronized(hostLastRequest) {
			String host = getHost(urlStr);
			if(hostLastRequest.containsKey(host)) {
				synchronized(hostLastRequest.get(host)) {
					hostLastRequest.put(host, Long.valueOf(new Date().getTime()));
				}
			}
		}
	}
	
	/**
	 * Remember that a response of a certain size was pulled from a URL.
	 * @param urlStr The {@link String} url that supplied the response.
	 * @param bytesLoaded The size of the response, in bytes.
	 */
	public void rememberResponse(String urlStr, int responseByteSize) {
		synchronized(hostLastResponse) {
			synchronized(hostLastResponseSize) {
				String host = getHost(urlStr);
				hostLastResponse.put(host, Long.valueOf(new Date().getTime()));
				hostLastResponseSize.put(host, Integer.valueOf(responseByteSize));
			}
		}
	}
	
	/**
	 * Check to see whether a request to <code>urlStr</code> should be delayed,
	 * and hold the thread accordingly.
	 * @param urlStr The {@link String} url whose host's rate should be calculated.
	 * @param log The {@link Logger} to log delay information to.
	 * @throws InterruptedException If the user interrupts during a delay.
	 */
	public void obeyRateLimit(String urlStr, Logger log) throws InterruptedException {
		long now = new Date().getTime();
		String host = getHost(urlStr);
		// Check delay due to repeated requests.
		synchronized(hostLastRequest) {
			if(hostLastRequest.containsKey(host)) {
				synchronized(hostLastRequest.get(host)) {
					long millisecondsSince = now - ((Long) hostLastRequest.get(host)).longValue();
					log.i("Delaying request of " + StringUtils.quote(urlStr) + ", " +
							" last request made " + millisecondsSince + "ms ago.");
					if(millisecondsSince < minRequestWaitMilliseconds) {
						Thread.sleep(DEFAULT_SLEEP_TIME);
						obeyRateLimit(urlStr, log);
					}
				}
			}
		}
		
		// Check delay due to rate limit.
		float rate = 0;
		synchronized(hostLastResponse) {
			synchronized(hostLastResponseSize) {
				if(hostLastResponse.containsKey(host)) {
					long millisecondsSince = now - ((Long) hostLastResponse.get(host)).longValue() + 1;
					int bytesLastLoaded = ((Integer) hostLastResponseSize.get(host)).intValue();
					rate = bytesLastLoaded / millisecondsSince;
				}
				if(rate > rateLimitKBps) {
					log.i("Delaying load of " + StringUtils.quote(urlStr) + ", current KBPS " +
							StringUtils.quote(Float.toString(rate)));
					Thread.sleep(DEFAULT_SLEEP_TIME);
					obeyRateLimit(urlStr, log);
				}
			}
		}
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