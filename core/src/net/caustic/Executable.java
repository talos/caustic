package net.caustic;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
import net.caustic.scope.Scope;

public final class Executable implements Runnable {
	private Find find;
	private Load load;
	private final String serialized;
	private final String uri;
	private final String source;
	private final Database db;
	private final Scope scope;
	private final HttpBrowser browser;
	private final AbstractScraper scraper;
	private boolean hasBeenRun = false;
	
	public Executable(Find find, String source, Database database, Scope scope,
			HttpBrowser browser, AbstractScraper scraper) {
		this.find = find;
		this.source = source;
		this.db = database;
		this.scope = scope;
		this.browser = browser;
		this.scraper = scraper;
		this.serialized = find.serialized;
		this.uri = find.uri;
	}

	public Executable(Load load, String source, Database database, Scope scope,
			HttpBrowser browser, AbstractScraper scraper) {
		this.load = load;
		this.source = source;
		this.db = database;
		this.scope = scope;
		this.browser = browser;
		this.scraper = scraper;
		this.serialized = load.serialized;
		this.uri = load.uri;
	}
	
	public synchronized boolean hasBeenRun() {
		return hasBeenRun;
	}
	
	public void run() {
		synchronized(this) {
			if(hasBeenRun) {
				return;
			} else {
				hasBeenRun = true; // prevent double-run
			}
		}
		try {
			if(load != null) {
				load.execute(db, scope, browser);
			} else {
				find.execute(source, db, scope);
			}
		} catch(DatabaseException e) {
			scraper.crash(scope, serialized, uri, e);
		} catch(InterruptedException e) {
			scraper.interrupt();
		} catch(Throwable e) {
			scraper.crash(scope, serialized, uri, e);
		}
	}
}
