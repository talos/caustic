package net.microscraper.resources.definitions;

import java.net.MalformedURLException;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL {
	private final MustacheableString urlString;
	public URL(MustacheableString url) {
		urlString = url;
	}
	
	/**
	 * Get the {@link java.net.URL} from the URL resource for a specific context.  The URL String is mustached.
	 * @param context Provides the variables used in mustacheing.
	 * @return a {@link java.net.URL}
	 * @throws MalformedURLException 
	 * @throws ExecutionDelay
	 * @throws ExecutionFailure
	 * @throws ExecutionFatality
	 */
	public java.net.URL getURL(ExecutionContext context) throws MalformedURLException, ExecutionDelay, ExecutionFailure, ExecutionFatality {
		return new java.net.URL(urlString.parse(context));
	}
}
