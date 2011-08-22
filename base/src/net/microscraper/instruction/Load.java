package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.Encoder;
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

	/**
	 * The {@link Encoder} to use when encoding the URL.
	 */
	private final Encoder encoder;
	
	private Load( 
			Browser browser, Encoder encoder,
			String method, MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies, MustachePattern[] stops) {
		this.browser = browser;
		this.encoder = encoder;
		this.method = method;
		this.url = url;
		this.postData = postData;
		this.postNameValuePairs = postNameValuePairs;
		this.headers = headers;
		this.cookies = cookies;
		this.stops = stops;
	}
	
	public static Load head(Browser browser, Encoder encoder, MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies, MustachePattern[] stops) {
		return new Load(browser, encoder, Browser.HEAD, url, null, null, headers, cookies, stops);
	}
	
	public static Load get(Browser browser, Encoder encoder, MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies, MustachePattern[] stops) {
		return new Load(browser,encoder,  Browser.GET, url, null, null, headers, cookies, stops);
	}
	
	public static Load post(Browser browser, Encoder encoder, MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies, MustachePattern[] stops) {
		return new Load(browser,encoder,  Browser.POST, url, postData, null, headers, cookies, stops);
	}
	
	public static Load post(Browser browser, Encoder encoder, MustacheTemplate url, MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies, MustachePattern[] stops) {
		return new Load(browser, encoder, Browser.POST, url, null, postNameValuePairs, headers, cookies, stops);
	}
	
	/**
	 * Make the request and retrieve the response body specified by this {@link Load}.
	 * @return An {@link Execution} whose {@link Execution#getExecuted()} is a one-length {@link String}
	 * with the response body, which is a zero-length {@link String} if the {@link Load}'s method
	 * is Head.
	 */
	public Execution execute(String source, Variables variables)
			throws InterruptedException {		
		try {
			Execution urlSub = url.sub(variables, encoder, Browser.UTF_8);
			Execution headersSub = Execution.arraySubNameValuePair(headers, variables);
			Execution cookiesSub = Execution.arraySubNameValuePair(cookies, variables);
			
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
					
					Execution stopsSub = Execution.arraySubPattern(stops, variables);
					if(!stopsSub.isSuccessful()) {
						return Execution.missingVariables(stopsSub.getMissingVariables());
					} else {
						Pattern[] stops = (Pattern[]) stopsSub.getExecuted();
						if(method.equals(Browser.POST)) {
							if(postNameValuePairs != null) {
								Execution postsSub = Execution.arraySubNameValuePair(postNameValuePairs, variables);
								if(!postsSub.isSuccessful()) {
									return Execution.missingVariables(postsSub.getMissingVariables());
								} else {
									NameValuePair[] posts = (NameValuePair[]) postsSub.getExecuted();
									responseBody = browser.post(url, headers, cookies, stops, posts);
								}
							} else {
								//Execution postsSub = postData.sub(variables, encoder, Browser.UTF_8);
								Execution postsSub = postData.sub(variables);
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
