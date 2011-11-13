package net.caustic.database;

import java.util.Vector;

import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;

/**
 * Implementations of the {@link Database} interface provide a method
 * to get a new {@link DatabaseView}.
 * @author talos
 * @see #newScope()
 *
 */
public abstract class Database {

	private final Vector listeners = new Vector();
	private final ScopeFactory scopeFactory;
	
	/**
	 * A default name for the {@link Scope} scope column.
	 */
	public final static String DEFAULT_SCOPE_NAME = "scope";

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
	 */
	public final void put(Scope scope, String key, String value) throws DatabaseException {
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).put(scope, key, value);
		}
	}

	/**
	 * A fresh {@link Scope}.
	 */
	public final Scope newScope() throws DatabaseException {
		Scope scope = scopeFactory.get();
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).newScope(scope);
		}
		return scope;
		//return new DatabaseView(this, scope);
		
	}
	
	/**
	 */
	public final Scope newScope(Scope parent, String key) throws DatabaseException {
		Scope child = scopeFactory.get();
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).newScope(parent, key, child);
		}
		return child;
	}
	
	/**
	 */
	public final Scope newScope(Scope parent, String key, String value)
			throws DatabaseException {
		Scope child = scopeFactory.get();
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).newScope(parent, key, value, child);
		}
		return child;
	}
}