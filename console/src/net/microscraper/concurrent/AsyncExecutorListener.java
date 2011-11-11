package net.microscraper.concurrent;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.DatabaseViewHookException;
import net.microscraper.database.DatabaseViewListener;

/**
 * A {@link DatabaseViewListener} to kick {@link AsyncExecutor} when new data is inserted.
 * @author talos
 *
 */
public class AsyncExecutorListener implements DatabaseViewListener {

	private AsyncExecutor executor;
	
	AsyncExecutorListener(AsyncExecutor executor) {
		this.executor = executor;
	}
	
	@Override
	public void put(String key, String value) throws DatabaseViewHookException {
		try {
			executor.kick(key);
		} catch(DatabaseReadException e) {
			throw new DatabaseViewHookException("Error reading database", e);
		}
	}
	
	/**
	 * No value was added, don't kick.
	 */
	@Override
	public void spawnChild(String name, DatabaseView child)
			throws DatabaseViewHookException {
	}

	@Override
	public void spawnChild(String name, String value, DatabaseView child)
			throws DatabaseViewHookException {
		// keep monitoring the child view.
		child.addListener(new AsyncExecutorListener(executor));
		
		try {
			executor.kick(name);
		} catch(DatabaseReadException e) {
			throw new DatabaseViewHookException("Error reading database", e);
		}
	}
}
