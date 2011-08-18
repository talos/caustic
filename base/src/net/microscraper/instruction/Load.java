package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Substitution;
import net.microscraper.util.Variables;

/**
 * An {@link Executable} for making an HTTP request and likely loading
 * a URL.
 * @author realest
 *
 */
public final class Load implements Executable {
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
	
	private Load(Browser browser, String method, MustacheTemplate url, MustacheTemplate postData,
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
	
	public Execution execute(String source, Variables variables)
			throws InterruptedException {		
		// Temporary executions to do before.  Not published, executed each time.
		for(int i = 0 ; i < preload.length ; i ++) {
			preload[i].execute(source, variables);
		}
		try {
			final Execution result;

			Substitution urlSub = url.sub(variables, browser, Browser.UTF_8);
			Substitution headersSub = Substitution.arraySub(headers, variables);
			Substitution cookiesSub = Substitution.arraySub(cookies, variables);
			
			if(!urlSub.isSuccessful() || !headersSub.isSuccessful() || !cookiesSub.isSuccessful()) {
				result = Execution.missingVariables(Substitution.combine(new Substitution[] {
						urlSub, headersSub, cookiesSub
				}).getMissingVariables());
			} else {
				String url = (String) urlSub.getSubstituted();
				NameValuePair[] headers = (NameValuePair[]) headersSub.getSubstituted();
				NameValuePair[] cookies = (NameValuePair[]) cookiesSub.getSubstituted();
				
				if(method.equals(Browser.HEAD)){
					browser.head(url, headers, cookies);
					result = Execution.success();
				} else {
					Substitution stopsSub = Substitution.arraySub(stops, variables);
					if(!stopsSub.isSuccessful()) {
						result = Execution.missingVariables(stopsSub.getMissingVariables());
					} else {
						Pattern[] stops = (Pattern[]) stopsSub.getSubstituted();
						if(method.equals(Browser.POST)) {
							if(postNameValuePairs == null) {
								Substitution postsSub = Substitution.arraySub(postNameValuePairs, variables);
								if(!postsSub.isSuccessful()) {
									result  = Execution.missingVariables(postsSub.getMissingVariables());
								} else {
									NameValuePair[] posts = (NameValuePair[]) postsSub.getSubstituted();
									result = Execution.success(browser.post(url, headers, cookies, stops, posts));
								}
							} else {
								Substitution postsSub = postData.sub(variables, browser, Browser.UTF_8);
								if(!postsSub.isSuccessful()) {
									result = Execution.missingVariables(postsSub.getMissingVariables());
								} else {
									String postData = (String) postsSub.getSubstituted();
									result = Execution.success(browser.post(url, headers, cookies, stops, postData));
								}
							}
						} else {
							result = Execution.success(browser.get(url, headers, cookies, stops));
						}
					}
				}
			}
			return result;
		} catch(IOException e) {
			return Execution.ioException(e);
		}
	}
}
