package net.microscraper.client;

import java.io.IOException;

import net.microscraper.server.resource.DeserializationException;
import net.microscraper.server.resource.Link;
import net.microscraper.server.resource.Page;
import net.microscraper.server.resource.Parser;
import net.microscraper.server.resource.Scraper;

/**
 * Implementations of this interface can load and deserialize {@link Link}s.
 * @author realest
 *
 */
public interface ResourceLoader {
	
	/**
	 * Load a {@link Parser} from a {@link Link}.
	 * @param link The {@link Link} to load.
	 * @return A {@link Parser} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Parser} could not be deserialized.
	 */
	public Parser loadParser(Link link) throws IOException, DeserializationException;

	/**
	 * Load a {@link Scraper} from a {@link Link}.
	 * @param link The {@link Link} to load.
	 * @return A {@link Scraper} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Scraper} could not be deserialized.
	 */
	public Scraper loadScraper(Link link) throws IOException, DeserializationException;
	
	/**
	 * Load a {@link Page} from a {@link Link}.
	 * @param link The {@link Link} to load.
	 * @return A {@link Page} instance.
	 * @throws IOException If the link could not be loaded.
	 * @throws DeserializationException If the {@link Page} could not be deserialized.
	 */
	 public Page loadPage(Link link) throws IOException, DeserializationException;
}
