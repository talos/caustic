package net.microscraper.resources.definitions;

import java.net.MalformedURLException;

import net.microscraper.resources.Scraper;
import net.microscraper.resources.ScrapingDelay;
import net.microscraper.resources.ScrapingFatality;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL implements Problematic {
	private final MustacheTemplate urlTemplate;
	public URL(MustacheTemplate urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Get the {@link java.net.URL} from the URL resource for a specific context.  The URL String is mustached.
	 * @param context Provides the variables used in mustacheing.
	 * @return a {@link java.net.URL}
	 * @throws ScrapingDelay if the Mustache template for the url can't be compiled.
	 * @throws ScrapingFatality 
	 */
	public java.net.URL getURL(Scraper context)
				throws ScrapingDelay, ScrapingFatality {
		try {
			return new java.net.URL(urlTemplate.getString(context));
		} catch(MalformedURLException e) {
			throw new ScrapingFatality(e, this);
		}
	}
	public String getName() {
		return urlTemplate.getName();
	}
}
