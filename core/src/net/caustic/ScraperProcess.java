package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.template.StringSubstitution;

/**
 * This handles a single call to
 * {@link AbstractScraper#scrape(String, Hashtable, ScraperListener)}
 * by enclosing the provided {@link ScraperListener} and determining
 * whether and when {@link Instruction}s should be retried.
 * @author talos
 *
 */
final class ScraperProcess {
	private final ScraperListener listener;
	private final AbstractScraper scraper;
	
	private volatile int submitted = 0;
	
	// a count of the number of elements internal to {@link #stuck}, which is multidimensional
	// and thus hard to count.
	private volatile int stuckCnt = 0;
	private volatile int successful = 0;
	private int failed = 0;
	
	/**
	 * If {@link #autoRun} is <code>true</code>, instructions are automatically fired from 
	 * {@link #onReady(Instruction, Database, Scope, Scope, String, HttpBrowser, Runnable)}.
	 */
	private final boolean autoRun;
	
	/**
	 * A {@link Hashtable} of stuck {@link Executable}s.  The first dimension of
	 * keys are {@link Scope}s, and the second dimension is an array of Strings
	 * of missing tags.  The final values are the {@link Executable}s.
	 */
	private final Hashtable stuck = new Hashtable();
	
	public ScraperProcess(ScraperListener listener, AbstractScraper scraper, boolean autoRun) {
		this.listener = listener;
		this.scraper = scraper;
		this.autoRun = autoRun;
	}
	
	public synchronized final void triggerReady(
			final Instruction instruction, final Database db,
			final Scope scope, final Scope parent, 
			final String source, final HttpBrowser browser) {
		
		// automatically launch children and instructions that don't need confirmation,
		// skipping the onReady.
		if(autoRun == true || instruction.shouldConfirm() == false) {
			triggerScrape(instruction, db, scope, parent, source, browser);
		} else {
			
			try {
				// This is a bit of a hack, but allows the onReady listener method to be
				// called with an actual name.
				StringSubstitution ss = instruction.getName().sub(db, scope);
				
				// if we can't work out the name, trigger missing tags right now.
				if(ss.isMissingTags()) {
					triggerMissingTags(instruction, db, scope, parent, source, browser, ss.getMissingTags());
				} else {
					
					// otherwise, we can trigger the onReady with a real name.
					String name = ss.getSubstituted();
					Runnable start = new Runnable() {
						// This run method is called from the listener that is passed onReady
						public void run() {
							triggerScrape(instruction, db, scope, parent, source, browser);
						}
					};
					listener.onReady(instruction, name, db, scope, parent, source, browser, start);
				}
			} catch(DatabaseException e) {
				triggerCrashed(instruction, scope, parent, source, e);
			}			
		}
	}
	
	public synchronized final void triggerScrape(
			final Instruction instruction, final Database db, final Scope scope, final Scope parent,
			final String source, final HttpBrowser browser) {
		submitted++;
		
		scraper.submit(new Executable(instruction, db, scope, parent, source, browser, this));
		
		listener.onScrape(instruction, db, scope, parent, source, browser);
	}
	
	public synchronized final void triggerSuccess(Instruction instruction, Database db, Scope scope, Scope parent,
			String source, String key, String[] results) {
		try {
			successful++;
	
			// Retry stuck scrapers based off of new data.
			if(stuck.containsKey(scope)) {
				Hashtable stuckInScope = (Hashtable) stuck.get(scope);
				Enumeration e = stuckInScope.keys();
				
				Vector resubmitted = new Vector(); // vector of missing tag string array references
				while(e.hasMoreElements()) {
					boolean shouldResubmit = true; // change to false if still missing tags
					
					String[] missingTags = (String[]) e.nextElement();
					for(int j = 0 ; j < missingTags.length ; j ++) {
						if(db.get(scope, missingTags[j]) == null) {
							shouldResubmit = false;
							break;
						}
					}
					
					if(shouldResubmit == true) {
						scraper.submit((Executable) stuck.get((Executable) stuck.get(missingTags)));
						resubmitted.add(missingTags);
					}
				}
				
				// remove the elements that were resubmitted.
				e = resubmitted.elements();
				while(e.hasMoreElements()) {
					stuckInScope.remove(e.nextElement());
					stuckCnt--;
				}
				
				// if nothing left stuck in scope, remove the scope
				if(stuckInScope.size() == 0) {
					stuck.remove(scope);
				}
			}
			
			// only tell extra listener about successes that actually have keys.
			if(key != null) {
				listener.onSuccess(instruction, db, scope, parent, source, key, results);
			}
			
			if(isDone()) {
				triggerFinish(successful, stuck.size(), failed);
			}
		} catch(DatabaseException e) {
			triggerCrashed(instruction, scope, parent, null, e);
		}
	}
	
	public synchronized void triggerMissingTags(Instruction instruction,
			Database db, Scope scope, Scope parent, String source,
			HttpBrowser browser, String[] missingTags) {
		
		// add an executable missing tags to the hash of stuck executables.
		if(!stuck.containsKey(scope)) {
			stuck.put(scope, new Hashtable());
		}
		
		Hashtable stuckInScope = (Hashtable) stuck.get(scope);
		stuckInScope.put(missingTags, new Executable(instruction, db, scope, parent, source, browser, this));
		stuckCnt++;
		
		listener.onMissingTags(instruction, db, scope, parent, source, browser, missingTags);
		
		if(isDone()) {
			triggerFinish(successful, stuck.size(), failed);
		}
	}
	
	public synchronized void triggerFailed(Instruction instruction, Database db,
			Scope scope, Scope parent, String source, String failedBecause) {
		failed++;
		
		listener.onFailed(instruction, db, scope, parent, source, failedBecause);
		
		if(isDone()) {
			triggerFinish(successful, stuck.size(), failed);
		}
	}
	
	public synchronized final void triggerCrashed(Instruction instruction, Scope scope, Scope parent, String source, Throwable e) {
		e.printStackTrace();
		scraper.interrupt();
		
		listener.onCrashed(instruction, scope, parent, source, e);
	}
	
	private synchronized final void triggerFinish(int successful, int stuck, int failed) {		
		scraper.finishedScrape(successful, stuck, failed);
		listener.onFinish(successful, stuck, failed);
	}
	
	private synchronized final boolean isDone() {
		return submitted == failed + stuckCnt + successful;
	}
}
