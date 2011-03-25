package net.microscraper.database.schema;

import net.microscraper.database.AbstractModel;
import net.microscraper.database.Relationship;

public class WebPage  {

	/**
	 * This creates a one-off WebPage resource for loading a random page with the browser.
	 * @param url
	 * @return A web page with the specified URL and no headers.
	 */
	/*public Resource forURL(String url) {
		Hashtable attributes = new Hashtable();
		attributes.put(URL, url);
		return new Resource(attributes);
	}*/
	
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
