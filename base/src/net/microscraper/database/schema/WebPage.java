package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.Relationships.Relationship;
import net.microscraper.database.Resource;
import net.microscraper.database.schema.AbstractHeader.Cookie;
import net.microscraper.database.schema.AbstractHeader.Header;
import net.microscraper.database.schema.AbstractHeader.Post;

public class WebPage  {
	public final String url;
	public final AbstractHeader[] posts;
	public final AbstractHeader[] cookies;
	public final AbstractHeader[] headers;
	public final Pattern[] terminates;
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
		terminates = new Pattern[_terminates.length];
		for(int i = 0; i < _terminates.length ; i ++) {
			terminates[i] = new Regexp(_terminates[i], variables).pattern;
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
		terminates = new Pattern[] {};
	}
	
	/**
	 * Test for equality against another web page.  Considered 'equal' if the URL and all
	 * headers/posts/cookies/terminates are identical.
	 * @param web_page
	 * @return
	 */
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof AbstractHeader))
			return false;
		WebPage other = (WebPage) obj;
		if(!this.url.equals(other.url))
			return false;
		if(!Utils.arraysEqual(this.posts, other.posts))
			return false;
		if(!Utils.arraysEqual(this.headers, other.headers))
			return false;
		if(!Utils.arraysEqual(this.cookies, other.cookies))
			return false;
		if(!Utils.arraysEqual(this.terminates, other.terminates))
			return false;
		return true;
	}
	
	public static class Model implements ModelDefinition {
		public static final String KEY = "web_page";
	
		public static final String URL = "url";
		
		public static final String TERMINATES = "terminates";
		public static final String POSTS = "posts";
		public static final String HEADERS = "headers";
		public static final String COOKIES = "cookies";
					
		public String key() { return KEY; }
		public String[] attributes() { return new String[] { URL }; }
		public Relationship[] relationships() {
			return new Relationship[] {
				new Relationship( TERMINATES, Regexp.Model.KEY ),
				new Relationship( POSTS, Post.Model.KEY ),
				new Relationship( HEADERS, Header.Model.KEY ),
				new Relationship( COOKIES, Cookie.Model.KEY )
			};
		}
	}
}
