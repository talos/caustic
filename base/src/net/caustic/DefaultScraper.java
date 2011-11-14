package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseListenerException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Executable;
import net.caustic.instruction.Instruction;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

public abstract class DefaultScraper implements ScraperListener, Loggable {
	static private final String SEPARATOR = "\t";
	
	private int submitted = 0;
	private int stuckCnt = 0;
	private int successful = 0;
	private int failed = 0;
	
	private final Vector listeners = new Vector();
	private final Database db;
	
	private final MultiLog log = new MultiLog();
	
	/**
	 * A {@link Hashtable} of stuck {@link Executable}s.  The first dimension of
	 * keys are {@link Scope}s, and the second dimension is an array of Strings
	 * of missing tags.  The final values are the {@link Executable}s.
	 */
	private final Hashtable stuck = new Hashtable();
	
	public DefaultScraper(Database db) {
		this.db = db;
		db.addListener(this);
	}
	
	public final void scrape(Instruction instruction, Scope scope,
			String source, HttpBrowser browser) {
		submitted++;
		log.i("Scraping " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
		submit(new Executable(instruction, db, scope, source, browser, this));
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).scrape(instruction, scope, source, browser);
		}
	}
	
	public final void success(Instruction instruction, Scope scope,
			String source, HttpBrowser browser) {
		successful++;
		log.i("Finished " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).success(instruction, scope, source, browser);
		}
		
		if(isDone()) {
			terminated(successful, stuck.size(), failed);
		}
	}
	
	public final void missing(Instruction instruction, Scope scope,
			String source, HttpBrowser browser, String[] missingTags) {
		log.i("Stuck on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because missing tags " + StringUtils.quoteJoin(missingTags, ", "));
		
		// add an executable missing tags to the hash of stuck executables.
		if(!stuck.containsKey(scope)) {
			stuck.put(scope, new Hashtable());
		}
		Hashtable stuckInScope = (Hashtable) stuck.get(scope);
		stuckInScope.put(missingTags, new Executable(instruction, db, scope, source, browser, this));
		stuckCnt++;
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).missing(instruction, scope, source, browser, missingTags);
		}
		
		if(isDone()) {
			terminated(successful, stuck.size(), failed);
		}
	}

	public final void failed(Instruction instruction, Scope scope,
			String source, String failedBecause) {
		failed++;
		log.i("Failed on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because of " + StringUtils.quote(failedBecause));

		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).failed(instruction, scope, source, failedBecause);
		}
		
		if(isDone()) {
			terminated(successful, stuck.size(), failed);
		}
	}

	public final void crashed(Instruction instruction, Scope scope, String source, Throwable e) {
		interrupt();
		log.i("Crashed on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because of " + StringUtils.quote(e.toString()));
		e.printStackTrace();
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).crashed(instruction, scope, source, e);
		}
	}
	
	public void terminated(int successful, int missing, int failed) {
		log.i("Finished scraping, there were " + successful + " executions, " + missing + " that " +
				"were missing tags, and " + failed + " failures.");

		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).terminated(successful, missing, failed);
		}
	}
	
	public void put(Scope scope, String key, String value)
			throws DatabaseListenerException {
		try {
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
						submit((Executable) stuck.get((Executable) stuck.get(missingTags)));
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
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Error reading database trying to retry", e);
		}
		
		print(null, scope, key, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).put(scope, key, value);
		}
	}

	public void newScope(Scope scope) throws DatabaseListenerException { 
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).newScope(scope);
		}
	}

	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		
		print(parent, child, key, null);
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).newScope(parent, key, child);
		}
	}

	public void newScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		print(parent, child, key, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++ ) {
			((ScraperListener) listeners.elementAt(i)).newScope(parent, key, value, child);
		}
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public final void addListener(ScraperListener listener) {
		listeners.add(listener);
	}
	
	public final boolean isDone() {
		return submitted == failed + stuckCnt + successful;
	}
	
	/**
	 * Scrape an {@link Instruction} using a {@link Browser}.
	 * @param instruction
	 * @param input
	 * @param browser
	 * @throws DatabaseException
	 */
	public final void scrape(Instruction instruction, Hashtable table, HttpBrowser browser) throws DatabaseException {
		Scope scope = db.newScope();
		Enumeration e = table.keys();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			db.put(scope, key, (String) table.get(key));
		}
		scrape(instruction, scope, null, browser);
	}
	
	protected abstract void submit(Executable executable);
	
	protected abstract void interrupt();

	private void print(Scope parent, Scope scope, String key, String value) {
		log.i(StringUtils.join(
				new String[] {
						parent == null ? "" : parent.asString(),
						scope == null  ? "" : scope.asString(),
						key,
						value }
				, SEPARATOR));
	}
}
