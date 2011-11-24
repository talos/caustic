package net.caustic;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.scope.Scope;

/**
 * A {@link Runnable} binding of an {@link Instruction} to {@link Scope},
 * {@link ScraperListener}, {@link Database}, and a source.
 * @author talos
 *
 */
final class Executable implements Runnable {

	private final Instruction instruction;
	private final Database db;
	private final Scope parent;
	private final Scope scope;
	private final HttpBrowser browser;
	private final String source;
	private final ScraperProcess process;
	
	public Executable(Instruction instruction, Database db, Scope scope, Scope parent,
			String source, HttpBrowser browser, ScraperProcess process) {
		this.instruction = instruction;
		this.source = source;
		this.db = db;
		this.parent = parent;
		this.scope = scope;
		this.browser = browser;
		this.process = process;
	}
	
	/**
	 * When {@link #run()}, an {@link Executable} will execute its {@link #instruction}. 
	 * After execution, {@link #process} will be notified of the results.
	 * 
	 */
	public void run() {
		try {
			InstructionResult result = instruction.execute(source, db, scope, browser);
			
			if(result.isSuccess()) {
				Instruction[] children = result.getChildren();
				String[] results = result.getResults();
				
				// if shouldStoreValues is false, then name could be null.
				boolean shouldStoreValues = result.shouldStoreValues();
				String name = result.getName();
				
				for(int i = 0 ; i < results.length ; i ++) {
					final Scope childScope;
					// generate result scopes.
					final String resultValue = results[i];
					if(results.length == 1) { // don't spawn a new result for single match
						childScope = scope;
						if(shouldStoreValues) {
							db.put(childScope, name, resultValue);
						}
					} else {
						if(shouldStoreValues) {
							childScope = db.newScope(scope, name, resultValue);
						} else {
							childScope = db.newScope(scope, name);							
						}
					}
					// create & scrape children.
					for(int j = 0 ; j < children.length ; j ++) {
						
						// Tell listener to scrape the child when ready if the child is real,
						// otherwise do it automatically.
						final Instruction child = children[j];
						final HttpBrowser browserCopy = browser.copy();
						
						process.triggerReady(child, db, childScope, scope, results[i], browserCopy);
					}
				}
				// Tell listener this instruction was successful, if it is visible.
				process.triggerSuccess(instruction, db, scope, parent, source, name, results);
			} else if(result.isMissingTags()) {
				
				// Tell listener this instruction could not be completed due to missing tags.
				process.triggerMissingTags(instruction, db, scope, parent, source, browser, result.getMissingTags());
			} else {
				
				// Tell listener this instruction failed.
				process.triggerFailed(instruction, db, scope, parent, source, result.getFailedBecause());
			}
		} catch(DatabaseException e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		} catch(InterruptedException e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		} catch(Throwable e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		}
	}
}