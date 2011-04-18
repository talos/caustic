package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
import net.microscraper.database.Result.Success;

public abstract class AbstractResult {
	protected final Vector called = new Vector();
	public final int id;
	private static int number = 0;
	
	public AbstractResult() {
		this.id = number;
		number++;
	}
	
	protected void addCalled(Success result) {
		this.called.addElement(result);
	}
	
	/**
	 * Put everything within this Result's scope that is marked as a variable into a Variables object.
	 * @return
	 */
	public Variables variables() {
		Success[] scope = scope();
		Variables variables = new Variables();
		for(int i = 0 ; i < scope.length ; i ++) {
			if(scope[i].isVariable) {
				variables.put(scope[i].key, scope[i].value);
			}
		}
		return variables;
	}
	/**
	 * Find all the other successful results in this result's scope.
	 * Will return all one-to-one called results.
	 * @return
	 */
	protected Success[] scope() {
		Vector scope = new Vector();
		for(int i = 0; i < this.called.size() ; i++ ) {
			Success called = (Success) this.called.elementAt(i);
			if(called.isOneToOne && called.successful) {
				scope.addElement((Success) called);
				Utils.arrayIntoVector(called.scope(), scope);
			}
		}
		Success[] scope_ary = new Success[scope.size()];
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
