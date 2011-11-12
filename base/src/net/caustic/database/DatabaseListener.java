package net.caustic.database;

import net.caustic.scope.Scope;

/**
 * {@link DatabaseListener}s can be hooked into a {@link DatabaseView}.
 * Each hooked {@link DatabaseListener} is called after the method of
 * the same name in {@link DatabaseView}, and {@link DatabaseView#spawnChild(String)}
 * and {@link DatabaseView#spawnChild(String, String)} have an additional
 * {@link DatabaseView} parameter that is the result of the {@link DatabaseView}
 * method.
 * @author talos
 *
 */
public interface DatabaseListener {
	
	/**
	 * This method is called after the {@link DatabaseView#put(String, String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param view the {@link Scope} scope within which <code>put</code> was called.
	 * @param key the same {@link String} as was the key for {@link DatabaseView}.
	 * @param value the same {@link String} as was the value for {@link DatabaseView}.
	 * @throws DatabaseListenerException if there was an exception running the hook.
	 */
	public void put(Scope scope, String key, String value) throws DatabaseListenerException;

	/**
	 * Called after fresh {@link DatabaseView} created.
	 * @param scope
	 * @param name
	 * @throws DatabaseListenerException
	 */
	public void newView(Scope scope) throws DatabaseListenerException;
	
	/**
	 * This method is called after the {@link DatabaseView#spawnChild(String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param parent the {@link Scope} scope within which <code>newScope</code> was called.
	 * @param name the same {@link String} as was the name for {@link DatabaseView}.
	 * @param child the {@link Scope} that resulted from {@link DatabaseView#spawnChild(String)}.
	 * @throws DatabaseListenerException if there was an exception running the hook.
	 */
	public void newScope(Scope parent, String key, Scope child) throws DatabaseListenerException;
	
	/**
	 * This method is called after the {@link DatabaseView#spawnChild(String, String)}
	 * of the {@link DatabaseView} that this is hooked into.
	 * @param parent the {@link DatabaseView} upon which <code>newScope</code> was called.
	 * @param name the same {@link String} as was the name for {@link DatabaseView}.
	 * @param value the same {@link String} as was the value for {@link DatabaseView}.
	 * @param child the {@link Scope} that resulted from {@link DatabaseView#spawnChild(String, String)}.
	 * @throws DatabaseListenerException if there was an exception running the hook.
	 */
	public void newScope(Scope parent, String key, String value, Scope child) throws DatabaseListenerException;
}
