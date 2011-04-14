package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.client.Variables;

public class Result {
	public final Result caller;
	private final Vector called = new Vector();
	public final Reference ref;
	protected final boolean isOneToOne;
	private final boolean isVariable;
	public final String key;
	public final String value;
	public final int id;
	private static int number = 0;
	public Result(Result caller, AbstractResource resource, String key, String value) {
		this.caller = caller;
		this.ref = resource.ref();
		if(this.caller != null) {
			this.isOneToOne = resource.branchesResults();
		} else {
			this.isOneToOne = false;
		}
		this.isVariable = resource.isVariable();
		this.key = key;
		this.value = value;
		this.caller.addCalled(this);
		this.id = number;
		number++;
	}
	public void addCalled(Result called) {
		this.called.addElement(called);
	}
	/**
	 * Put everything within this Result's scope that is marked as a variable into a Variables object.
	 * @return
	 */
	public Variables variables() {
		Result[] scope = scope();
		Variables variables = new Variables();
		for(int i = 0 ; i < scope.length ; i ++) {
			if(scope[i].isVariable) {
				variables.put(scope[i].key, scope[i].value);
			}
		}
		return variables;
	}
	/**
	 * Find all the other results in this result's scope.
	 * @return
	 */
	protected Result[] scope() {
		Vector scope = new Vector();
		// Check the scope above.
		if(this.caller != null) {
			Utils.arrayIntoVector(this.caller.scope(), scope);
		}
		// If this is one to one, it will be included in a parent's scope already -- don't include it!
		if(isOneToOne) {
			
		} else {
		// If this is not one to one, pull in the scope of all its one-to-one called results.
			scope.addElement(this);
			for(int i = 0; i < this.called.size() ; i++ ) {
				Result called = (Result) this.called.elementAt(i);
				if(called.isOneToOne) {
					Utils.arrayIntoVector(called.scope(), scope);
				}
			}
		}
		
		Result[] scope_ary = new Result[scope.size()];
		scope.copyInto(scope_ary);
		return scope_ary;
	}
	
	/**
	 * Allow this to be used in a hash.
	 */
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Result))
			return false;
		return this.id == ((Result) obj).id;
	}
	public int hashCode() {
		return this.id;
	}
}
