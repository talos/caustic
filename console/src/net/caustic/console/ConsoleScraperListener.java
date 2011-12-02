package net.caustic.console;

import java.util.ArrayList;
import java.util.List;

import net.caustic.LogScraperListener;
import net.caustic.scope.Scope;

/**
 * An extension of {@link LogScraperListener} that keeps track of open default scopes.
 * Because there is no external input, there is no way for a default scope to reopen
 * once it has closed.
 * @author realest
 *
 */
public class ConsoleScraperListener extends LogScraperListener {

	private final List<Scope> open = new ArrayList<Scope>();
	
	@Override
	public synchronized void onNewDefaultScope(Scope scope) {
		super.onNewDefaultScope(scope);
		open.add(scope);
	}
	
	@Override
	public synchronized void onScopeComplete(Scope scope, int successes, int stuck, int failed) {
		super.onScopeComplete(scope, successes, stuck, failed);
		open.remove(scope);
	}
	
	@Override
	public synchronized void onCrash(Scope scope, String instruction, String uri, Throwable e) {
		super.onCrash(scope, instruction, uri, e);
		//e.printStackTrace();
		open.clear();
	}
	
	public synchronized int getNumberOfOpenScopes() {
		return open.size();
	}
}
