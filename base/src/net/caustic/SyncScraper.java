package net.caustic;

import net.caustic.database.Database;
import net.caustic.deserializer.Deserializer;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.http.HttpBrowser;

/**
 * An executor that blocks the current thread on {@link #execute()}.
 * @author talos
 *
 */
public class SyncScraper extends AbstractScraper {
	
	public SyncScraper(Database db, HttpBrowser browser, Deserializer deserializer) {
		super(db, browser, deserializer);
	}
	
	protected void submit(Executable executable) {
		executable.run();
	}
	
	protected void interrupt() {
		throw new UnsupportedOperationException("Synchronized scraper does not like interruptions.");
	}
}
