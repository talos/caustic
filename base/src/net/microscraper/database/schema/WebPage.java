package net.microscraper.database.schema;

public class WebPage {
	public final String url;
	
	public final Regexp[] terminates;
	public final Post[] posts;
	public final Header[] headers;
	public final Cookie[] cookies;
		
	public WebPage(
				String _url, Post[] _posts,
				Header[] _headers, Cookie[] _cookies,
				Regexp[] _terminates) {
		url = _url;
		terminates = _terminates;
		posts = _posts;
		headers = _headers;
		cookies = _cookies;
	}
	
	public WebPage(String _url) {
		url = _url;
		terminates = new Regexp[] { };
		posts = new Post[] { };
		headers = new Header[] { };
		cookies = new Cookie[] { };
	}
}
