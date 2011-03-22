package net.microscraper.client;


import net.microscraper.database.schema.WebPage;

public interface Browser {
	public String load(WebPage web_page) throws InterruptedException;
	
	/**
	 * An equivalent interface to CookieStore, implemented by the network
	 * library (org.apache.* etc.) of choice.  It is recommended that implementations
	 * be synchronized, as each Gatherer (as opposed to each Information-Gatherer combo)
	 * has its own GeograpeCookieStore.
	 * @author john
	 *
	 */
	/*
	public static interface CookieStoreInterface {
		public abstract CookieInterface[] getCookies();
		public abstract void addCookies(CookieInterface[] cookies);
	}
	*/
	/**
	 * An equivalent interface to Cookie, implemented by the network library
	 * (org.apache.* etc.) of choice.
	 * @author john
	 *
	 */
	/*
	public static interface CookieInterface {
		public abstract String getDomain();
		public abstract Date getExpiryDate();
		public abstract String getName();
		public abstract String getPath();
		public abstract String getValue();
		
	}
	
	public final static class Cookie implements CookieInterface {
		private final String domain;
		private final Date expiryDate = null;
		private final String name;
		private final String path;
		private final String value;
		public Cookie(String url, String n, String v) {
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
	*/
}
