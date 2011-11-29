package net.caustic;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * A binding to run {@link Instruction}.
 * @author talos
 *
 */
final class Executable implements Runnable {
	
	private final Instruction instruction;
	private final Scope scope;
	private final String source;
	private final AbstractScraper scraper;
	private final Database db;
	private final HttpBrowser browser;
	private final ScraperListener listener;
	private final boolean autoRun;
	
	public Executable(Instruction instruction, Scope scope, String source,
			boolean autoRun,
			AbstractScraper scraper, Database db, HttpBrowser browser, ScraperListener listener) {
		this.instruction = instruction;
		this.autoRun = autoRun;
		this.scope = scope;
		this.source = source;
		this.db = db;
		this.browser = browser;
		this.scraper = scraper;
		this.listener = listener;
	}
	
	public void run() {
		//scraper.triggerStarted(frozen);
		try {
			scraper.handle(instruction, scope, source, listener,
					autoRun,
					instruction.execute(source, db, scope, browser));
			
			//.handle(frozen, frozen.execute(db, browser));
		} catch(DatabaseException e) {
			e.printStackTrace();
			scraper.interrupt(); //(frozen, e);
		} catch(InterruptedException e) {
			e.printStackTrace();
			scraper.interrupt();
			//process.handle(frozen, e);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws DatabaseException
	 * @throws InterruptedException
	 * @throws Throwable
	 */
	/*public InstructionResult execute(ScraperProcess process)
			throws DatabaseException, InterruptedException, Throwable {
		process.handle(instruction, scope, source, cookies, )*/
		//try {
		//process.handle(instruction.execute(source, db, scope, cookies, browser));
		/*
		if(result.isSuccess()) {
			
		}
		
		return result;*/
			// Tell listener this instruction was successful, if it is visible.
			//process.triggerSuccess(instruction, db, scope, parent, source, name, results);
		/*} else if(result.isMissingTags()) {
			
			// Tell listener this instruction could not be completed due to missing tags.
			//process.triggerMissingTags(instruction, db, scope, parent, source, browser, result.getMissingTags());
		} else {
			
			// Tell listener this instruction failed.
			//process.triggerFailed(instruction, db, scope, parent, source, result.getFailedBecause());
		}*/
			/*
		} catch(DatabaseException e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		} catch(InterruptedException e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		} catch(Throwable e) {
			process.triggerCrashed(instruction, scope, parent, source, e);
		}*/
	//}
}