package net.microscraper.resources.definitions;

import java.net.URI;

import net.microscraper.client.Interfaces;

public class ScraperSource {
	public final MustacheTemplate stringSource;
	public final boolean hasStringSource;
	public final Link pageLinkSource;
	public final boolean hasPageLinkSource;
	
	protected ScraperSource(MustacheTemplate stringSource) {
		this.hasStringSource = true;
		this.stringSource = stringSource;

		this.hasPageLinkSource = false;
		this.pageLinkSource = null;
	}
	protected ScraperSource(Link pageLinkSource) {
		this.hasStringSource = false;
		this.stringSource = null;

		this.hasPageLinkSource = true;
		this.pageLinkSource = pageLinkSource;		
	}
	
	/**
	 * Deserialize a {@link ScraperSource} from a {@link Interfaces.JSON.Object}.
	 * @param location The {@link URI} location of the scraper's source.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An {@link ScraperSource} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * a {@link ScraperSource}.
	 */
	public static ScraperSource deserialize(Interfaces.JSON jsonInterface,
					URI location, Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		return new ScraperSource(Link.deserialize(jsonInterface, location, jsonObject));
	}
	

	/**
	 * Obtain a {@link ScraperSource} from a String.
	 * @param source The Mustacheable string to use.
	 * @return An {@link ScraperSource} instance.
	 */
	public static ScraperSource fromString(String source) {
		return new ScraperSource(new MustacheTemplate(source));
	}
}
