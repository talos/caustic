package net.caustic.database;

import java.util.Vector;

import net.caustic.instruction.Instruction;
import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;
import net.caustic.util.Encoder;

/**
 * Implementations of the {@link Database} interface provide a method
 * to get a new {@link DatabaseView}.
 * @author talos
 * @see #newDefaultScope()
 *
 */
public abstract class Database {

	private final Vector listeners = new Vector();
	private final ScopeFactory scopeFactory;
	
	/**
	 * A default name for the {@link Scope} scope column.
	 */
	public final static String SCOPE_COLUMN_NAME = "scope";
	
	/**
	 * The name for the default {@link Scope}.
	 */
	public final static String DEFAULT_SCOPE = "default";

	public Database() {
		this.scopeFactory = new IntScopeFactory();
	}
	
	public Database(ScopeFactory scopeFactory) {
		this.scopeFactory = scopeFactory;
	}
	
	/**
	 * Add a {@link DatabaseListener} to all this {@link Database}'s
	 * {@link DatabaseView}s and their children.
	 */
	public final void addListener(DatabaseListener listener) {
		if(listener == null) {
			throw new NullPointerException();
		}
		listeners.add(listener);
	}

	/**
	 * Return the value for <code>key</code> within <code>scope</code> or its
	 * enclosing scope.  Returns <code>null</code> otherwise.
	 * @param scope The {@link Scope} to look within the database.
	 * @param key The {@link String} key to look for within {@link Scope}.
	 * @return A {@link String} value, or <code>null</code> if the <code>key</code> does not exist
	 * in <code>scope</code>
	 * @throws DatabaseException if there was an reading the {@link Database}.
	 */
	public abstract String get(Scope scope, String key) throws DatabaseException;
	
	/**
	 * Return all the {@link StoppedInstruction}s that could be, but have not yet been,
	 * executed in the <code>scope</code>.
	 * @param scope
	 * @return An array of {@link StoppedInstruction}s.
	 * @throws DatabaseException if there was an reading the {@link Database}.
	 */
	public abstract StoppedInstruction[] getStoppedInstructions(Scope scope) throws DatabaseException;

	/**
	 * Get an array of {@link String} encoded cookies for <code>scope</code> and
	 * its ancestors in the following format:
	 * <p>
	 * <code>name=value</code>
	 * @param scope The {@link Scope} to check, in addition to all parents.  Children's cookies take
	 * precedence.
	 * @param host The {@link String} host of the cookie.
	 * @param encoder The {@link Encoder} to use for encoding.
	 * @return An array of {@link String}s.
	 * @throws DatabaseException if there was an reading the {@link Database}.
	 */
	public abstract String[] getCookies(Scope scope, String host, Encoder encoder) throws DatabaseException;
	
	/**
	 * Remove <code>instruction</code> from the set of {@link StoppedInstruction}s for <code>scope</code>.
	 * @param scope The {@link Scope} from which to pull <code>instruction</code>.
	 * @param instruction The {@link StoppedInstruction} to remove from <code>scope</code>.
	 * @throws DatabaseException if there was an error reading the {@link Database}.
	 */
	public final void restart(Scope scope, StoppedInstruction stopped)
			throws DatabaseException {
		onRestart(scope, stopped.source, stopped.instruction);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onRestart(scope, stopped.instruction, stopped.source);
		}
	}
	
	/**
	 * Add a cookie with <code>name</code> and <code>value</code> for <code>host</code>.
	 * @param scope The {@link Scope} in which the cookie is accessible.
	 * @param host The {@link String} host of the cookie.
	 * @param name The {@link String} name of the cookie.
	 * @param value The {@link String} value of the cookie.
	 * @throws DatabaseException if there was an error reading the {@link Database}.
	 */
	public final void addCookie(Scope scope, String host, String name, String value)
			throws DatabaseException {
		onAddCookie(scope, host, name, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onAddCookie(scope, host, name, value);
		}
	}
	
	/**
	 * Add a key-value mapping for {@link Scope}.
	 */
	public final void put(Scope scope, String key, String value) throws DatabaseException {
		onPut(scope, key, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPut(scope, key, value);
		}
	}
	
	public final void stopInstruction(Scope scope, String source, Instruction instruction)
			throws DatabaseException {
		onStop(scope, source, instruction);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onStop(scope, source, instruction);
		}
	}

	/**
	 * A fresh {@link Scope} with the name {@link #DEFAULT_SCOPE} and no parent {@link Scope}.
	 */
	public final Scope newDefaultScope() throws DatabaseException {
		Scope scope = scopeFactory.get(DEFAULT_SCOPE);
		
		onNewDefaultScope(scope);

		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onNewDefaultScope(scope);
		}
		return scope;
	}
	
	/**
	 */
	public final Scope newScope(Scope parent, String key) throws DatabaseException {
		Scope scope = scopeFactory.get(key);
		
		onNewScope(parent, scope);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onNewScope(parent, scope);
		}
		return scope;
	}
	
	/**
	 */
	public final Scope newScope(Scope parent, String key, String value)
			throws DatabaseException {
		Scope scope = scopeFactory.get(key);
		
		onNewScope(parent, scope, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onNewScope(parent, scope, value);
		}
		return scope;
	}

	protected abstract void onAddCookie(Scope scope, String host, String name, String value) throws DatabaseException;
	protected abstract void onStop(Scope scope, String source, Instruction instruction) throws DatabaseException;
	protected abstract void onRestart(Scope scope, String source, Instruction instruction) throws DatabaseException;
	protected abstract void onPut(Scope scope, String key, String value) throws DatabaseException;
	protected abstract void onNewDefaultScope(Scope scope) throws DatabaseException;
	protected abstract void onNewScope(Scope parent, Scope scope) throws DatabaseException;
	protected abstract void onNewScope(Scope parent, Scope scope, String value) throws DatabaseException;
}