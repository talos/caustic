package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseListener;
import net.caustic.database.LogDatabaseListener;
import net.caustic.database.ReadyExecution;
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
public abstract class AbstractScraper extends LogDatabaseListener implements Loggable {

	private volatile int submitted = 0;
	private volatile int paused = 0;
	
	// a count of the number of elements internal to {@link #stuck}, which is multidimensional
	// and thus hard to count.
	private volatile int stuckCnt = 0;
	private volatile int successful = 0;
	private volatile int failed = 0;
	
	private final Database db;
	private final HttpBrowser browser;
	private final Deserializer deserializer;
	
	private final String rootURI = StringUtils.USER_DIR;
	
	private final MultiLog log = new MultiLog();

	/**
	 * A {@link Hashtable} of stuck {@link StoppedInstruction}s.  The first dimension of
	 * keys are {@link Scope}s, and the second dimension is an array of Strings
	 * of missing tags.  The final values are the {@link StoppedInstruction}s.
	 */
	//private final Hashtable stuck = new Hashtable();
	
	//private volatile int scrapesStarted = 0;
	//private volatile int scrapesFinished = 0;
	
	public AbstractScraper(Database db, HttpBrowser browser, Deserializer deserializer) {
		super(log);
		this.db = db;
		this.browser = browser;
		this.deserializer = deserializer;
		db.addListener(this);
		
	}
	
	public final void register(Logger logger) {
		browser.register(logger);
		log.register(logger);
	}

	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * <code>listener</code>.  Child {@link Instruction}s must be listened for on
	 * {@link ScraperListener#onReady(Instruction, Database, Scope, Scope, String, HttpBrowser, Runnable)}
	 * and fired manually.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.  URIs entered here do not need to be quoted, 
	 * although URIs inside files <strong>do</strong> need to be quoted.
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
	 * URI referring to a JSON string.  URIs entered here do not need to be quoted, 
	 * although URIs inside files <strong>do</strong> need to be quoted.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks.
	 * @return the parent {@link Scope} of all results.
	 */
	public Scope scrapeAll(String uriOrJSON, Hashtable input, ScraperListener listener) {
		return scrape(uriOrJSON, input, listener, true);
	}
	
	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * <code>listener</code>.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.  URIs entered here do not need to be quoted, 
	 * although URIs inside files <strong>do</strong> need to be quoted.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param listener A {@link ScraperListener} to receive callbacks.
	 * @param autoRun Whether {@link Instruction} children should be scraped automatically.
	 * @return the parent {@link Scope} of all results.
	 */
	public Scope scrape(String uriOrJSON, Hashtable input, ScraperListener listener, boolean autoRun) {
		
		// Quote string it if it's not quoted, and isn't JSON.
		// This is OK because the process hits {@link #submit}, not {@link #scrape}.
		final String quoted;
		final char first = uriOrJSON.charAt(0);
		if(first == '"' || first == '[' || first == '{') {
			quoted = uriOrJSON;
		} else {
			quoted = StringUtils.quote(uriOrJSON);
		}
		
		// The process calls back to {@link #submit}.
		//ScraperProcess process = new ScraperProcess(listener, this, autoRun);
		
		// obtain an instruction from the supplied URI or JSON relative to the root URI.
		final Instruction instruction = new SerializedInstruction(quoted, deserializer, rootURI);
		
		// creation of a new scope could be stopped by database crash.
		try {
			final Scope scope = db.newDefaultScope();
			Enumeration e = input.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				db.put(scope, key, (String) input.get(key));
			}
			
			db.putReady(scope, null, instruction);
			
			scrape(scope, listener, autoRun);
			
			return scope;
		} catch(DatabaseException e) {
			e.printStackTrace(); // TODO 
			//process.triggerCrashed(instruction, null, null, null, e);
			return null;
		}
	}
	
	public void scrape(Scope scope, ScraperListener listener, boolean autoRun) {
		submitted++;
		
		ReadyExecution[] ready = db.getReady(scope);
		for(int i = 0 ; i < ready.length ; i ++) {
			submit(new Executable(browser, db, scope, ready[i]));
		}
	}
	

	public void onPutReady(Scope scope, ReadyExecution ready) {
		super.onPutReady(scope, ready);
		//TODO
	}

	public void onPutMissing(Scope scope, String source,
			Instruction instruction, String[] missingTags) {
		super.onPutMissing(scope, source, instruction, missingTags);
		// TODO
	}

	public void onPutFailed(Scope scope, String source,
			Instruction instruction, String failedBecause) {
		super.onPutFailed(scope, source, instruction, failedBecause);
		//TODO
	}
	
	/**
	 * 
	 * @return <code>true</code> if this {@link AbstractScraper} is not currently scraping
	 * anything, <code>false</code> otherwise.
	 */
	public final boolean isDormant() {
		return submitted == failed + stuckCnt + successful && paused == 0;
	}
	/*
	protected final void finishedScrape(int successful, int stuck, int failed) {
		scrapesFinished++;
	}*/
	
	/**
	 * Override this method to run the {@link Executable}.  Do not call it explicitly.
	 * @param executable
	 */
	protected abstract void submit(Executable executable);
	
	protected abstract void interrupt();
}
