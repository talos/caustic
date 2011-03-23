package net.microscraper.database.schema;

public class WebPage {
	public static final String RESOURCE = "web_page";
	
	public static final String URL = "url";
	public static final String TERMINATES = "terminates";
	public static final String POSTS = "posts";
	public static final String HEADERS  = "headers";
	public static final String COOKIES = "cookies";
	
	public final String url;
	public final Reference[] terminates;
	public final Reference[] posts;
	public final Reference[] headers;
	public final Reference[] cookies;
		
	public WebPage(
				String _url, Reference[] _posts,
				Reference[] _headers, Reference[] _cookies,
				Reference[] _terminates) {
		url = _url;
		terminates = _terminates;
		posts = _posts;
		headers = _headers;
		cookies = _cookies;
	}
	
	public WebPage(String _url) {
		url = _url;
		terminates = new Reference[] { };
		posts = new Reference[] { };
		headers = new Reference[] { };
		cookies = new Reference[] { };
	}
}
