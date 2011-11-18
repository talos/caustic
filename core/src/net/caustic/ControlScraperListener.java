package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * A {@link ScraperListener} that handles control flow for {@link AbstractScraper}.
 * Encloses the provided {@link ScraperListener} and wraps its events.
 * @author talos
 *
 */
final class ControlScraperListener implements ScraperListener {
	private final ScraperListener extraListener;
	private final AbstractScraper scraper;
	
	private volatile int submitted = 0;
	
	// a count of the number of elements internal to {@link #stuck}, which is multidimensional
	// and thus hard to count.
	private volatile int stuckCnt = 0;
	private volatile int successful = 0;
	private int failed = 0;
	
	/**
	 * A {@link Hashtable} of stuck {@link Executable}s.  The first dimension of
	 * keys are {@link Scope}s, and the second dimension is an array of Strings
	 * of missing tags.  The final values are the {@link Executable}s.
	 */
	private final Hashtable stuck = new Hashtable();
	
	public ControlScraperListener(ScraperListener listener, AbstractScraper scraper) {
		this.extraListener = listener;
		this.scraper = scraper;
	}
	
	public synchronized final void onScrape(Instruction instruction, Database db, Scope scope, Scope parent,
			String source, HttpBrowser browser) {
		submitted++;
		extraListener.onScrape(instruction, db, scope, parent, source, browser);
		scraper.submit(new Executable(instruction, db, scope, parent, source, browser, this));
	}
	
	public synchronized final void onSuccess(Instruction instruction, Database db, Scope scope, Scope parent,
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
			
			extraListener.onSuccess(instruction, db, scope, parent, source, key, results);
			
			if(isDone()) {
				onFinish(successful, stuck.size(), failed);
			}
		} catch(DatabaseException e) {
			onCrashed(instruction, scope, parent, null, e);
		}
	}
	
	public synchronized void onMissingTags(Instruction instruction,
			Database db, Scope scope, Scope parent, String source,
			HttpBrowser browser, String[] missingTags) {
		
		// add an executable missing tags to the hash of stuck executables.
		if(!stuck.containsKey(scope)) {
			stuck.put(scope, new Hashtable());
		}
		
		Hashtable stuckInScope = (Hashtable) stuck.get(scope);
		stuckInScope.put(missingTags, new Executable(instruction, db, scope, parent, source, browser, this));
		stuckCnt++;
		
		extraListener.onMissingTags(instruction, db, scope, parent, source, browser, missingTags);
		
		if(isDone()) {
			onFinish(successful, stuck.size(), failed);
		}
	}
	
	public synchronized void onFailed(Instruction instruction, Database db,
			Scope scope, Scope parent, String source, String failedBecause) {
		failed++;
		
		extraListener.onFailed(instruction, db, scope, parent, source, failedBecause);
		
		if(isDone()) {
			onFinish(successful, stuck.size(), failed);
		}
	}
	
	public synchronized final void onCrashed(Instruction instruction, Scope scope, Scope parent, String source, Throwable e) {
		e.printStackTrace();
		scraper.interrupt();
		
		extraListener.onCrashed(instruction, scope, parent, source, e);
	}
	
	public synchronized final void onFinish(int successful, int stuck, int failed) {		
		scraper.finishedScrape(successful, stuck, failed);
		extraListener.onFinish(successful, stuck, failed);
	}
	
	public synchronized final boolean isDone() {
		return submitted == failed + stuckCnt + successful;
	}
}
