package net.caustic.database;

import net.caustic.instruction.Instruction;
import net.caustic.log.Logger;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A {@link DatabaseListener} implementation that logs {@link DatabaseListener} events.
 * @author talos
 *
 */
public class LogDatabaseListener implements DatabaseListener {
	
	public final Logger logger;
	
	/**
	 * Create a {@link DatabaseListener} that logs to <code>logger</code>
	 * @param logger The {@link Logger} to log to.
	 */
	public LogDatabaseListener(Logger logger) {
		this.logger = logger;
	}
	
	public void onPut(Scope scope, String key, String value) {
		logger.i("Mapped " + StringUtils.quote(key) + ":" +StringUtils.quote(value)
				+ " in " + StringUtils.quote(scope));
	}

	public void onStop(Scope scope, String source, Instruction instruction) {
		logger.i("Froze " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
	}

	public void onNewDefaultScope(Scope scope) {
		logger.i("New default scope " + StringUtils.quote(scope));
	}

	public void onNewScope(Scope parent, Scope scope) {
		logger.i("New scope " + StringUtils.quote(scope) + " in " + StringUtils.quote(parent));
		
	}

	public void onNewScope(Scope parent, Scope scope, String value) {
		logger.i("New scope " + StringUtils.quote(scope) + " in " + StringUtils.quote(parent) + " with " +
			" value " + value);
	}

	public void onAddCookie(Scope scope, String url, String name, String value) {
		logger.i("Adding cookie " + StringUtils.quote(name) + "=" + StringUtils.quote(value) + " in scope " +
				StringUtils.quote(scope) + " for URL " + StringUtils.quote(url));
		
	}

	public void onRestart(Scope scope, Instruction instruction, String source) {
		logger.i("Unfroze " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
		
	}
}
