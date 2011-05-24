package net.microscraper.server.resource;

import java.io.IOException;

import net.microscraper.client.executable.Cookie;
import net.microscraper.client.executable.Header;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.Ref;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.Page.Method.UnknownHTTPMethodException;


/**
 * A web page.
 * @author realest
 *
 */
public final class Page extends Resource {
	/**
	 * The resource's identifier when deserializing.
	 */
	public static final String key = "page";
	
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
	
	/**
	 * The HTTP request type to use.  Either Post, Get, or Head.
	 */
	public final Method method;
	
	/**
	 * The requested URL, which is mustached.
	 */
	public final URL url;
	
	/**
	 * {@link NameValuePairs} pairs of cookies.
	 */
	public final NameValuePairs cookies;
	
	/**
	 * {@link NameValuePairs} of generic headers.
	 */
	public final NameValuePairs headers;
	
	/**
	 * {@link Page} requests to make beforehand. No data is extracted from these pages.
	 */
	public final Page[] preload;
	
	/**
	 * {@link Regexp}s that terminate the loading of this page's body.
	 */
	public final Regexp[] stopBecause;
	
	/**
	 * {@link NameValuePairs} of post data.
	 */
	public final NameValuePairs posts;
	
	/**
	 * @param location A {@link URIInterface} that identifies the resource's location.
	 * @param url A {@link URL} to use requesting the page. 
	 * @param cookies A {@link NameValuePair} to add to the browser before requesting this web page.
	 * @param headers A {@link NameValuePair} to add when requesting this web page.
	 * @param preload An array of {@link Page}s that should be loaded before loading this page.
	 * @param stopBecause An array of {@link Regexp}s that terminate the loading of this page.
	 * @param posts A {@link NameValuePair} to add to include in the request.
	 * @throws URIMustBeAbsoluteException If the provided location is not absolute.
	 */
	public Page(URIInterface location, Method method, URL url, NameValuePairs cookies,
			NameValuePairs headers, Page[] preload, Regexp[] stopBecause,
			NameValuePairs posts) throws URIMustBeAbsoluteException {
		super(location);
		this.method = method;
		this.url = url;
		this.cookies = cookies;
		this.headers = headers;
		this.preload = preload;
		this.stopBecause = stopBecause;
		this.posts = posts;
	}
	
	private static final String METHOD = "method";
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	private static final String PRELOAD = "preload";
	private static final String STOP_BECAUSE = "stop_because";
	private static final String POSTS = "posts";
	
	/**
	 * Deserialize a {@link Page} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Page} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Page}.
	 * @throws IOException If one of the {@link #preload} {@link Page}s could not be loaded.
	 */
	public static Page deserialize(JSONInterfaceObject jsonObject)
				throws DeserializationException, IOException {
		try {
			Method method = Method.fromString(jsonObject.getString(METHOD));
			URL url = URL.deserialize(jsonObject);
			
			NameValuePairs cookies = NameValuePairs.deserialize(jsonObject.getJSONObject(COOKIES));
			NameValuePairs headers = NameValuePairs.deserialize(jsonObject.getJSONObject(HEADERS));
			Page[] preload = Page.deserializeArray(jsonObject, jsonObject.getJSONArray(PRELOAD));
			Regexp[] stopBecause = Regexp.deserializeArray(jsonObject.getJSONArray(STOP_BECAUSE));
			NameValuePairs posts = NameValuePairs.deserialize(jsonObject.getJSONObject(POSTS));
			
			//URL url = net.microscraper.server.resource.URL.fromString(jsonObject.getString(URL));
			
			/*Cookie[] cookies = jsonObject.has(COOKIES) ? Cookie.deserializeHash(jsonObject.getJSONObject(COOKIES)) : new Cookie[0];
			Header[] headers = jsonObject.has(HEADERS) ? Header.deserializeHash(jsonInterface, jsonObject.getJSONObject(HEADERS)) : new Header[0];
			Ref[] loadBeforeLinks = jsonObject.has(LOAD_BEFORE) ? Ref.deserializeArray(jsonInterface, location, (jsonObject.getJSONArray(LOAD_BEFORE))) : new Ref[0];
			Regexp[] terminates  = jsonObject.has(TERMINATES) ? Regexp.deserializeArray(jsonInterface, (jsonObject.getJSONArray(TERMINATES))) : new Regexp[0];
			Post[] posts = jsonObject.has(POSTS) ? Post.deserializeHash(jsonInterface, jsonObject.getJSONObject(POSTS)) : new Post[0];
			*/
			return new Page(root.resolve(path), method, url, cookies,
					headers, preload, stopBecause, posts);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(UnknownHTTPMethodException e) {
			throw new DeserializationException(e.getMessage(), jsonObject);
		} catch(URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
}
