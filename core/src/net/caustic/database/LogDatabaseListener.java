package net.caustic.database;

import net.caustic.log.Logger;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A {@link DatabaseListener} implementation that logs {@link DatabaseListener} events.
 * @author talos
 *
 */
public class LogDatabaseListener implements DatabaseListener {
	
	
	static private final String SEPARATOR = "\t";

	public final Logger logger;
	
	/**
	 * Create a {@link DatabaseListener} that logs to <code>logger</code>
	 * @param logger The {@link Logger} to log to.
	 */
	public LogDatabaseListener(Logger logger) {
		this.logger = logger;
	}
	
	public void onPut(Scope scope, String key, String value)
			throws DatabaseListenerException {
		print(null, scope, key, value);		
	}

	public void onNewScope(Scope scope) throws DatabaseListenerException { 
		print(null, scope, null, null);
	}

	public void onNewScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		print(parent, child, key, null);
	}

	public void onNewScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		print(parent, child, key, value);
	}

	private void print(Scope parent, Scope scope, String key, String value) {
		logger.i(StringUtils.join(
				new String[] {
						parent == null ? "" : parent.asString(),
						scope == null  ? "" : scope.asString(),
						key,
						value }
				, SEPARATOR));
	}
}
