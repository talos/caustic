package net.microscraper.resources.definitions;

import net.microscraper.client.Browser;
import net.microscraper.resources.ExecutionContext;

/**
 * Class to make an HTTP POST request.
 * @see WebPage
 * @author realest
 *
 */
public class WebPagePost extends WebPageBody {
	private final Post[] posts;
	
	/**
	 * @param url A URL to use. 
	 * @param headers An array of headers to add when requesting this web page.
	 * @param cookies An array of cookies to add to the browser before requesting this web page.
	 * @param priorWebPages An array of web pages that should be loaded before
	 * requesting this web page.
	 * @param terminates An array of regular expression resources that terminate loading.
	 * @param posts An array of posts to add to include in the request.
	 */
	public WebPagePost(URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPage[] webPages, Regexp[] terminates, Post[] posts) {
		super(url, headers, cookies, webPages, terminates);
		this.posts = posts;
	}
	
	public String loadUsing(Browser browser) {
		return null;
	}
	
	public Object execute(ExecutionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
