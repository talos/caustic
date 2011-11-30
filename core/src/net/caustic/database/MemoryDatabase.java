package net.caustic.database;

import java.util.Enumeration;
import java.util.Hashtable;

import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StringUtils;

/**
 * A {@link Database} implementation that creates a new {@link InMemoryDatabaseView}
 * for each call of {@link #newDefaultScope()}.
 * @author talos
 *
 */
public final class MemoryDatabase extends Database {
	
	private static final ReadyExecution[] EMPTY_INSTRUCTION_ARRAY = new ReadyExecution[0];
	
	/**
	 * Tree of scope parent relationships.  {@link Scope} => {@link Scope}.
	 */
	private final Hashtable tree = new Hashtable();
	
	/**
	 * Hashtables of substitutions.<br>
	 * { Scope => { String (name) => String (value) }
	 */
	private final Hashtable substitutions = new Hashtable();
	
	/**
	 * Hashtables of cookies.
	 * { Scope => { String (host) => { String (name) => String (value) } } }
	 */
	private final Hashtable cookies = new Hashtable();
	
	/**
	 * Hashtables of stopped instructions.
	 * { Scope => { String (template name) => StoppedInstruction } }
	 */
	private final Hashtable stopped = new Hashtable();

	/**
	 * Look in a data node for a value.  If it's not there, traverse
	 * up the tree.
	 */
	public String get(Scope scope, String key) {
		while(scope != null) {
			Hashtable dataNode = (Hashtable) substitutions.get(scope);
			String value = (String) dataNode.get(key);
			if(value != null) {
				return value;
			} else {
				scope = (Scope) tree.get(scope); // could be null, would break loop.
			}
		}
		return null;
	}

	public ReadyExecution[] getStoppedInstructions(Scope scope)
			throws DatabaseException {
		final ReadyExecution[] results;
		if(stopped.containsKey(scope)) {
			final Hashtable frozenForScope = (Hashtable) stopped.get(scope);
			results = new ReadyExecution[frozenForScope.size()];
			final Enumeration e = frozenForScope.elements();
			
			// convert to an array.
			int i = 0;
			while(e.hasMoreElements()) {
				results[i] = (ReadyExecution) e.nextElement();
				i++;
			}
		} else {
			results = EMPTY_INSTRUCTION_ARRAY;
		}
		return results;
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
			scope = (Scope) tree.get(scope); // get the parent scope
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

	protected void onAddCookie(Scope scope, String host, String name,
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

	protected void onStop(Scope scope, ReadyExecution stoppedInstruction) {
		final Hashtable stoppedForScope;
		final Instruction instruction = stoppedInstruction.instruction;
		
		// create new hashtable for frozenForScope if no entry for this scope yet.
		if(stopped.containsKey(scope)) {
			stoppedForScope = (Hashtable) stopped.get(scope);
		} else {
			stoppedForScope = new Hashtable();
			stopped.put(scope, stoppedForScope);
		}
		
		stoppedForScope.put(instruction.getName().toString(), stoppedInstruction);
	}
	
	protected void onRestart(Scope scope, ReadyExecution stoppedInstruction) 
		throws DatabaseException {
		final Instruction instruction = stoppedInstruction.instruction;
		try {
			// remove the restarted instruction from the table of stopped instructions.
			final Hashtable stoppedForScope = (Hashtable) stopped.get(scope);
				
			// pull out hashtable stopped for this scope
			stoppedForScope.remove(instruction.getName().toString());
			
			// if none left, pull out the entire hashtable.
			if(stoppedForScope.size() == 0) {
				stopped.remove(scope);
			}
		} catch(NullPointerException e) {
			throw new DatabaseException("Could not remove stopped instruction " + StringUtils.quote(instruction)
					+ " from database because it was not in database scope " + StringUtils.quote(scope));
		}
	}

	protected void onPut(Scope scope, String key, String value) {
		Hashtable dataNode = (Hashtable) substitutions.get(scope);
		dataNode.put(key, value);
	}
	
	protected void onNewDefaultScope(Scope scope) {
		substitutions.put(scope, new Hashtable());
	}

	protected void onNewScope(Scope parent, Scope scope) {
		tree.put(scope, parent);
		substitutions.put(scope, new Hashtable());
	}

	protected void onNewScope(Scope parent, Scope scope, String value) {
		tree.put(scope, parent); // search for parent by child.
		Hashtable dataNode = new Hashtable();
		dataNode.put(scope.getName(), value);
		substitutions.put(scope, dataNode);		
	}
}
