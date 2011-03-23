package net.microscraper.client;

import java.util.Hashtable;

public class Collection {

	public final Hashtable datas;
	public final Hashtable posts;
	public final Hashtable cookies;
	public final Hashtable headers;
	public final Hashtable regexps;
	public final Hashtable web_pages;
	public final Hashtable defaults;
	
	public Collection(Hashtable _datas, Hashtable _posts, Hashtable _cookies,
			Hashtable _headers, Hashtable _regexps, Hashtable _web_pages,
			Hashtable _defaults) {
		datas = _datas;
		posts = _posts;
		cookies = _cookies;
		headers = _headers;
		regexps = _regexps;
		web_pages = _web_pages;
		defaults = _defaults;
	}
}
