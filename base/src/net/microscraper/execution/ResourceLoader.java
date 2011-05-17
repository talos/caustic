package net.microscraper.execution;

import java.io.IOException;

import net.microscraper.model.DeserializationException;
import net.microscraper.model.Link;
import net.microscraper.model.Page;
import net.microscraper.model.Parser;
import net.microscraper.model.Scraper;

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
