package net.caustic.database;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.HashtableUtils;
import net.caustic.util.VectorUtils;

/**
 * A {@link Database} implementation that creates a new {@link InMemoryDatabaseView}
 * for each call of {@link #newDefaultScope()}.
 * @author talos
 *
 */
public final class MemoryDatabase extends Database {
	
	private static final Scope[] NO_SCOPES = new Scope[0];
	
	//private static final ReadyExecution[] EMPTY_INSTRUCTION_ARRAY = new ReadyExecution[0];
	
	/**
	 * Tree of scope parent relationships.  Child => Parent.<br>
	 * <code>Hashtable&lt;Scope, Scope&gt;</code>
	 */
	private final Hashtable parentsByChild = new Hashtable();

	/**
	 * Tree of scope parent relationships. Parent => Children.<br>
	 * <code>Hashtable&lt;Scope, Vector&lt;Scope&gt;&gt;</code>
	 */
	private final Hashtable childrenByParent = new Hashtable();
	
	/**
	 * Hashtables of tags.<br>
	 * <code>Hashtable&lt;Scope, Hashtable&lt;String, String&gt;&gt;</code>
	 */
	private final Hashtable tags = new Hashtable();
	
	/**
	 * Hashtables of cookies.<br>
	 * Inner hashtable is hosts.<br>
	 * <code>Hashtable&lt;Scope, Hashtable&lt;String, Hashtable&lt;String, String&gt;&gt;&gt;</code>
	 */
	private final Hashtable cookies = new Hashtable();
	
	/**
	 * Hashtable of {@link ReadyExecution} vectors.<br>
	 * <code>Hashtable&lt;Scope, Vector&lt;ReadyExecution&gt;&gt;</code>
	 */
	private final Hashtable submitted = new Hashtable();
	
	/**
	 * Hashtable of {@link Instruction} vectors.<br>
	 * <code>Hashtable&lt;Scope, Vector&lt;Instruction&gt;&gt;</code>
	 */
	private final Hashtable success = new Hashtable();

	/**
	 * Hashtable of {@link StuckExecution} vectors.<br>
	 * <code>Hashtable&lt;Scope, Vector&lt;StuckExecution&gt;&gt;</code>
	 */
	private final Hashtable stuck = new Hashtable();

	/**
	 * Hashtable of {@link FailedExecution} vector.<br>
	 * <code>Hashtable&lt;Scope, Vector&lt;FailedExecution&gt;&gt;</code>
	 */
	private final Hashtable failed = new Hashtable();
	
	/**
	 * Look in a data node for a value.  If it's not there, traverse
	 * up the tree.
	 */
	public String get(Scope scope, String key) {
		while(scope != null) {
			Hashtable dataNode = (Hashtable) tags.get(scope);
			String value = (String) dataNode.get(key);
			if(value != null) {
				return value;
			} else {
				scope = (Scope) parentsByChild.get(scope); // could be null, would break loop.
			}
		}
		return null;
	}
	
	public String[] getCookies(Scope scope, final String host, final Encoder encoder)
			throws DatabaseException {
		Hashtable allCookies = new Hashtable();
		
		// Travel up the tree
		while(scope != null) {
			if(cookies.containsKey(scope)) {
				Hashtable scopeCookies = (Hashtable) cookies.get(scope);
				
				// check to see if there's an entry for host
				if(scopeCookies.containsKey(host)) {
					Hashtable hostCookies = (Hashtable) scopeCookies.get(host);
					
					// precedence is given to prior (lower on the tree) cookies.
					allCookies = HashtableUtils.combine(new Hashtable[] { hostCookies, allCookies } );
				}
			}
			scope = (Scope) parentsByChild.get(scope); // get the parent scope
		}
		
		// now that allCookies has been stocked, convert it into an array of strings.
		final String[] result = new String[allCookies.size()];
		
		Enumeration e = allCookies.keys();
		int i = 0;
		while(e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String value = (String) allCookies.get(name);
			result[i] = encoder.encode(name) + '=' + encoder.encode(value);
			i++;
		}
		
		return result;
	}

	void onAddCookie(Scope scope, String host, String name,
			String value) {
		// we have cookies already especially for this scope.
		if(cookies.containsKey(scope)) {
			final Hashtable cookiesForScope = (Hashtable) cookies.get(scope);
			
			// we have cookies already for this host, just replace what's there.
			if(cookiesForScope.containsKey(host)) {
				((Hashtable) cookiesForScope.get(host)).put(name, value);
			} else {
				Hashtable cookiesForHost = new Hashtable();
				cookiesForHost.put(name,  value);
				cookiesForScope.put(host, cookiesForHost);
			}
		} else { // we do not have cookies yet for this scope.
			final Hashtable cookiesForScope = new Hashtable();
			final Hashtable cookiesForHost = new Hashtable();
			cookies.put(scope, cookiesForScope);
			cookiesForScope.put(host, cookiesForHost);
			cookiesForHost.put(name, value);
		}
	}

	void onPut(Scope scope, String key, String value) {
		Hashtable dataNode = (Hashtable) tags.get(scope);
		dataNode.put(key, value);
	}
	
	void onNewDefaultScope(Scope scope) {
		initializeScope(scope, null);
	}

	void onNewScope(Scope parent, Scope scope) {
		initializeScope(scope, parent);
	}

	void onNewScope(Scope parent, Scope scope, String value) {
		initializeScope(scope, parent);
		onPut(scope, scope.getName(), value); // use onPut to avoid erroneous listener calls
	}

