package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplate;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

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
				throws IllegalArgumentException {
			if(method.equals("get")) {
				return GET;
			} else if (method.equals("post")) {
				return POST;
			} else if (method.equals("head")) {
				return HEAD;
			} else {
				throw new IllegalArgumentException("Method '" + method + "' is not recognized, should use 'post', 'cookie', or 'header'.");
			}
		}
		
		/**
		 * Use only the static Method declarations.
		 */
		private Method() {}
	}
		
	private final Method method;
	/**
	 * @return The HTTP request type to use.  Either Post, Get, or Head.
	 * Defaults to {@link #DEFAULT_METHOD}
	 */
	public final Method getMethod() {
		return method;
	}
	
	private final MustacheNameValuePair[] cookies;
	/**
	 * @return {@link MustacheNameValuePair}s of cookies.
	 */
	public final MustacheNameValuePair[] getCookies() {
		return cookies;
	}
	
	private final MustacheNameValuePair[] headers;
	/**
	 * @return {@link MustacheNameValuePair}s of generic headers.
	 */
	public final MustacheNameValuePair[] getHeaders() {
		return headers;
	}
	
	private final Page[] preload;
	/**
	 * @return {@link Page} requests to make beforehand. No data is extracted from these pages.
	 */
	public final Page[] getPreload() {
		return preload;
	}
	
	private final Regexp[] stopBecause;
	/**
	 * @return {@link Regexp}s that terminate the loading of this page's body.
	 */
	public final Regexp[] getStopBecause() {
		return stopBecause;
	}
	
	private final MustacheNameValuePair[] posts;
	/**
	 * @return {@link MustacheNameValuePair}s of post data.
	 */
	public final MustacheNameValuePair[] getPosts() {
		return posts;
	}
	
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
			try {
				this.method = jsonObject.has(METHOD) ?
					Method.fromString(jsonObject.getString(METHOD)) : DEFAULT_METHOD;
			} catch(IllegalArgumentException e) {
				throw new DeserializationException(e, jsonObject);
			}
			
			this.cookies = jsonObject.has(COOKIES) ?
					NameValuePairs.deserialize(jsonObject.getJSONObject(COOKIES)) :
					DEFAULT_COOKIES;
			this.headers = jsonObject.has(HEADERS) ?
					NameValuePairs.deserialize(jsonObject.getJSONObject(HEADERS)) :
					DEFAULT_HEADERS;
					
			if(jsonObject.has(PRELOAD)) {
				JSONInterfaceArray preload = jsonObject.getJSONArray(PRELOAD);
				this.preload = new Page[preload.length()];
				for(int i = 0 ; i < this.preload.length ; i++) {
					this.preload[i] = new Page(preload.getJSONObject(i));
				}
			} else {
				this.preload = DEFAULT_PRELOAD;
			}
			
			if(jsonObject.has(STOP_BECAUSE)) {
				JSONInterfaceArray stopBecause = jsonObject.getJSONArray(STOP_BECAUSE);
				this.stopBecause = new Regexp[stopBecause.length()];
				for(int i = 0 ; i < this.stopBecause.length ; i++) {
					this.stopBecause[i] = new Regexp(stopBecause.getJSONObject(i));
				}
			} else {
				this.stopBecause = new Regexp[] {};
			}
			
			this.posts = jsonObject.has(POSTS) ?
					NameValuePairs.deserialize(jsonObject.getJSONObject(POSTS)) :
					new MustacheNameValuePair[] {};
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	public Page(JSONLocation location, MustacheTemplate name, 
			Page[] spawnPages, Scraper[] spawnScrapers,
			FindMany[] findManys, FindOne[] findOnes, MustacheTemplate urlTemplate,
			Method method, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] posts, MustacheNameValuePair[] cookies,
			Regexp[] stopBecause, Page[] preload) {
		super(location, name, spawnPages, spawnScrapers, findManys, findOnes, urlTemplate);
		this.method = method;
		this.headers = headers;
		this.posts = posts;
		this.cookies = cookies;
		this.stopBecause = stopBecause;
		this.preload = preload;
	}
	
	/**
	 * Key for {@link #getMethod()} when deserializing. Default is {@link #DEFAULT_METHOD}.
	 */
	public static final String METHOD = "method";
	
	/**
	 * The default {@link Method} is {@link Method#GET}.
	 */
	public static final Method DEFAULT_METHOD = Method.GET;
	
	/**
	 * Key for {@link #getCookies()} when deserializing. Default is {@link #DEFAULT_COOKIES}.
	 */
	public static final String COOKIES = "cookies";
	
	/**
	 * Default value for {@link #getCookies()}.
	 */
	public static final MustacheNameValuePair[] DEFAULT_COOKIES = new MustacheNameValuePair[] {};
	
	/**
	 * Key for {@link #getHeaders()} when deserializing. Default is {@link #DEFAULT_HEADERS}.
	 */
	public static final String HEADERS = "headers";

	/**
	 * Default value for {@link #getHeaders()}.
	 */
	public static final MustacheNameValuePair[] DEFAULT_HEADERS = new MustacheNameValuePair[] {};
	
	/**
	 * Key for {@link #getPreload()} when deserializing. Default is {@link #DEFAULT_PRELOAD}.
	 */
	public static final String PRELOAD = "preload";

	/**
	 * Default value for {@link #getPreload()}.
	 */
	public static final Page[] DEFAULT_PRELOAD = new Page[] {};
	
	/**
	 * Key for {@link #getStopBecause()} when deserializing. Default is {@link #DEFAULT_STOP_BECAUSE}.
	 */
	public static final String STOP_BECAUSE = "stop_because";
	
	/**
	 * Default value for {@link #getStopBecause()}.
	 */
	public static final Regexp[] DEFAULT_STOP_BECAUSE = new Regexp[] {};
	
	/**
	 * Key for {@link #getPosts()} when deserializing. Default is {@link #DEFAULT_POSTS}.
	 */
	public static final String POSTS = "posts";

	/**
	 * Default value for {@link #getPosts()}.
	 */
	public static final MustacheNameValuePair[] DEFAULT_POSTS = new MustacheNameValuePair[] {};
}
