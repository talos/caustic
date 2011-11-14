package net.caustic.instruction;

import net.caustic.ScraperListener;
import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.scope.Scope;

/**
 * {@link Executable} binds an {@link Instruction} to a {@link String}
 * source, {@link DatabaseView}, and {@link HttpBrowser}, 
 * allowing it to be tried and retried.
 * A successful {@link #execute()} supplies children {@link Executable}s
 * that should be tried next.
 * @author realest
 *
 */
public final class Executable implements Runnable {

	private final Instruction instruction;
	private final Database db;
	private final Scope scope;
	private final HttpBrowser browser;
	private final String source;
	private final ScraperListener scraper;
	
	public Executable(Instruction instruction, Database db, Scope scope,
			String source, HttpBrowser browser, ScraperListener scraper) {
		this.instruction = instruction;
		this.source = source;
		this.db = db;
		this.scope = scope;
		this.browser = browser;
		this.scraper = scraper;
	}
	
	public void run() {
		try {
			InstructionResult result = instruction.execute(source, db, scope, browser);
			
			if(result.isSuccess()) {
				Instruction[] children = result.getChildren();
				String[] results = result.getResults();
				boolean shouldStoreValues = result.shouldStoreValues();
				String name = result.getName();
				
				for(int i = 0 ; i < results.length ; i ++) {
					final Scope childScope;
					// generate result views.
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
					for(int j = 0 ; j < children.length ; j ++) {
						scraper.scrape(children[j], childScope, results[i],
								browser.copy());
					}
				}
				scraper.success(instruction, scope, name, browser);
			} else if(result.isMissingTags()) {
				scraper.missing(instruction, scope, source, browser, result.getMissingTags());
			} else {
				scraper.failed(instruction, scope, source, result.getFailedBecause());
			}
		} catch(DatabaseException e) {
			scraper.crashed(instruction, scope, source, e);
		} catch(InterruptedException e) {
			scraper.crashed(instruction, scope, source, e);
		} catch(Throwable e) {
			scraper.crashed(instruction, scope, source, e);
		}
	}
}
