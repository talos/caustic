package net.microscraper.resources.definitions;

import java.net.MalformedURLException;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL implements Executable {
	private final MustacheTemplate urlTemplate;
	public URL(MustacheTemplate urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Get the {@link java.net.URL} from the URL resource for a specific context.  The URL String is mustached.
	 * @param context Provides the variables used in mustacheing.
	 * @return a {@link java.net.URL}
	 * @throws ExecutionDelay if the Mustache template for the url can't be compiled.
	 * @throws ExecutionFatality 
	 */
	public java.net.URL getURL(ExecutionContext context)
				throws ExecutionDelay, ExecutionFatality {
		try {
			return new java.net.URL(urlTemplate.getString(context));
		} catch(MalformedURLException e) {
			throw new ExecutionFatality(e, this);
		}
	}
}
