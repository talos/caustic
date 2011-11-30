package net.caustic;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

final class Executable implements Runnable {
	private final Instruction instruction;
	private final String source;
	private final Database db;
	private final Scope scope;
	private final HttpBrowser browser;
	private final AbstractScraper scraper;
	
	public Executable(Instruction instruction, String source, Database database, Scope scope,
			HttpBrowser browser, AbstractScraper scraper) {
		this.instruction = instruction;
		this.source = source;
		this.db = database;
		this.scope = scope;
		this.browser = browser;
		this.scraper = scraper;
	}
	
	public void run() {
		try {
			instruction.execute(source, db, scope, browser);
		} catch(DatabaseException e) {
			scraper.crash(scope, instruction, e);
		} catch(InterruptedException e) {
			scraper.interrupt();
		} catch(Throwable e) {
			scraper.crash(scope, instruction, e);
		}
	}
}
