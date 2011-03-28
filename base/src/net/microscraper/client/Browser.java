package net.microscraper.client;

import net.microscraper.database.schema.AbstractHeader;
import net.microscraper.database.schema.WebPage;

public interface Browser {
	public String load(WebPage web_page) throws InterruptedException, BrowserException;
	
	public static final AbstractHeader[] DEFAULT_HEADERS = new AbstractHeader[] {
		new AbstractHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)"),
		new AbstractHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
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
