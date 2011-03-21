package net.microscraper.client;

import java.util.Vector;

public class WebPage {
	public final String name;

	private final String url;
	
	private final Vector terminates = new Vector();
	private final Vector posts = new Vector();
	private final Vector headers = new Vector();
	private final Vector cookies = new Vector();
	private final Hashtable results = new Hashtable();
	
	private final Browser browser;
	
	public WebPage(String _name, Regexp[] _terminates,
				String _url, String[] _posts,
				String[] _headers, String[] _cookies,
				Browser _browser) {
		name = _name;
		url = _url;
		Utils.arrayIntoVector(_terminates, terminates);
		Utils.arrayIntoVector(_posts, posts);
		Utils.arrayIntoVector(_headers, headers);
		Utils.arrayIntoVector(_cookies, cookies);
		browser = _browser;
	}
	
	public String load(Data.Results results) {
		
	}
}
