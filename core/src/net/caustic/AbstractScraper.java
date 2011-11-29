package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.LogDatabaseListener;
import net.caustic.database.StoppedInstruction;
import net.caustic.deserializer.Deserializer;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
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
	private final Hashtable stuck = new Hashtable();
	
	//private volatile int scrapesStarted = 0;
	//private volatile int scrapesFinished = 0;
	
	public AbstractScraper(Database db, HttpBrowser browser, Deserializer deserializer) {
		this.db = db;
		this.browser = browser;
		this.deserializer = deserializer;
		db.addListener(new LogDatabaseListener(log));
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
			
			//ScraperProcess process = new ScraperProcess(browser, db, listener, autoRun);
			
			
			// initial call doesn't filter through .onReady
			//process.triggerScrape(instruction, db, scope, null, null, browser.copy());
			
			// freeze with very little info
			scrape(instruction, scope, null, listener, autoRun);
			
			return scope;
		} catch(DatabaseException e) {
			e.printStackTrace(); // TODO 
			//process.triggerCrashed(instruction, null, null, null, e);
			return null;
		}
	}
	
	public void scrape(Instruction instruction, Scope scope, String source,
			ScraperListener listener, boolean autoRun) {
		submitted++;		
		
		submit(new Executable(instruction, scope, source, autoRun, this, db, browser, listener));
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
	
	/**
	 * Synchronized handler for instruction results from {@link Executable}.
	 * @param instruction
	 * @param result
	 */
	synchronized final void handle(Instruction instruction, Scope scope, String source,
			ScraperListener listener, boolean autoRun, InstructionResult result) {
		if(result.isSuccess()) {
			// only tell extra listener about successes that actually have keys.
			if(result.getName() != null) {
				listener.onSuccess(instruction, scope, source, result.getName(), result.getResults());
			}
			handleSuccess(instruction, scope, source, autoRun, listener, result);
		} else if(result.isMissingTags()) {
			listener.onMissingTags(instruction, scope, source, result.getMissingTags());
			handleMissingTags(instruction, scope, source, result.getMissingTags());
		} else {
			listener.onFailed(instruction, scope, source, result.getFailedBecause());
			handleFailure(instruction, result.getFailedBecause());
		}
	}
	
	private void handleSuccess(Instruction instruction, Scope scope, String source,
			boolean autoRun, ScraperListener listener, InstructionResult result) {
		final Instruction[] children = result.getChildren();
		final String[] results = result.getResults();
		
		// if shouldStoreValues is false, then name could be null.
		boolean shouldStoreValues = result.shouldStoreValues();
		String name = result.getName();
		
		// Launch or freeze children.
		try {
			for(int i = 0 ; i < results.length ; i ++) {
				final Scope childScope;
				// generate result scopes.
				final String childSource = results[i];
				if(results.length == 1) { // don't spawn a new result for single match
					childScope = scope;
					if(shouldStoreValues) {
						db.put(childScope, name, childSource);
					}
				} else {
					if(shouldStoreValues) {
						childScope = db.newScope(scope, name, childSource);
					} else {
						childScope = db.newScope(scope, name);							
					}
				}
				// create & scrape children.
				for(int j = 0 ; j < children.length ; j ++) {
					
					// Tell listener to scrape the child when ready if the child is real,
					// otherwise do it automatically.
					final Instruction child = children[j];
					//final HttpBrowser browserCopy = browser.copy();
					
					//process.triggerReady(child, db, childScope, scope, results[i], browserCopy);
					
					// Scrape immediately if we don't need to confirm or if autoRun flag is true.
					if(autoRun == true || instruction.shouldConfirm() == false) {
						scrape(child, childScope, childSource, listener, autoRun);
						//triggerScrape(instruction, db, scope, source, );
					} else {
						db.stopInstruction(scope, source, instruction);
						listener.onFreeze(instruction, childScope, childSource);
						//listener.onFreeze(instruction, db, scope, source);
					}
				}
			}
		
			// Retry stuck scrapers based off of new data.
			if(stuck.containsKey(scope)) {
				
				// stuckInScope is a table of Executables by String arrays of missing strings.
				Hashtable stuckInScope = (Hashtable) stuck.get(scope);
				Enumeration e = stuckInScope.keys();
				
				// Vector of missing tag string array references.  This will be used to prune
				// stuckInScope after enumeration is complete.
				Vector removeFromStuckInScope = new Vector(); 
				while(e.hasMoreElements()) {
					String[] missingTags = (String[]) e.nextElement();
					
					boolean shouldResubmit = true; // change to false if still missing tags
					
					// check the database to see if we're still missing any of these tags.
					for(int j = 0 ; j < missingTags.length ; j ++) {
						if(db.get(scope, missingTags[j]) == null) {
							// we are still missing a tag, break out.
							shouldResubmit = false;
							break;
						}
					}
					
					if(shouldResubmit == true) {
						//scraper.submit((Executable) stuck.get((Executable) stuck.get(missingTags)));
						//submit((Executable) stuckInScope.get(missingTags));
						
						StoppedInstruction frozen = (StoppedInstruction) stuckInScope.get(missingTags);
						
						// cancel out the prior submit.
						submitted--;
						scrape(frozen.instruction, scope, frozen.source, listener, autoRun);
						
						
						//triggerReady(instruction, db, scope, parent, source, browser)
						removeFromStuckInScope.add(missingTags);
					}
				}
				
				// remove the elements that were resubmitted.
				// we have to do this here because we have an enumerator, not an iterator. *grumble*
				e = removeFromStuckInScope.elements();
				while(e.hasMoreElements()) {
					stuckInScope.remove(e.nextElement());
					stuckCnt--;
				}
				
				// if nothing left stuck in scope, remove the scope
				if(stuckInScope.size() == 0) {
					stuck.remove(scope);
				}
			}
		}catch(DatabaseException e) {
			e.printStackTrace();
			interrupt(); // TODO
			//triggerCrashed(instruction, scope, source, e);
		}
	}
	
	private void handleMissingTags(final Instruction instruction, final Scope scope, 
			final String source, final String[] missingTags) {		
		final Hashtable stuckInScope;
		// add an executable missing tags to the hash of stuck executables.
		if(!stuck.containsKey(scope)) {
			stuckInScope = new Hashtable();
			stuck.put(scope, stuckInScope);
		} else {
			stuckInScope = (Hashtable) stuck.get(scope);
		}
		
		stuckInScope.put(missingTags, new StoppedInstruction(instruction, source));
		//stuckInScope.put(missingTags, new Executable(instruction, db, scope, source, browser, this));
		
		stuckCnt++;
	}
	
	private void handleFailure(Instruction instruction, String failedBecause) {		
		failed++;				
	}
}
