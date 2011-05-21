package net.microscraper.model;

import java.io.IOException;
import java.net.URI;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.model.Page.Method.UnknownHTTPMethodException;


/**
 * Class used to request a web page using a browser.
 * @author realest
 *
 */
public final class Page extends Resource {
	
	/**
	 * Static class defining HTTP methods.
	 * @author realest
	 *
	 */
	public static final class Method {
		/**
		 * Static declaration of HTTP Get method.
		 */
		public static final Method GET = new Method();
		
		/**
		 * Static declaration of HTTP Post method.
		 */
		public static final Method POST = new Method();
		
		/**
		 * Static declaration of HTTP Head method.
		 */
		public static final Method HEAD = new Method();
		
		/**
		 * Obtain the static {@link Method} instance from a string.
		 * @param method A lower-case string specifying the HTTP
		 * method.  Either "get", "post", or "head".
		 * @return The static method instance.
		 * @throws IllegalArgumentException if some other string is
		 * passed.
		 */
		public static final Method fromString(String method)
				throws UnknownHTTPMethodException {
			if(method.equals("get")) {
				return GET;
			} else if (method.equals("post")) {
				return POST;
			} else if (method.equals("head")) {
				return HEAD;
			} else {
				throw new UnknownHTTPMethodException(method);
			}
		}
		
		/**
		 * Use only the static Method declarations.
		 */
		private Method() {}
		
		public static class UnknownHTTPMethodException extends Exception {
			public UnknownHTTPMethodException(String method) {
				super("Method '" + method + "' is not recognized, should use 'post', 'cookie', or 'header'.");
			}
		}
	}
	
	public final Method method;
	public final URL url;
	public final Cookie[] cookies;
	public final Header[] headers;
	public final Link[] loadBeforeLinks;
	public final Pattern[] terminates;
	public final Post[] posts;
	
	/**
	 * @param location A {@link URI} that identifies the resource's location.
	 * @param url A {@link URL} to use requesting the page. 
	 * @param cookies An array of {@link Cookie}s to add to the browser before requesting this web page.
	 * @param headers An array of {@link Header}s to add when requesting this web page.
	 * @param loadBeforeLinks An array of {@link Link}s to Pages that should be loaded before loading this page.
	 * @param terminates An array of {@link Pattern}s that terminate the loading of this page.
	 * @param posts An array of {@link Post}s to add to include in the request.
	 * @throws URIMustBeAbsoluteException If the provided location is not absolute.
	 */
	public Page(URI location, Method method, URL url, Cookie[] cookies,
			Header[] headers, Link[] loadBeforeLinks, Pattern[] terminates,
			Post[] posts) throws URIMustBeAbsoluteException {
		super(location);
		this.method = method;
		this.url = url;
		this.cookies = cookies;
		this.headers = headers;
		this.loadBeforeLinks = loadBeforeLinks;
		this.terminates = terminates;
		this.posts = posts;
	}
	
	public URI getLocation() {
		return location;
	}
	
	private static final String METHOD = "method";
	private static final String URL    = "url";
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	private static final String LOAD_BEFORE = "loadBefore";
	private static final String TERMINATES = "terminates";
	private static final String POSTS = "posts";
	
	/**
	 * Deserialize a {@link Page} from a {@link JSONInterfaceObject}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param location {@link URI} from which the resource was loaded.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Page} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a Page.
	 * @throws IOException If one of the prior pages could not be loaded.
	 */
	public static Page deserialize(
				JSONInterface jsonInterface,
				URI location, JSONInterfaceObject jsonObject)
				throws DeserializationException, IOException {
		try {
			Method method = Method.fromString(jsonObject.getString(METHOD));
			URL url = net.microscraper.model.URL.fromString(jsonObject.getString(URL));
			
			Cookie[] cookies = jsonObject.has(COOKIES) ? Cookie.deserializeHash(jsonInterface, jsonObject.getJSONObject(COOKIES)) : new Cookie[0];
			Header[] headers = jsonObject.has(HEADERS) ? Header.deserializeHash(jsonInterface, jsonObject.getJSONObject(HEADERS)) : new Header[0];
			Link[] loadBeforeLinks = jsonObject.has(LOAD_BEFORE) ? Link.deserializeArray(jsonInterface, location, (jsonObject.getJSONArray(LOAD_BEFORE))) : new Link[0];
			Pattern[] terminates  = jsonObject.has(TERMINATES) ? Pattern.deserializeArray(jsonInterface, (jsonObject.getJSONArray(TERMINATES))) : new Pattern[0];
			Post[] posts = jsonObject.has(POSTS) ? Post.deserializeHash(jsonInterface, jsonObject.getJSONObject(POSTS)) : new Post[0];
			
			return new Page(location, method, url, cookies,
					headers, loadBeforeLinks, terminates, posts);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(UnknownHTTPMethodException e) {
			throw new DeserializationException(e.getMessage(), jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
