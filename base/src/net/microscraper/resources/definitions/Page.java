package net.microscraper.resources.definitions;

import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;


/**
 * Class used to request a web page using a browser.
 * @author realest
 *
 */
public final class Page implements Resource {
	
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
		 * @throws DeserializationException if some other string is
		 * passed.
		 */
		public static final Method fromString(String method)
				throws DeserializationException {
			if(method.equals("get")) {
				return GET;
			} else if (method.equals("post")) {
				return POST;
			} else if (method.equals("head")) {
				return HEAD;
			} else {
				throw new DeserializationException("Method '" + method + "' is not recognized, should use 'post', 'cookie', or 'header'.");
			}
		}
		
		/**
		 * Use only the static Method declarations.
		 */
		private Method() {}
	}
	
	private final URI location;
	public final Method method;
	public final URL url;
	public final Cookie[] cookies;
	public final Header[] headers;
	public final Links loadBefore;
	public final Links terminate;
	public final Post[] posts;
	
	/**
	 * @param location A {@link URI} that identifies the resource's location.
	 * @param url A {@link URL} to use requesting the page. 
	 * @param cookies An array of {@link Cookie}s to add to the browser before requesting this web page.
	 * @param headers An array of {@link Header}s to add when requesting this web page.
	 * @param loadBefore {@link Links} to Pages that should be loaded before loading this page.
	 * @param terminate {@link Links} to Parsers that terminate the loading of this page.
	 * @param posts An array of {@link Post}s to add to include in the request.
	 */
	public Page(URI location, Method method, URL url, Cookie[] cookies,
			Header[] headers, Links loadBefore, Links terminate,
			Post[] posts) {
		this.location = location;
		this.method = method;
		this.url = url;
		this.cookies = cookies;
		this.headers = headers;
		this.loadBefore = loadBefore;
		this.terminate = terminate;
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
	private static final String TERMINATE = "terminate";
	private static final String POSTS = "posts";
	
	/**
	 * Deserialize a {@link Page} from a {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param location {@link URI} from which the resource was loaded.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return A {@link Page} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a Page.
	 */
	public static Page deserialize(Interfaces.JSON jsonInterface,
				URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			Method method = Method.fromString(jsonObject.getString(METHOD));
			URL url = net.microscraper.resources.definitions.URL.fromString(jsonObject.getString(URL));
			
			Cookie[] cookies = jsonObject.has(COOKIES) ? Cookie.deserializeHash(jsonInterface, jsonObject.getJSONObject(COOKIES)) : new Cookie[0];
			Header[] headers = jsonObject.has(HEADERS) ? Header.deserializeHash(jsonInterface, jsonObject.getJSONObject(HEADERS)) : new Header[0];
			Links loadBefore = jsonObject.has(LOAD_BEFORE) ? Links.deserializeArray(jsonInterface, (jsonObject.getJSONArray(LOAD_BEFORE))) : Links.blank();
			Links terminate  = jsonObject.has(TERMINATE) ? Links.deserializeArray(jsonInterface, (jsonObject.getJSONArray(TERMINATE))) : Links.blank();
			Post[] posts = jsonObject.has(POSTS) ? Post.deserializeHash(jsonInterface, jsonObject.getJSONObject(POSTS)) : new Post[0];
			
			return new Page(location, method, url, cookies,
					headers, loadBefore, terminate, posts);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e);
		}
	}
	
	/*
	protected java.net.URL generateURL(Scraper context) throws ScrapingDelay, ScrapingFatality {
		return url.getURL(context);
	}
	
	protected UnencodedNameValuePair[] generateHeaders(Scraper context)
				throws ScrapingDelay, ScrapingFatality {
		UnencodedNameValuePair[] headersAry = new UnencodedNameValuePair[this.headers.length];
		for(int i = 0 ; i < this.headers.length ; i ++) {
			headersAry[i] = headers[i].getNameValuePair(context);
		}
		return headersAry;
	}
	
	protected EncodedNameValuePair[] generateEncodedNameValuePairs(
				Scraper context, EncodedHeader[] encodedHeaders)
				throws ScrapingDelay, ScrapingFatality {
		try {
			EncodedNameValuePair[] nameValuePairs = new EncodedNameValuePair[encodedHeaders.length];
			for(int i = 0 ; i < nameValuePairs.length ; i ++) {
				nameValuePairs[i] = encodedHeaders[i].getNameValuePair(context);
			}
			return nameValuePairs;
		} catch (UnsupportedEncodingException e) {
			throw new ScrapingFatality(e, this);
		}
	}
	
	protected EncodedNameValuePair[] generateCookies(Scraper context) throws ScrapingDelay, ScrapingFatality {
		return generateEncodedNameValuePairs(context, cookies);
	}
	
	protected void headPriorWebPages(Scraper context) throws ScrapingDelay, ScrapingFatality {
		for(int i = 0 ; i < priorWebPages.length ; i ++) {
			priorWebPages[i].headUsing(context);
		}
	}
	*/
	/**
	 * Send an HTTP Head for the web page.  This will add cookies to the browser.
	 * @param browser the browser to use.
	 * @throws ScrapingFatality 
	 * @throws ScrapingFailure 
	 * @throws ScrapingDelay 
	 * @throws BrowserException 
	 * @throws DelayRequest 
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	/*
	public void headUsing(Scraper context) throws ScrapingDelay, ScrapingFatality {
		try {
			UnencodedNameValuePair[] headers = generateHeaders(context);
			EncodedNameValuePair[] cookies;
			cookies = generateCookies(context);
			
			headPriorWebPages(context);
			
			context.getBrowser().head(generateURL(context), headers, cookies);
		} catch (DelayRequest e) {
			throw new ScrapingDelay(e, this);
		} catch (BrowserException e) {
			throw new ScrapingFatality(e, this);
		}
	}
	
	public String getName() {
		return url.getName();
	}*/
}
