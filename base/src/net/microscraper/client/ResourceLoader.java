package net.microscraper.client;

import java.io.IOException;

import net.microscraper.server.Ref;
import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.Scraper;

/**
 * Implementations of this interface can load and deserialize {@link Ref}s.
 * @author realest
 *
 */
public interface ResourceLoader {
	
	/**
	 * Load a {@link Parser} from a {@link Ref}.
	 * @param link The {@link Ref} to load.
	 * @return A {@link Parser} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Parser} could not be deserialized.
	 */
	public Parser loadParser(Ref link) throws IOException, DeserializationException;

	/**
	 * Load a {@link Scraper} from a {@link Ref}.
	 * @param link The {@link Ref} to load.
	 * @return A {@link Scraper} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Scraper} could not be deserialized.
	 */
	public Scraper loadScraper(Ref link) throws IOException, DeserializationException;
	
	/**
	 * Load a {@link Page} from a {@link Ref}.
	 * @param link The {@link Ref} to load.
	 * @return A {@link Page} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Page} could not be deserialized.
	 */
	 public Page loadPage(Ref link) throws IOException, DeserializationException;
}
