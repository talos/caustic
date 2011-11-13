package net.caustic.database;

import net.caustic.log.Logger;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

public class LoggingDatabaseListener implements DatabaseListener {

	private final Logger logger;
	private final String delimiter;
	public LoggingDatabaseListener(Logger logger, String delimiter) {
		this.logger = logger;
		this.delimiter = delimiter;
	}
	
	@Override
	public void put(Scope scope, String key, String value)
			throws DatabaseListenerException {
		print(null, scope, key, value);
	}

	@Override
	public void newScope(Scope scope) throws DatabaseListenerException {
		//print(new String[] { scope.asString(), key, value } );
	}

	@Override
	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		print(parent, child, key, null);
	}

	@Override
	public void newScope(Scope parent, String key, String value, Scope child)
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
				, delimiter));
	}
}
