package net.caustic.database;

import java.util.Vector;

import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
import net.caustic.scope.IntScopeFactory;
import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;
import net.caustic.util.Encoder;

/**
 * @author talos
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
	 * Add a key-value mapping for {@link Scope}.  This will call {@link #resort}, and thus
	 * hit {@link #putReady(Scope, String, Instruction)} for any {@link Instruction}s that were
	 * previously stuck.
	 */
	public final void put(Scope scope, String key, String value) throws DatabaseException {
		onPut(scope, key, value);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPut(scope, key, value);
		}
		
		// resort and re-submit instructions that can now be used.
		StuckExecution[] newlyReady = getUnstuck(scope, key, value);
		for(int i = 0 ; i < newlyReady.length ; i ++) {
			newlyReady[i].retry(this, scope);
		//	putInstruction(scope, newlyReady[i].source, newlyReady[i].instruction);
		}
	}
	
	public final void putLoad(Scope scope, String source, Load load)
			throws DatabaseException {		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutLoad(scope, source, load);
		}
	}
	
	public final void putFind(Scope scope, String source, Find find)
			throws DatabaseException {		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutFind(scope, source, find);
		}
	}
	
	public final void putInstruction(Scope scope, String source, String instruction, String uri) {
		onPutInstruction(scope, source, instruction, uri);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutInstruction(scope, source, instruction, uri);
		}
	}
	
	public final void putSuccess(Scope scope, String source,
			String instruction, String uri) throws DatabaseException {
		onPutSuccess(scope, source, instruction, uri);
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutSuccess(scope, source,
					instruction, uri);
		}

		checkScopeComplete(scope);
	}
	
	public final void putMissing(Scope scope, String source, String instruction,
			String uri, String[] missingTags) throws DatabaseException {
		onPutMissing(scope, source, new StuckExecution(source, instruction, uri, missingTags));
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutMissing(scope, source,
					instruction, uri, missingTags);
		}
		checkScopeComplete(scope);
	}

	public final void putMissing(Scope scope, String source, Load load,
			String[] missingTags) throws DatabaseException {
		onPutMissing(scope, source, new StuckExecution(source, load));
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutMissing(scope, source,
					load.serialized, load.uri, missingTags);
		}
		checkScopeComplete(scope);
	}
	

	public final void putMissing(Scope scope, String source, Find find,
			String[] missingTags) throws DatabaseException {
		onPutMissing(scope, source, new StuckExecution(source, find));
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutMissing(scope, source,
					find.serialized, find.uri, missingTags);
		}
		checkScopeComplete(scope);
	}
	

	public final void putFailed(Scope scope, String source, String instruction,
			String uri, String failedBecause) throws DatabaseException {
		onPutFailed(scope, source, new FailedExecution(source, instruction, uri, failedBecause));
		
		for(int i = 0 ; i < listeners.size() ; i ++) {
			((DatabaseListener) listeners.elementAt(i)).onPutFailed(scope, source,
					instruction, uri, failedBecause);
		}
		
		checkScopeComplete(scope);
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
	

	/**
	 * Check <code>scope</code> and its children for executions that were previously
	 * stuck, but are no longer stuck due to new data.
	 * @param scope
	 * @param name
	 * @param value
	 * @return
	 */
	abstract StuckExecution[] getUnstuck(Scope scope, String name, String value)
			throws DatabaseException;
	
	/**
	 * Check to see whether the specified <code>scope</scope> has any more
	 * {@link Instruction}s that could be executed.  Should check all children of <code>scope</code>,
	 * and they should all be complete as well.
	 * @param scope The {@link Scope} to check.
	 * @return <code>True</code> if the scope is complete, <code>false</code>
	 * otherwise.  This could change back to <code>false</code> if an instruction was revived.
	 */
	//abstract boolean isScopeComplete(Scope scope) throws DatabaseException;
	
	/**
	 * 
	 * @param scope
	 * @return An array of {@Link Scope} parents of <code>scope</code> that are now complete,
	 * including <code>scope</code>.  Returns an empty array if <code>scope</code> is not complete.
	 * @throws DatabaseException
	 */
	abstract Scope[] getCompleteScopesAboveAndIncluding(Scope scope) throws DatabaseException;
	
	abstract void onPut(Scope scope, String key, String value);
	abstract void onPutInstruction(Scope scope, String source, String instruction, String uri);
	abstract void onAddCookie(Scope scope, String host, String name, String value);
	abstract void onNewScope(Scope parent, Scope scope, String value);
	abstract void onNewScope(Scope parent, Scope scope);
	abstract void onNewDefaultScope(Scope scope);
	abstract void onPutFailed(Scope scope, String source, FailedExecution failed);
	abstract void onPutMissing(Scope scope, String source, StuckExecution stuck);
	abstract void onPutSuccess(Scope scope, String source,
			String instruction, String uri);
	abstract void onScopeComplete(Scope scope);

	/**
	 * Uses {@link #isScopeComplete(Scope)} to check whether the passed <code>scope</code>
	 * is complete.  Data in <code>scope</code> can be cleared out if this is true.
	 * @param scope
	 * @throws DatabaseException
	 */
	private void checkScopeComplete(Scope scope) throws DatabaseException {
		Scope[] completeScopes = getCompleteScopesAboveAndIncluding(scope);
		for(int i = 0 ; i < completeScopes.length ; i ++) {
			onScopeComplete(completeScopes[i]);
			for(int j = 0 ; j < listeners.size() ; j ++) {
				((DatabaseListener) listeners.elementAt(j)).onScopeComplete(completeScopes[i]);
			}
		}
	}
}