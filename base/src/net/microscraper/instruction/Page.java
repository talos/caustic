package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplate;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * A {@link Scraper} that load a web page.
 * @author realest
 *
 */
public final class Page extends Instruction {
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

	/**
	 * The HTTP request type to use.  Either {@link Method#GET},
	 * {@link Method#POST}, or {@link Method#HEAD}.
	 * Defaults to {@link #DEFAULT_METHOD}
	 */
	private final Method method;
	
	/**
	 * @return The default {@link Method} when one is not explicitly defined.
	 * This is {@link Method#GET} if there is not a {@link #POSTS} key, and
	 * {@link Method#POST} if there is.
	 * @param jsonObject The {@link JSONInterfaceObject} being deserialized.
	 * @return {@link Method#GET} or {@link Method#POST}.
	 */
	private final Method getDefaultMethod(JSONInterfaceObject jsonObject) {
		if(jsonObject.has(POSTS)) {
			return Method.POST;
		} else {
			return Method.GET;
		}
	}

	/**
	 * {@link MustacheNameValuePair}s of cookies.
	 */
	private final MustacheNameValuePair[] cookies;

	/**
	 * {@link MustacheNameValuePair}s of generic headers.
	 */
	private final MustacheNameValuePair[] headers;

	/**
	 * {@link Page} requests to make beforehand. No data is extracted from these pages.
	 */
	private final Page[] preload;

	/**
	 * {@link Regexp}s that terminate the loading of this page's body.
	 */
	private final Regexp[] stopBecause;
	
	/**
	 * A {@link MustacheTemplate} of post data.  Exclusive of {@link #postNameValuePairs}.
	 */
	private final MustacheTemplate postData;
	
	/**
	 * {@link MustacheNameValuePair}s of post data.  Exclusive of {@link #postData}.
	 */
	private final MustacheNameValuePair[] postNameValuePairs;

	/**
	 * A string that can be mustached and used as a URL.
	 */
	private final MustacheTemplate url;

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

			this.url = new MustacheTemplate(jsonObject.getString(URL));
			
			try {
				this.method = jsonObject.has(METHOD) ?
					Method.fromString(jsonObject.getString(METHOD)) : getDefaultMethod(jsonObject);
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
			
			if(jsonObject.has(POSTS)) {
				if(jsonObject.isJSONObject(POSTS)) {
					this.postData = null;
					this.postNameValuePairs = NameValuePairs.deserialize(jsonObject.getJSONObject(POSTS));
				} else {
					this.postData = new MustacheTemplate(jsonObject.getString(POSTS));
					this.postNameValuePairs = null;
				}
			} else {
				this.postData = null;
				this.postNameValuePairs = null;
			}
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheTemplateException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	/**
	 * Key for {@link #getMethod()} when deserializing. Default is {@link #DEFAULT_METHOD},
	 */
	public static final String METHOD = "method";
	
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
	 * Key for {@link #getPosts()} when deserializing. Default is {@link #DEFAULT_POSTS}.
	 */
	public static final String URL = "url";

	/**
	 * Default value for {@link #getPosts()}.
	 */
	public static final MustacheNameValuePair[] DEFAULT_POSTS = new MustacheNameValuePair[] {};
	
	/**
	 * {@link Page} does not {@link #shouldSaveValue()} by default.
	 */
	public final boolean defaultShouldSaveValue() {
		return false;
	}
	
	private String getURL(Browser browser, Variables variables) throws MissingVariableException {
		return url.compileEncoded(variables, browser, Browser.UTF_8);
	}
	
	/**
	 * Request this {@link Page}, and return the response.
	 * @param browser The {@link Browser} to use when making the request.
	 * @param compiler The {@link RegexpCompiler} to use.
	 * @param variables The {@link Variables} to use when substituting.
	 * @return The response to this request as a {@link String}.
	 * @throws MissingVariableException If <code>variables</code> lacked a necessary element.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} had an error.
	 * @throws BrowserException If <code>browser</code> experienced an exception while loading the response.
	 */
	public String[] generateResultValues(RegexpCompiler compiler, Browser browser, Variables variables, String source)
			throws MissingVariableException, BrowserException {		
		// Temporary executions to do before.  Not published, executed each time.
		for(int i = 0 ; i < preload.length ; i ++) {
			preload[i].generateResultValues(compiler, browser, variables, source);
		}
		String response = null;
		if(method.equals(Method.GET)) {
			response = browser.get(getURL(browser, variables),
					MustacheNameValuePair.compile(headers, variables),
					MustacheNameValuePair.compile(cookies, variables),
					Regexp.compile(stopBecause, compiler, variables));
		} else if(method.equals(Method.POST)) {
			if(postNameValuePairs == null) {
				response = browser.post(getURL(browser, variables),
						MustacheNameValuePair.compile(headers, variables),
						MustacheNameValuePair.compile(cookies, variables),
						Regexp.compile(stopBecause, compiler, variables),
						postData.compile(variables));
			} else {
				response = browser.post(getURL(browser, variables),
						MustacheNameValuePair.compile(headers, variables),
						MustacheNameValuePair.compile(cookies, variables),
						Regexp.compile(stopBecause, compiler, variables),
						MustacheNameValuePair.compile(postNameValuePairs,variables));
			}
		} else if(method.equals(Method.HEAD)) {
			browser.head(getURL(browser, variables), 
					MustacheNameValuePair.compile(headers, variables),
					MustacheNameValuePair.compile(cookies, variables));
		}
		return new String[] { response };
	}
	

	protected String getDefaultName(Variables variables, RegexpCompiler compiler, Browser browser)
			throws MissingVariableException {
		return getURL(browser, variables);
	}
}
