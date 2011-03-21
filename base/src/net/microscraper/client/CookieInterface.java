/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package net.microscraper.client;

import java.util.Date;

/**
 * An equivalent interface to Cookie, implemented by the network library
 * (org.apache.* etc.) of choice.
 * @author john
 *
 */
public interface CookieInterface {
	public abstract String getDomain();
	public abstract Date getExpiryDate();
	public abstract String getName();
	public abstract String getPath();
	public abstract String getValue();
	
	
	/**
	 * An implementation of GeograpeCookie created from within the Gatherer.
	 * @author john
	 *
	 */
	public final static class ScraperCookie implements CookieInterface {
		private final String domain;
		private final Date expiryDate = null;
		private final String name;
		private final String path;
		private final String value;
		public ScraperCookie(String url, String n, String v) {
			// First, eliminate any method. ("http://")
			int split;
			if((split = url.indexOf("://")) != -1) {
				url = url.substring(split + 3);
			}
			// Next, eliminate any "www" subdomain.
			if(url.startsWith("www")) {
				url = url.substring(3);
			}
			// Next, eliminate the path (including the slash).
			if((split = url.indexOf("/")) != -1) {
				url = url.substring(0, split);
			}
			
			domain = url;
			path = "/";
			name = n;
			value = v;
		}
		public String getDomain() { return domain; }
		public Date getExpiryDate() { return expiryDate; }
		public String getName() { return name; }
		public String getPath() { return path; }
		public String getValue() { return value; }
	}
}
