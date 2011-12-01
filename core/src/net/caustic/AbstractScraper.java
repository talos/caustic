package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.LogDatabaseListener;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Deserializer;
import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
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
	
	// we can use volatile because changed value not dependent on prior value.
	private volatile boolean autoRun = false;
	
	public AbstractScraper(Database db, HttpBrowser browser, Deserializer deserializer,
			ScraperListener listener) {
		this.db = db;
		this.browser = browser;
		this.deserializer = deserializer;
		db.addListener(this);
		this.listener = listener;
	}
	
	public final void register(Logger logger) {
		super.register(logger);
		browser.register(logger);
	}
	
	public final void setAutoRun(boolean autoRun) {
		this.autoRun = autoRun;
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
		//final Instruction instruction = new SerializedInstruction(quoted, deserializer, rootURI);
		
		// creation of a new scope could be stopped by database crash.
		try {
			final Scope scope = db.newDefaultScope();
			Enumeration e = input.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				db.put(scope, key, (String) input.get(key));
			}
			
			db.putInstruction(scope, null, uriOrJSON, rootURI);
			
			return scope;
		} catch(DatabaseException e) {
			crash(null, uriOrJSON, rootURI, e);
			return null;
		}
	}
	
	public void onPutInstruction(Scope scope, String source, String instruction, String uri) {
		super.onPutInstruction(scope, source, instruction, uri);
		
		try {
			deserializer.deserialize(instruction, db, scope, uri, source);
		} catch(InterruptedException e) {
			crash(scope, instruction, uri, e);
		} catch(DatabaseException e) {
			crash(scope, instruction, uri, e);
		}
	}
	
	public void onPutLoad(Scope scope, String source, Load load) {
		super.onPutLoad(scope, source, load);
		
		Executable executable = new Executable(load, source, db, scope, browser, this);
		if(autoRun == false) {
			listener.onPause(scope, load.serialized, load.uri, new Resume(this, executable));
		} else {
			submit(executable);
		}
	}
	
	public void onPutFind(Scope scope, String source, Find find) {
		super.onPutFind(scope, source, find);
		
		submit(new Executable(find, source, db, scope, browser, this));
	}
	
	protected final void crash(Scope scope, String instruction, String uri, Throwable reason) {
		listener.onCrash(scope, instruction, uri, reason);
		interrupt();
	}

	/**
	 * Override this method to run the {@link Instruction}.  Do not call it explicitly.
	 */
	protected abstract void submit(Executable executable);
		
	protected abstract void interrupt();
}
