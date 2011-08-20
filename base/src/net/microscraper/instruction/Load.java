package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load implements Action {
	/**
	 * The HTTP request type to use.  Either {@link Browser#GET},
	 * {@link Browser#POST}, or {@link Browser#HEAD}.
	 */
	private final String method;

	/**
	 * {@link MustacheNameValuePair}s of cookies.
	 */
	private final MustacheNameValuePair[] cookies;

	/**
	 * {@link MustacheNameValuePair}s of generic headers.
	 */
	private final MustacheNameValuePair[] headers;

	/**
	 * {@link Load} requests to make beforehand. No data is extracted from these pages.
	 */
	private final Load[] preload;

	/**
	 * {@link MustachePattern}s that terminate the loading of this page's body.
	 */
	private final MustachePattern[] stops;
	
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
	 * The {@link Browser} to use when loading.
	 */
	private final Browser browser;
	
	private Load( 
			Browser browser, String method, MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		this.browser = browser;
		this.method = method;
		this.url = url;
		this.postData = postData;
		this.postNameValuePairs = postNameValuePairs;
		this.headers = headers;
		this.cookies = cookies;
		this.preload = preload;
		this.stops = stops;
	}
	
	public static Load head(Browser browser, MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(browser, Browser.HEAD, url, null, null, headers, cookies, preload, stops);
	}
	
	public static Load get(Browser browser, MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(browser, Browser.GET, url, null, null, headers, cookies, preload, stops);
	}
	
	public static Load post(Browser browser, MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(browser, Browser.POST, url, postData, null, headers, cookies, preload, stops);
	}
	
	public static Load post(Browser browser, MustacheTemplate url, MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(browser, Browser.POST, url, null, postNameValuePairs, headers, cookies, preload, stops);
	}
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a one-length {@link String}
	 * with the response body, which is a zero-length {@link String} if the {@link Load}'s method
	 * is Head.
	 */
	public Execution execute(String source, Variables variables)
			throws InterruptedException {		
		// Temporary executions to do before.  Not published, executed each time.
		// TODO combine missingVariables from preexecutions here?
		for(int i = 0 ; i < preload.length ; i ++) {
			Execution preExecution = preload[i].execute(source, variables);
			if(!preExecution.isSuccessful()) {
				return preExecution;
			}
		}
		try {
			//final Execution result;

			Execution urlSub = url.sub(variables, browser, Browser.UTF_8);
			Execution headersSub = Execution.arraySub(headers, variables);
			Execution cookiesSub = Execution.arraySub(cookies, variables);
			
			if(!urlSub.isSuccessful() || !headersSub.isSuccessful() || !cookiesSub.isSuccessful()) {
				return Execution.combine(new Execution[] {
						urlSub, headersSub, cookiesSub
				});
			} else {
				
				final String responseBody;

				String url = (String) urlSub.getExecuted();
				NameValuePair[] headers = (NameValuePair[]) headersSub.getExecuted();
				NameValuePair[] cookies = (NameValuePair[]) cookiesSub.getExecuted();
				
				if(method.equals(Browser.HEAD)){
					browser.head(url, headers, cookies);
					responseBody = "";
				} else {
					
					Execution stopsSub = Execution.arraySub(stops, variables);
					if(!stopsSub.isSuccessful()) {
						return Execution.missingVariables(stopsSub.getMissingVariables());
					} else {
						Pattern[] stops = (Pattern[]) stopsSub.getExecuted();
						if(method.equals(Browser.POST)) {
							if(postNameValuePairs == null) {
								Execution postsSub = Execution.arraySub(postNameValuePairs, variables);
								if(!postsSub.isSuccessful()) {
									return Execution.missingVariables(postsSub.getMissingVariables());
								} else {
									NameValuePair[] posts = (NameValuePair[]) postsSub.getExecuted();
									responseBody = browser.post(url, headers, cookies, stops, posts);
								}
							} else {
								Execution postsSub = postData.sub(variables, browser, Browser.UTF_8);
								if(!postsSub.isSuccessful()) {
									return Execution.missingVariables(postsSub.getMissingVariables());
								} else {
									String postData = (String) postsSub.getExecuted();
									responseBody = browser.post(url, headers, cookies, stops, postData);
								}
							}
						} else {
							responseBody = browser.get(url, headers, cookies, stops);
						}
					}
				}
				return Execution.success(new String[] { responseBody } );
			}
			//return result;
		} catch(IOException e) {
			return Execution.ioException(e);
		}
	}
	
	/**
	 * {@Link Load}'s default name is its {@link #url}.
	 */
	public MustacheTemplate getDefaultName() {
		return url;
	}

	/**
	 * {@link Load} does not persist its value by default, because entire pages tend to be large.
	 */
	public boolean getDefaultShouldPersistValue() {
		return false;
	}
}
