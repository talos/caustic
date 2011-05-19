package net.microscraper.model;

import java.net.MalformedURLException;
import java.net.URI;

import net.microscraper.client.Interfaces;
import net.microscraper.execution.ScraperExecution;
import net.microscraper.execution.ScrapingDelay;
import net.microscraper.execution.ScrapingFatality;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL {
	public final MustacheTemplate urlTemplate;
	public URL(MustacheTemplate urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Create a {@link URL} from a String.
	 * @param String Input string.
	 * @return A {@link URL} instance.
	 */
	public static URL fromString(String urlTemplate) {
		return new URL(new MustacheTemplate(urlTemplate));
	}
	
	/**
	 * Get the {@link java.net.URL} from the URL resource for a specific context.  The URL String is mustached.
	 * @param context Provides the variables used in mustacheing.
	 * @return a {@link java.net.URL}
	 * @throws ScrapingDelay if the Mustache template for the url can't be compiled.
	 * @throws ScrapingFatality 
	 */
	/*
	public java.net.URL getURL(ContextRoot context)
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

	public URI getLocation() {
		// TODO Auto-generated method stub
		return null;
	}*/
}
