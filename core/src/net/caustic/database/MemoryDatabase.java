package net.caustic.database;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.instruction.Instruction;
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
	private final Hashtable ready = new Hashtable();

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

	public void onAddCookie(Scope scope, String host, String name,
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

	public void onPut(Scope scope, String key, String value) {
		Hashtable dataNode = (Hashtable) tags.get(scope);
		dataNode.put(key, value);
	}
	
	public void onNewDefaultScope(Scope scope) {
		initializeScope(scope, null);
	}

	public void onNewScope(Scope parent, Scope scope) {
		initializeScope(scope, parent);
	}

	public void onNewScope(Scope parent, Scope scope, String value) {
		initializeScope(scope, parent);
		onPut(scope, scope.getName(), value); // use onPut to avoid erroneous listener calls
	}

	public void onPutReady(Scope scope, String source, Instruction instruction) {
		((Vector) ready.get(scope)).add(new ReadyExecution(source, instruction));
	}

	public void onPutMissing(Scope scope, String source,
			Instruction instruction, String[] missingTags) {
		((Vector) stuck.get(scope)).add(new StuckExecution(source, instruction, missingTags));
	}

	public void onPutFailed(Scope scope, String source,
			Instruction instruction, String failedBecause) {
		((Vector) failed.get(scope)).add(new FailedExecution(source, instruction, failedBecause));		
	}

	/**
	 * Look through <code>scope</code> and all its children for {@link StuckExecution}s
	 * that are no longer stuck based off of new data.
	 * @param scope
	 * @param name A newly available <code>name</code>.
	 * @param value A newly available <code>value</code>.
	 * @throws DatabaseException
	 */
	protected ReadyExecution[] resort(Scope scope, String name, String value) throws DatabaseException {
		// vector of readyExecutions to send back.
		final Vector result = new Vector();
		
		// Copy all readyExecutions in directly.
		VectorUtils.vectorIntoVector((Vector) ready.get(scope), result);
		
		// Browse through stuckExecutions, and add them if they're not still stuck.
		Vector stuckInScope = (Vector) stuck.get(scope);
		for(int i = 0 ; i < stuckInScope.size() ; i ++) {
			StuckExecution stuckExecution = (StuckExecution) stuckInScope.elementAt(i);
			
			// if it's no longer stuck, pull it out of the vector and add it into result.
			if(stuckExecution.found(name)) {
				stuckInScope.removeElementAt(i); // pull out
				result.add(stuckExecution); // add to result
				i--;
			}
		}
		

		// Traverse through children.
		Vector children = (Vector) childrenByParent.get(scope);
		for(int i = 0 ; i < children.size() ; i ++) {
			VectorUtils.arrayIntoVector(
					// TODO recursive!! don't blow yer stack
					resort((Scope) children.elementAt(i), name, value), result);
		}
		
		// Send it to an array.
		final ReadyExecution[] resultAry = new ReadyExecution[result.size()];
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
		ready.put(scope, new Vector());
		stuck.put(scope, new Vector());
		failed.put(scope, new Vector());
	}	
}
