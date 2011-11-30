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
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * Implementations of {@link AbstractScraper} can scrape an {@link Instruction}.
 * @author talos
 *
 */
public abstract class AbstractScraper extends LogDatabaseListener implements Loggable {

	private static final String rootURI = StringUtils.USER_DIR;

	private final Database db;
	private final HttpBrowser browser;
	private final Deserializer deserializer;
	private final ScraperListener listener;
	private final boolean autoRun;
	
	private int submitted = 0;
	private int finished = 0;
	private int paused = 0;
	
	private int stuck = 0;
	private int failed = 0;
	
	public AbstractScraper(Database db, HttpBrowser browser, Deserializer deserializer,
			ScraperListener listener, boolean autoRun) {
		this.db = db;
		this.browser = browser;
		this.deserializer = deserializer;
		db.addListener(this);
		this.autoRun = autoRun;
		this.listener = listener;
	}
	
	public final void register(Logger logger) {
		super.register(logger);
		browser.register(logger);
	}
	
	/**
	 * Scrape <code>instruction</code> with <code>input</code>, triggering events on
	 * the assigned {@link ScraperListener}.
	 * @param uriOrJSON An instruction template to scrape.  Either a JSON string or a
	 * URI referring to a JSON string.  URIs entered here do not need to be quoted, 
	 * although URIs inside files <strong>do</strong> need to be quoted.
	 * @param input A {@link Hashtable} of {@link String} to {@link String} key-values,
	 * which will be used as defaults for substitutions during the scrape.
	 * @param autoRun Whether {@link Instruction} children should be scraped automatically.
	 * @return the parent {@link Scope} of all results.
	 */
	public Scope scrape(String uriOrJSON, Hashtable input) {
		
		// Quote string it if it's not quoted, and isn't JSON.
		// This is OK because the process hits {@link #submit}, not {@link #scrape}.
		final String quoted;
		final char first = uriOrJSON.charAt(0);
		if(first == '"' || first == '[' || first == '{') {
			quoted = uriOrJSON;
		} else {
			quoted = StringUtils.quote(uriOrJSON);
		}
		
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
			
			//scrape(scope, listener, autoRun);
			
			return scope;
		} catch(DatabaseException e) {
			e.printStackTrace(); // TODO 
			//process.triggerCrashed(instruction, null, null, null, e);
			return null;
		}
	}
	
	/**
	 * When we register a new, ready instruction, run it immediately if we don't need
	 * to confirm, hold off otherwise.
	 */
	public void onPutReady(Scope scope, String source, Instruction instruction) {
		super.onPutReady(scope, source, instruction);
		listener.onPutReady(scope, source, instruction);
		
		Executable executable = new Executable(instruction, source, db, scope, browser, this);
		
		if(instruction.shouldConfirm() && this.autoRun == false) {
			synchronized(this) {
				paused++;
			}
			listener.onPause(scope, instruction, new Resume(this, executable));
		} else {
			synchronized(this) {
				submitted++;
			}
			submit(executable);
		}
	}
	
	public void onPutMissing(Scope scope, String source, Instruction instruction, String[] missingTags) {
		super.onPutMissing(scope, source, instruction, missingTags);
		listener.onPutMissing(scope, source, instruction, missingTags);
		
		synchronized(this) {
			stuck++;
		}
	}
	
	public void onPutFailed(Scope scope, String source, Instruction instruction, String failedBecause) {
		super.onPutFailed(scope, source, instruction, failedBecause);
		listener.onPutFailed(scope, source, instruction, failedBecause);
		
		synchronized(this) {
			failed++;
		}
	}
	
	public void onPut(Scope scope, String key, String value) {
		super.onPut(scope, key, value);
		listener.onPut(scope, key, value);
	}
	
	public void onNewDefaultScope(Scope scope) {
		super.onNewDefaultScope(scope);
		listener.onNewDefaultScope(scope);
	}
	
	public void onNewScope(Scope parent, Scope scope) {
		super.onNewScope(parent, scope);
		listener.onNewScope(parent, scope);
	}
	
	public void onNewScope(Scope parent, Scope scope, String value) {
		super.onNewScope(parent, scope, value);
		listener.onNewScope(parent, scope, value);
	}

	public void onAddCookie(Scope scope, String host, String name, String value) {
		super.onAddCookie(scope, host, name, value);
		listener.onAddCookie(scope, host, name, value);
	}
	
	/**
	 * 
	 * @return <code>true</code> if this {@link AbstractScraper} is not currently scraping
	 * anything, <code>false</code> otherwise.
	 */
	public synchronized final boolean isDormant() {
		return submitted == finished && paused == 0;
	}
	
	public synchronized int getSubmitted() {
		return submitted;
	}
	
	public synchronized int getStuck() {
		return stuck;
	}
	
	public synchronized int getFailed() {
		return failed;
	}
	
	public synchronized int getFinished() {
		return finished;
	}
	
	public synchronized int getPaused() {
		return paused;
	}
	
	protected synchronized final void incrementFinished() {
		finished++;
	}
	
	protected synchronized final void decrementPaused() {
		paused--;
	}
	
	protected final void crash(Scope scope, Instruction instruction, Throwable reason) {
		listener.onCrash(scope, instruction, reason);
		interrupt();
	}

	/**
	 * Override this method to run the {@link Instruction}.  Do not call it explicitly.
	 */
	protected abstract void submit(Executable executable);
	
	//protected abstract void interrupt(Throwable because);
	
	protected abstract void interrupt();
}
