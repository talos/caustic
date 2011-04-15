package net.microscraper.database;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Variables;

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
		this.isOneToOne = !resource.branchesResults();
		
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
	
	// If this is one-to-one, intercept variables call and toss it up the caller chain.
	public Variables variables() {
		if(isOneToOne) {
			return caller.variables();
		} else {
			return super.variables();
		}
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