	void onPutInstruction(Scope scope, String source, String instruction, String uri) {
		((Vector) submitted.get(scope)).add(instruction);
	}
	
	void onPutSuccess(Scope scope, String source, String instruction, String uri) {
		((Vector) success.get(scope)).add(instruction);
	}

	void onPutMissing(Scope scope, String source, StuckExecution stuckExec) {
		((Vector) stuck.get(scope)).add(stuckExec);
	}

	void onPutFailed(Scope scope, String source, FailedExecution failedExec) {
		((Vector) failed.get(scope)).add(failedExec);
	}

	void onScopeComplete(Scope scope) {
		destroyScope(scope);
	}
	
	Scope[] getCompleteScopesAboveAndIncluding(Scope scope) {
		if(!isScopeComplete(scope, true)) {
			return NO_SCOPES;
		} else {
			Vector scopes = new Vector();
			scopes.add(scope); // we know this one already.
			
			Scope parent = (Scope) parentsByChild.get(scope);
			while(parent != null) {
				// we already checked these scopes' children,
				// so we don't have to do that again.
				if(isScopeComplete(parent, false)) {
					scopes.add(parent);
					// continue traversing up
					parent = (Scope) parentsByChild.get(parent);
				} else {
					// if there is an incomplete scope above, break
					// the chain.
					break;
				}
			}
			
			Scope[] scopesAry = new Scope[scopes.size()];
			scopes.copyInto(scopesAry);
			return scopesAry;
		}
	}
	
	/**
	 * Look through <code>scope</code> and all its children for {@link StuckExecution}s
	 * that are no longer stuck based off of new data.
	 * @param scope
	 * @param name A newly available <code>name</code>.
	 * @param value A newly available <code>value</code>.
	 * @throws DatabaseException
	 */
	StuckExecution[] getUnstuck(Scope scope, String name, String value) throws DatabaseException {		
		// vector of readyExecutions to send back.
		final Vector result = new Vector();
				
		// Browse through stuckExecutions, and add them if they're not still stuck.
		Vector stuckInScope = (Vector) stuck.get(scope);
		for(int i = 0 ; i < stuckInScope.size() ; i ++) {
			StuckExecution stuckExecution = (StuckExecution) stuckInScope.elementAt(i);
			
			// if it's no longer stuck, pull out its ready and add it to the result.
			if(stuckExecution.isReady(name)) {
				result.add(stuckExecution);
				stuckInScope.removeElementAt(i);
				i--;
			}
		}
		
		// Traverse through children.
		Vector children = (Vector) childrenByParent.get(scope);
		for(int i = 0 ; i < children.size() ; i ++) {
			VectorUtils.arrayIntoVector(
					// TODO recursive!! don't blow yer stack
					getUnstuck((Scope) children.elementAt(i), name, value),
					result);
		}
		
		// Send it to an array.
		final StuckExecution[] resultAry = new StuckExecution[result.size()];
		result.copyInto(resultAry);
		return resultAry;
	}
	
	/**
	 * Initialize <code>scope</code> in {@link MemoryDatabase}.
	 * @param scope The {@link Scope} to initialize.
	 * @param parent <code>scope</code>'s parent.  Can be <code>null</code>
	 * if it is a default and doesn't have one.
	 */
	private void initializeScope(Scope scope, Scope parent) {
		
		// establish parent/child relationship if there is one.
		if(parent != null) {
			parentsByChild.put(scope, parent);
			((Vector) childrenByParent.get(parent)).add(scope);
		}
		
		childrenByParent.put(scope, new Vector());
		
		// initialize hashtable for tags.
		tags.put(scope, new Hashtable());
		
		// initialize instruction vectors
		submitted.put(scope, new Vector());
		success.put(scope, new Vector());
		stuck.put(scope, new Vector());
		failed.put(scope, new Vector());
	}
	
	/**
	 * Remove all data from {@link MemoryDatabase} related to <code>scope</code>.
	 * @param scope
	 */
	private void destroyScope(Scope scope) {
		// remove everything.
		Scope parent = (Scope) parentsByChild.get(scope);
		if(parent != null) {
			((Vector) childrenByParent.get(parent)).remove(scope);
		}
		parentsByChild.remove(scope);
		
		tags.remove(scope);		
		submitted.remove(scope);
		success.remove(scope);
		stuck.remove(scope);
		failed.remove(scope);
		
		if(childrenByParent.containsKey(scope)) {
			Vector children = (Vector) childrenByParent.get(scope);
			for(int i = 0 ; i < children.size(); i ++) {
				destroyScope((Scope) children.elementAt(i));
			}
		}
	}
	
	private boolean isScopeComplete(Scope scope, boolean traverseDown) {
		if(((Vector) submitted.get(scope)).size()
				!=
				((Vector) success.get(scope)).size() +
				((Vector) stuck.get(scope)).size() +
				((Vector) failed.get(scope)).size()) {
			return false; // we've submitted more than have succeeded, gotten stuck, or failed.
		} else {
			// check children.
			if(traverseDown) {
				if(childrenByParent.containsKey(scope)) {
					Vector children = (Vector) childrenByParent.get(scope);
					for(int i = 0 ; i < children.size(); i ++) {
						if(!isScopeComplete((Scope) children.elementAt(i), true)) {
							return false; // exit prematurely to save checking additional children.
						}
					}
				}
			}
			return true;
		}
	}
}
