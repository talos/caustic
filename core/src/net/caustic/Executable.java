package net.caustic;

import net.caustic.database.Database;
import net.caustic.database.ReadyExecution;
import net.caustic.http.HttpBrowser;
import net.caustic.scope.Scope;

class Executable implements Runnable {
	
	private final ReadyExecution ready;
	private final HttpBrowser browser;
	private final Database db;
	private final Scope scope;
	
	public Executable(HttpBrowser browser, Database db, Scope scope, ReadyExecution ready) {
		this.browser = browser;
		this.db = db;
		this.scope = scope;
		this.ready = ready;
	}
	
	public void run() {
		db.remove(scope, ready);
		ready.instruction.execute(ready.source, db, scope, browser);
	}
}
