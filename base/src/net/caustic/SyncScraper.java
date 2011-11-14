package net.caustic;

import net.caustic.database.Database;
import net.caustic.instruction.Executable;

/**
 * An executor that blocks the current thread on {@link #execute()}.
 * @author talos
 *
 */
public class SyncScraper extends DefaultScraper {
	
	public SyncScraper(Database db) {
		super(db);
	}
	
	protected void submit(Executable executable) {
		executable.run();
	}
	
	protected void interrupt() {
		throw new UnsupportedOperationException("Synchronized scraper does not like interruptions.");
	}
}
