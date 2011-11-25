package net.caustic.database;

import java.util.Hashtable;

import net.caustic.scope.Scope;

/**
 * A {@link Database} implementation that creates a new {@link InMemoryDatabaseView}
 * for each call of {@link #newScope()}.
 * @author talos
 *
 */
public class InMemoryDatabase extends Database {
	
	/**
	 * Tree of scope parent relationships.  {@link Scope} => {@link Scope}.
	 */
	private final Hashtable tree = new Hashtable();
	
	/**
	 * Tree of data nodes. {@link Scope} => {@link Hashtable}.
	 */
	private final Hashtable nodes = new Hashtable();
	
	public InMemoryDatabase() {
		addListener(new InMemoryDatabaseListener(tree, nodes));
	}

	/**
	 * Look in a data node for a value.  If it's not there, traverse
	 * up the tree.
	 */
	public String get(Scope scope, String key) {
		while(scope != null) {
			Hashtable dataNode = (Hashtable) nodes.get(scope);
			String value = (String) dataNode.get(key);
			if(value != null) {
				return value;
			} else {
				scope = (Scope) tree.get(scope); // could be null, would break loop.
			}
		}
		return null;
	}
}
