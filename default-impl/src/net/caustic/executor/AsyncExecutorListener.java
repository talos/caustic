package net.caustic.executor;

import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.database.DatabaseListenerException;
import net.caustic.database.DatabaseListener;
import net.caustic.scope.Scope;

/**
 * A {@link DatabaseListener} to kick {@link AsyncExecutor} when new data is inserted.
 * @author talos
 *
 */
public class AsyncExecutorListener implements DatabaseListener {

	private AsyncExecutor executor;
	
	AsyncExecutorListener(AsyncExecutor executor) {
		this.executor = executor;
	}
	
	@Override
	public void put(Scope scope, String key, String value) throws DatabaseListenerException {
		try {
			executor.kick(key);
		} catch(DatabaseReadException e) {
			throw new DatabaseListenerException("Error reading database", e);
		}
	}
	
	/**
	 * No value was added, don't kick.
	 */
	@Override
	public void spawnChild(String name, DatabaseView child)
			throws DatabaseListenerException {
	}

	@Override
	public void spawnChild(String name, String value, DatabaseView child)
			throws DatabaseListenerException {
		// keep monitoring the child view.
		child.addListener(new AsyncExecutorListener(executor));
		
		try {
			executor.kick(name);
		} catch(DatabaseReadException e) {
			throw new DatabaseListenerException("Error reading database", e);
		}
	}
}
