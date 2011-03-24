package net.microscraper.database.schema;

public class Resource {
	public final Reference ref;
	Resource(Reference reference) {
		ref = reference;
	}

	public static class Cookie extends Resource {
		public final static String RESOURCE = "cookie";
		public final static String NAME = "name";
		public final static String VALUE = "value";
		
		public final String name;
		public final String value;
		
		public Cookie(Reference reference, String _name, String _value) {
			super(reference);
			name = _name;
			value = _value;
		}
	}
	
	public static class Data extends Resource {	
		public final static String RESOURCE = "data";
		
		public final static String SCRAPERS = "scrapers";
		public final static String DEFAULTS = "defaults";
		
		public final Reference[] scrapers;
		public final Reference[] defaults;
		
		public Data(Reference reference, Reference[] _scrapers, Reference[] _defaults) {
			super(reference);
			scrapers = _scrapers;
			defaults = _defaults;
		}
	}
	
	public static class Default extends Resource {
		public final static String RESOURCE = "default";
		public final static String VALUE = "value";
		public final static String SUBSTITUTES_FOR = "substitutes_for";
		
		public final String value;
		public final Reference[] substitutes_for;
		public Default(Reference reference, String _value, Reference[] _substitutes_for) {
			super(reference);
			value = _value;
			substitutes_for = _substitutes_for;
		}
	}

	public static class Header extends Resource {
		public final static String RESOURCE	 = "header";
		public final static String NAME = "name";
		public final static String VALUE  = "value";
		
		public final String name;
		public final String value;
		
		public Header(Reference reference, String _name, String _value) {
			super(reference);
			name = _name;
			value = _value;
		}
	}

	public class Post extends Resource {
		public final static String RESOURCE = "post";
		public final static String NAME = "name";
		public final static String VALUE = "value";
		
		public final String name;
		public final String value;
		
		public Post(Reference reference, String _name, String _value) {
			super(reference);
			name = _name;
			value = _value;
		}
	}
	
	public class Regexp extends Resource {
		public final static String RESOURCE = "regexp";
		public final static String REGEXP = "regexp";

		public final String regexp;
		
		public Regexp(Reference reference, String _regexp) {
			super(reference);
			regexp = _regexp;
		}
	}
	
	public final class Scraper extends Resource {	
		public final static String RESOURCE = "scraper";
		
		public final static String REGEXP = "regexp";
		public final static String MATCH_NUMBER = "match_number";
		public final static String WEB_PAGES = "web_pages";
		public final static String SOURCE_SCRAPERS = "source_scrapers";
		
		public final String regexp;
		public final Integer match_number;
		public final Reference[] web_pages;
		public final Reference[] source_scrapers;
		
		public Scraper(Reference reference, String _regexp, Integer _match_number,
				Reference[] _web_pages, Reference[] _source_scrapers) {
			super(reference);
			regexp = _regexp;
			match_number = _match_number;
			web_pages = _web_pages;
			source_scrapers = _source_scrapers;
		}
	}
	
	public class WebPage extends Resource {
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
		
		public WebPage(Reference reference, 
					String _url, Reference[] _posts,
					Reference[] _headers, Reference[] _cookies,
					Reference[] _terminates) {
			super(reference);
			url = _url;
			terminates = _terminates;
			posts = _posts;
			headers = _headers;
			cookies = _cookies;
		}
		
		public WebPage(Reference reference, String _url) {
			super(reference);
			url = _url;
			terminates = new Reference[] { };
			posts = new Reference[] { };
			headers = new Reference[] { };
			cookies = new Reference[] { };
		}
	}
}
