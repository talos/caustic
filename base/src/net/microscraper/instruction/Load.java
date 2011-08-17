package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonObject;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheSubstitution;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Encoder;
import net.microscraper.util.Variables;

/**
 * A {@link Scraper} that load a web page.
 * @author realest
 *
 */
public final class Load {
	/**
	 * The HTTP request type to use.  Either {@link Method#GET},
	 * {@link Method#POST}, or {@link Method#HEAD}.
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
	
	private Load(String method, MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		this.method = method;
		this.url = url;
		this.postData = postData;
		this.postNameValuePairs = postNameValuePairs;
		this.headers = headers;
		this.cookies = cookies;
		this.preload = preload;
		this.stops = stops;
	}
	
	public static Load head(MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(Method.HEAD, url, null, null, headers, cookies, preload, stops);
	}
	
	public static Load get(MustacheTemplate url, MustacheNameValuePair[] headers,
			MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(Method.GET, url, null, null, headers, cookies, preload, stops);
	}
	
	public static Load post(MustacheTemplate url, MustacheTemplate postData,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(Method.POST, url, postData, null, headers, cookies, preload, stops);
	}
	
	public static Load post(MustacheTemplate url, MustacheNameValuePair[] postNameValuePairs,
			MustacheNameValuePair[] headers, MustacheNameValuePair[] cookies,
			Load[] preload, MustachePattern[] stops) {
		return new Load(Method.POST, url, null, postNameValuePairs, headers, cookies, preload, stops);
	}
	
	/**
	 * Request this {@link Load}, and return the response.
	 * @param browser The {@link Browser} to use when making the request.
	 * @param compiler The {@link RegexpCompiler} to use.
	 * @param variables The {@link Variables} to use when substituting.
	 * @return The response to this request as a {@link String}.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} had an error.
	 * @throws IOException If a resource could not be loaded.
	 * @throws InterruptedException If the user interrupted the loading..
	 */
	public Execution getResponse(Browser browser, Variables variables)
			throws IOException, InterruptedException {		
		// Temporary executions to do before.  Not published, executed each time.
		for(int i = 0 ; i < preload.length ; i ++) {
			preload[i].getResponse(browser, variables);
		}
		String response = null;
		
		MustacheSubstitution urlSub = url.sub(variables, browser, Browser.UTF_8);
		if(!urlSub.isSuccessful()) {
			return urlSub;
		} else {
			String url = urlSub.getSubbed();
			if(method.equals(Method.GET)) {
				response = browser.get(url,
						MustacheNameValuePair.compile(headers, variables),
						MustacheNameValuePair.compile(cookies, variables),
						MustachePattern.compile(stops, compiler, variables));
			} else if(method.equals(Method.POST)) {
				if(postNameValuePairs == null) {
					response = browser.post(url,
							MustacheNameValuePair.compile(headers, variables),
							MustacheNameValuePair.compile(cookies, variables),
							MustachePattern.compile(stops, compiler, variables),
							postData.sub(variables));
				} else {
					response = browser.post(url,
							MustacheNameValuePair.compile(headers, variables),
							MustacheNameValuePair.compile(cookies, variables),
							MustachePattern.compile(stopBecause, compiler, variables),
							MustacheNameValuePair.compile(postNameValuePairs,variables));
				}
			} else if(method.equals(Method.HEAD)) {
				browser.head(url, 
						MustacheNameValuePair.compile(headers, variables),
						MustacheNameValuePair.compile(cookies, variables));
			}
		}
	}
}
