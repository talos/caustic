package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.client.Variables;

public abstract class AbstractResult {
	protected final Vector called = new Vector();
	public final int id;
	private static int number = 0;
	
	public AbstractResult() {
		this.id = number;
		number++;
	}
	
	protected void addCalled(Result result) {
		this.called.addElement(result);
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
		for(int i = 0; i < this.called.size() ; i++ ) {
			Result called = (Result) this.called.elementAt(i);
			if(called.isOneToOne) {
				Utils.arrayIntoVector(called.scope(), scope);
			}
		}
		Result[] scope_ary = new Result[scope.size()];
		scope.copyInto(scope_ary);
		return scope_ary;
	}
}
