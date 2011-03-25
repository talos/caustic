package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractModel;
import net.microscraper.database.Reference;
import net.microscraper.database.Relationship;

public class WebPage extends AbstractModel {
	public static String KEY = "web_page";

	public static String URL = "url";
	public static String[] ATTRIBUTES = { URL };
	
	public static Relationship TERMINATES = new Relationship( "terminates", new Regexp());
	public static Relationship POSTS = new Relationship( "posts", new AbstractHeader.Post());
	public static Relationship HEADERS = new Relationship( "headers", new AbstractHeader.Header());
	public static Relationship COOKIES = new Relationship( "cookies", new AbstractHeader.Cookie());
	public static Relationship[] RELATIONSHIPS =
			{ TERMINATES, POSTS, HEADERS, COOKIES };
	
	protected String _key() { return KEY; }
	protected String[] _attributes() { return ATTRIBUTES; }
	protected Relationship[] _relationships() { return RELATIONSHIPS; }
	
	public Resource resource(Reference ref, Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		return new Resource(ref, json_obj);
	}
	
	public Resource forURL(String url) {
		Hashtable _attributes = new Hashtable();
		_attributes.put(URL, url);
		return new Resource(Reference.blank(), _attributes);
	}
	
	public class Resource extends AbstractResource {
		public Resource(Reference ref, Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
			super(_model, ref, json_obj);
		}
		
		public Resource(Reference ref, Hashtable _attributes) {
			super(_model, ref, _attributes);
		}
	}
}
