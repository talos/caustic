package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.LogDatabaseListener;
import net.caustic.deserializer.Deserializer;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * Implementations of {@link AbstractScraper} can scrape an {@link Instruction}.
 * @author talos
 *
 */
public abstract class AbstractScraper implements Loggable {
	
	private final Database db;
	private final HttpBrowser browser;
	private final Deserializer deserializer;
	
	private final String rootURI = StringUtils.USER_DIR;
	
	private final MultiLog log = new MultiLog();

	private volatile int scrapesStarted = 0;
	private volatile int scrapesFinished = 0;

	public AbstractScraper(Database db, HttpBrowser browser,
			Deserializer deserializer) {
		this.db = db;
		this.browser = browser;
		this.deserializer = deserializer;
		db.addListener(new LogDatabaseListener(log));
	}
	
	public final void register(Logger logger) {
		log.register(logger);
	}
	
	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * <code>listener</code>.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks as 
	 */
	public Scope scrape(String uriOrJSON, Hashtable input, ScraperListener listener) {
		
		// The wrapped listener performs control-flow callbacks to this abstract scraper,
		// specifically for this call to #scrape.
		ScraperListener wrappedListener = new ControlScraperListener(listener, this);
		
		// obtain an instruction from the supplied URI or JSON relative to the root URI.
		Instruction instruction = new SerializedInstruction(uriOrJSON, deserializer, rootURI);

		// creation of a new scope could be stopped by database crash.
		try {
			Scope scope = db.newScope();
			Enumeration e = input.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				db.put(scope, key, (String) input.get(key));
			}
			scrapesStarted++;
			wrappedListener.onScrape(instruction, db, scope, null, null, browser.copy());
			return scope;
		} catch(DatabaseException e) {
			wrappedListener.onCrashed(instruction, null, null, null, e); // TODO
			return null;
		}
	}
	
	/**
	 * 
	 * @return <code>true</code> if this {@link AbstractScraper} is not currently scraping
	 * anything, <code>false</code> otherwise.
	 */
	public final boolean isDormant() {
		return scrapesStarted == scrapesFinished;
	}
	
	protected final void finishedScrape(int successful, int stuck, int failed) {
		scrapesFinished++;
	}
	
	/**
	 * Override this method to run the {@link Executable}.
	 * @param executable
	 */
	protected abstract void submit(Executable executable);
	
	protected abstract void interrupt();
}
