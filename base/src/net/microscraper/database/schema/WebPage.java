package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.Relationship;
import net.microscraper.database.Resource;

public class WebPage  {
	public final String url;
	public final AbstractHeader[] posts;
	public final AbstractHeader[] cookies;
	public final AbstractHeader[] headers;
	public final Regexp[] terminates;
	public WebPage(Resource resource, Variables variables)
				throws MissingVariable, PrematureRevivalException, TemplateException {
		url = Mustache.compile(resource.attribute_get(Model.URL), variables);
		Resource[] _posts = resource.relationship(Model.POSTS);
		posts = new AbstractHeader[_posts.length];
		for(int i = 0; i < _posts.length ; i ++) {
			posts[i] = new AbstractHeader(_posts[i], variables);
		}
		Resource[] _cookies = resource.relationship(Model.COOKIES);
		cookies = new AbstractHeader[_cookies.length];
		for(int i = 0; i < _cookies.length ; i ++) {
			cookies[i] = new AbstractHeader(_cookies[i], variables);
		}
		Resource[] _headers = resource.relationship(Model.HEADERS);
		headers = new AbstractHeader[_headers.length];
		for(int i = 0; i < _headers.length ; i ++) {
			headers[i] = new AbstractHeader(_headers[i], variables);
		}
		Resource[] _terminates = resource.relationship(Model.TERMINATES);
		terminates = new Regexp[_terminates.length];
		for(int i = 0; i < _terminates.length ; i ++) {
			terminates[i] = new Regexp(_terminates[i], variables);
		}
	}
	
	/**
	 * This creates a one-off WebPage resource for loading a page with the browser.
	 * @param url
	 * @return A web page with the specified URL and no headers.
	 */
	public WebPage(String _url) {
		url = _url;
		posts = new AbstractHeader[] {};
		headers = new AbstractHeader[] {};
		cookies = new AbstractHeader[] {};
		terminates = new Regexp[] {};
	}
	
	public static class Model extends AbstractModel {
		public static final String KEY = "web_page";
	
		public static final String URL = "url";
		public static final String[] ATTRIBUTES = { URL };
		
		public static final String TERMINATES = "terminates";
		public static final String POSTS = "posts";
		public static final String HEADERS = "headers";
		public static final String COOKIES = "cookies";
		public final Relationship terminates = new Relationship( TERMINATES, new Regexp.Model());
		public final Relationship posts = new Relationship( POSTS, new AbstractHeader.Post.Model());
		public final Relationship headers = new Relationship( HEADERS, new AbstractHeader.Header.Model());
		public final Relationship cookies = new Relationship( COOKIES, new AbstractHeader.Cookie.Model());
		public final Relationship[] relationships =
				{ terminates, posts, headers, cookies };
		
		protected String _key() { return KEY; }
		protected String[] _attributes() { return ATTRIBUTES; }
		protected Relationship[] _relationships() { return relationships; }
		
	}
}
