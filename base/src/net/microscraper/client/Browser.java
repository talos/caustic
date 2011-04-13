package net.microscraper.client;

import net.microscraper.database.schema.AbstractHeader;
import net.microscraper.database.schema.Cookie;
import net.microscraper.database.schema.Header;
import net.microscraper.database.schema.Post;

public interface Browser {
	public String load(String url) throws InterruptedException, BrowserException;
	public String load(String url, Post[] posts, Header[] headers, Cookie[] cookies) throws InterruptedException, BrowserException;
	
	public static final AbstractHeader[] DEFAULT_HEADERS = new AbstractHeader[] {
		new Header("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)"),
		new Header("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
	};
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public static class BrowserException extends Exception {
		public BrowserException(String message) {
			super(message);
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = -849994574375042801L;
		
	}
}
