package net.microscraper.database;

/**
 * {@link DatabaseViewListener}s can be hooked into a {@link DatabaseView}.
 * Each hooked {@link DatabaseViewListener} is called after the method of
 * the same name in {@link DatabaseView}, and {@link DatabaseView#spawnChild(String)}
 * and {@link DatabaseView#spawnChild(String, String)} have an additional
 * {@link DatabaseView} parameter that is the result of the {@link DatabaseView}
 * method.
 * @author talos
 *
 */
public interface DatabaseViewListener {
	
	/**
	 * This method is called after the {@link DatabaseView#put(String, String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param key the same {@link String} as was the key for {@link DatabaseView}.
	 * @param value the same {@link String} as was the value for {@link DatabaseView}.
	 * @throws DatabaseViewHookException if there was an exception running the hook.
	 */
	public void put(String key, String value) throws DatabaseViewHookException;
	

	/**
	 * This method is called after the {@link DatabaseView#spawnChild(String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param name the same {@link String} as was the name for {@link DatabaseView}.
	 * @param child the {@link DatabaseView} that resulted from {@link DatabaseView#spawnChild(String)}.
	 * @throws DatabaseViewHookException if there was an exception running the hook.
	 */
	public void spawnChild(String name, DatabaseView child) throws DatabaseViewHookException;
	

	/**
	 * This method is called after the {@link DatabaseView#spawnChild(String, String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param name the same {@link String} as was the name for {@link DatabaseView}.
	 * @param value the same {@link String} as was the value for {@link DatabaseView}.
	 * @param child the {@link DatabaseView} that resulted from {@link DatabaseView#spawnChild(String, String)}.
	 * @throws DatabaseViewHookException if there was an exception running the hook.
	 */
	public void spawnChild(String name, String value, DatabaseView child) throws DatabaseViewHookException;
}
