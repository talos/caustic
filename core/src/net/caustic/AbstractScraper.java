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
	 * <code>listener</code>.  Child {@link Instruction}s must be listened for on
	 * {@link ScraperListener#onReady(Instruction, Database, Scope, Scope, String, HttpBrowser, Runnable)}
	 * and fired manually.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks.
	 */
	public Scope scrape(String uriOrJSON, Hashtable input, ScraperListener listener) {
		return scrape(uriOrJSON, input, listener, false);
	}

	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * <code>listener</code>.  All child {@link Instruction}s will be scraped automatically.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks.
	 */
	public Scope scrapeAll(String uriOrJSON, Hashtable input, ScraperListener listener) {
		return scrape(uriOrJSON, input, listener, true);
	}
	
	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * <code>listener</code>.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks.
	 * @param autoRun Whether {@link Instruction} children should be scraped automatically.
	 */
	public Scope scrape(String uriOrJSON, Hashtable input, ScraperListener listener, boolean autoRun) {
		
		// The process calls back to this method.
		ScraperProcess process = new ScraperProcess(listener, this, autoRun);
		
		// obtain an instruction from the supplied URI or JSON relative to the root URI.
		final Instruction instruction = new SerializedInstruction(uriOrJSON, deserializer, rootURI);
		
		// creation of a new scope could be stopped by database crash.
		try {
			final Scope scope = db.newScope();
			Enumeration e = input.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				db.put(scope, key, (String) input.get(key));
			}
			scrapesStarted++;
			
			// initial call doesn't filter through .onReady
			process.triggerScrape(instruction, db, scope, null, null, browser.copy());
			
			return scope;
		} catch(DatabaseException e) {
			process.triggerCrashed(instruction, null, null, null, e); // TODO
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
