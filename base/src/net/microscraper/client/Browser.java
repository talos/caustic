package net.microscraper.client;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.schema.AbstractHeader;

public interface Browser {
	public static final int TIMEOUT = 5000;
	public static final int MAX_REDIRECTS = 50;
	public static final int SUCCESS_CODE = 200;
	public static final String LOCATION_HEADER_NAME = "location";
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public String load(String url, AbstractResult caller)
			throws InterruptedException, BrowserException, ResourceNotFoundException, TemplateException, MissingVariable;
	public String load(String url, AbstractResource[] posts, AbstractResource[] headers,
			AbstractResource[] cookies, AbstractResource[] terminates, AbstractResult caller)
			throws BrowserException, ResourceNotFoundException, TemplateException, MissingVariable, InterruptedException;
	
	public static final AbstractHeader[] DEFAULT_HEADERS = new AbstractHeader[] {
		new AbstractHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)"),
		new AbstractHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
	};
	
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
