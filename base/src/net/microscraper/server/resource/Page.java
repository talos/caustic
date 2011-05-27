package net.microscraper.server.resource;

import java.io.IOException;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheNameValuePair;
import net.microscraper.server.resource.Page.Method.UnknownHTTPMethodException;


/**
 * A {@link Scraper} that load a web page.
 * @author realest
 *
 */
public final class Page extends URL {
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
	 * The {@link URL} {@link Resource} to use in constructing the {@link URLInterface} to load.
	 */
	//public final URL url;
	
	/**
	 * The HTTP request type to use.  Either Post, Get, or Head.
	 * Defaults to {@link #DEFAULT_METHOD}
	 */
	public final Method method;
	
	/**
	 * {@link MustacheNameValuePair}s of cookies.
	 */
	public final MustacheNameValuePair[] cookies;
	
	/**
	 * {@link MustacheNameValuePair}s of generic headers.
	 */
	public final MustacheNameValuePair[] headers;
	
	/**
	 * {@link Page} requests to make beforehand. No data is extracted from these pages.
	 */
	public final Page[] preload;
	
	/**
	 * {@link Regexp}s that terminate the loading of this page's body.
	 */
	public final Regexp[] stopBecause;
	
	/**
	 * {@link MustacheNameValuePair}s of post data.
	 */
	public final MustacheNameValuePair[] posts;
	
	/**
	 * Deserialize a {@link Page} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Page} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a {@link Page}.
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Page(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		super(jsonObject);
		try {
			//this.url = new URL(jsonObject);
			
			this.method = jsonObject.has(METHOD) ?
					Method.fromString(jsonObject.getString(METHOD)) : DEFAULT_METHOD;
			
			this.cookies = jsonObject.has(COOKIES) ?
					new NameValuePairs(jsonObject.getJSONObject(COOKIES)).pairs :
					new MustacheNameValuePair[0];
			this.headers = jsonObject.has(HEADERS) ?
					new NameValuePairs(jsonObject.getJSONObject(HEADERS)).pairs :
					new MustacheNameValuePair[0];
					
			if(jsonObject.has(PRELOAD)) {
				JSONInterfaceArray preload = jsonObject.getJSONArray(PRELOAD);
				this.preload = new Page[preload.length()];
				for(int i = 0 ; i < this.preload.length ; i++) {
					this.preload[i] = new Page(preload.getJSONObject(i));
				}
			} else {
				this.preload = new Page[0];
			}
			
			if(jsonObject.has(STOP_BECAUSE)) {
				JSONInterfaceArray stopBecause = jsonObject.getJSONArray(STOP_BECAUSE);
				this.stopBecause = new Regexp[stopBecause.length()];
				for(int i = 0 ; i < this.stopBecause.length ; i++) {
					this.stopBecause[i] = new Regexp(stopBecause.getJSONObject(i));
				}
			} else {
				this.stopBecause = new Regexp[0];
			}
			
			this.posts = jsonObject.has(POSTS) ?
					new NameValuePairs(jsonObject.getJSONObject(POSTS)).pairs :
					new MustacheNameValuePair[0];
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(UnknownHTTPMethodException e) {
			throw new DeserializationException(e.getLocalizedMessage(), jsonObject);
		}
	}
	
	private static final String METHOD = "method";
	
	/**
	 * The default {@link Method} is {@link Method#GET}.
	 */
	private static final Method DEFAULT_METHOD = Method.GET;
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	private static final String PRELOAD = "preload";
	private static final String STOP_BECAUSE = "stop_because";
	private static final String POSTS = "posts";
	
}
