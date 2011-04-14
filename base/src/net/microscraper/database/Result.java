package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.client.Publisher.PublisherException;

public class Result extends AbstractResult {
	public final AbstractResult caller;
	public final Reference ref;
	protected final boolean isOneToOne;
	protected final boolean isVariable;
	public final String key;
	public final String value;
	private final Publisher publisher = Client.context().publisher;
	
	public Result(AbstractResult caller, AbstractResource resource, String key, String value) {
		if(caller == null || resource == null || key == null || value == null)
			throw new IllegalArgumentException();
		this.caller = caller;
		this.ref = resource.ref();
		this.isOneToOne = resource.branchesResults();
		
		this.isVariable = resource.isVariable();
		this.key = key;
		this.value = value;
		this.caller.addCalled(this);
		
		if(publisher.live()) {
			try {
				publisher.publish(this);
			} catch (PublisherException e) {
				Client.context().log.e(e);
			}
		}
	}
	
	protected Result[] scope() {
		Vector scope = new Vector();
		//Vector scope = new Vector();
		// Check the scope above.
		if(this.caller != null) {
			Utils.arrayIntoVector(this.caller.scope(), scope);
		}
		// If this is one to one, it will be included in a parent's scope already -- don't include it!
		if(isOneToOne) {
			
		} else {
		// If this is not one to one, pull in the scope of all its one-to-one called results.
			scope.addElement(this);
			Utils.arrayIntoVector(super.scope(), scope);
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
