package net.caustic.database;

import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * {@link DatabaseListener}s can be hooked into a {@link Database}.
 * Each hooked {@link DatabaseListener} is called after the method of
 * the same name in {@link Database}.
 * @author talos
 *
 */
public interface DatabaseListener {
	
	/**
	 * This method is called after the {@link Database#put(String, String)}
	 * of the {@link Database} that this is hooked into.
	 * @param scope the {@link Scope} scope within which <code>put</code> was called.
	 * @param key the same {@link String} as was the key for {@link Database}.
	 * @param value the same {@link String} as was the value for {@link Database}.
	 */
	public void onPut(Scope scope, String key, String value);
	
	/**
	 * Called after fresh {@link Scope} created.
	 * @param scope
	 * @throws DatabaseListenerException
	 */
	public void onNewDefaultScope(Scope scope);
	
	/**
	 * This method is called after the {@link Database#newScope(Scope, String)}
	 * of the {@link Database} that this is hooked into.
	 * @param parent the {@link Scope} scope within which <code>newScope</code> was called.
	 * @param scope the new {@link Scope}.
	 */
	public void onNewScope(Scope parent, Scope scope);
	
	/**
	 * This method is called after the {@link Database#newScope(String, String, String)}
	 * of the {@link Database} that this is hooked into.
	 * @param parent the {@link Database} upon which <code>newScope</code> was called.
	 * @param scope the new {@link Scope}.
	 * @param value the same {@link String} as was the value for {@link Database}.
	 */
	public void onNewScope(Scope parent, Scope scope, String value);

	/**
	 * This method is called after the {@link Database#addCookie(Scope, String, String, String)
	 * of the {@link Database} that this is hooked into.
	 * @param scope The {@link Scope} for the cookie.
	 * @param host The host name this cookie should be used with.
	 * @param name The name of the cookie.
	 * @param value The value of the cookie.
	 */
	public void onAddCookie(Scope scope, String host, String name, String value);

	public void onPutReady(Scope scope, String source, String instruction, String uri);

	public void onPutMissing(Scope scope, String source, String instruction, String uri, String[] missingTags);

	public void onPutFailed(Scope scope, String source, String instruction, String uri, String failedBecause);
	
	public void onPutSuccess(Scope scope, String source, String instruction, String uri);
	
	public void onScopeComplete(Scope scope);
}
