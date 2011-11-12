package net.caustic.database;

import java.util.Hashtable;

import net.caustic.scope.Scope;

/**
 * This {@link DatabaseListener} implements persistence in-memory.
 * @author realest
 *
 */
class InMemoryDatabaseListener implements DatabaseListener {

	private final Hashtable tree;
	private final Hashtable nodes;
	
	public InMemoryDatabaseListener(Hashtable tree, Hashtable nodes) {
		this.tree = tree;
		this.nodes = nodes;
	}
	
	public void put(Scope scope, String key, String value)
			throws DatabaseListenerException {
		Hashtable dataNode = (Hashtable) nodes.get(scope);
		dataNode.put(key, value);
	}
	
	public void newScope(Scope scope)
			throws DatabaseListenerException {
		nodes.put(scope, new Hashtable());
	}

	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		tree.put(parent, child);
		nodes.put(child, new Hashtable());
	}

	public void newScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		tree.put(child, parent); // search for parent by child.
		Hashtable dataNode = new Hashtable();
		dataNode.put(key, value);
		nodes.put(child, dataNode);
	}

}
