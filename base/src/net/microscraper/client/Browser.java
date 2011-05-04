package net.microscraper.client;

import java.util.Date;
import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Regexp.Pattern;

public interface Browser {
	public static final int TIMEOUT = 10000;
	public static final int MAX_REDIRECTS = 50;
	public static final int SUCCESS_CODE = 200;
	public static final int MAX_KBPS_FROM_HOST = 5;
	
	public static final String LOCATION_HEADER_NAME = "location";
	public static final String REFERER_HEADER_NAME = "Referer";
	
	public static final String USER_AGENT_HEADER_NAME = "User-Agent";
	public static final String USER_AGENT_HEADER_DEFAULT_VALUE = "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)";
	
	public static final String ACCEPT_LANGUAGE_HEADER_NAME = "Accept-Language";
	public static final String ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE = "en-US,en;q=0.8";
	
	public static final String ACCEPT_HEADER_NAME = "Accept";
	public static final String ACCEPT_HEADER_DEFAULT_VALUE = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	public static final String ACCEPT_HEADER_JSON_VALUE = "application/json,text/json";
	
	public static final String ENCODING = "UTF-8";
	
	public JSON.Object loadJSON(String url, Interfaces.JSON jsonInterface) throws InterruptedException, BrowserException, JSONInterfaceException;
	public String load(String url, Hashtable posts, Hashtable headers,
			Hashtable cookies, Pattern[] terminates) throws WaitToDownload, BrowserException, InterruptedException;
	
	public static class BrowserException extends Exception {
		public BrowserException(String url, Throwable e) {
			super("Loading " + url, e);
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = -849994574375042801L;
		
	}
	
	public static class WaitToDownload extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8357347717343426486L;
		public final String host;
		public final Date start;
		public final int amountDownloaded;
		
		public WaitToDownload(String host, Date start, int amountDownloaded) {
			this.host = host;
			this.start = start;
			this.amountDownloaded = amountDownloaded;
		}
	}
}
