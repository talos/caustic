package net.microscraper.database;

import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Publisher;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Variables;
import net.microscraper.database.AbstractResource.FatalExecutionException;

public abstract class Result extends AbstractResult {
	public static final String ERROR = "error";
	public static final String MISSING_VARIABLE = "missing";
	public static final String VALUE = "value";
	public static final String KEY = "key";
	
	public final AbstractResult caller;
	public final AbstractResource resource;
	public final Reference ref;
	public final boolean successful;
	public final boolean premature;
	public final boolean failure;
	protected final Publisher publisher = Client.context().publisher;
	protected final Variables messages = new Variables();
	
	protected Result(AbstractResult caller, AbstractResource resource, boolean successful, boolean premature, boolean failure) throws FatalExecutionException {
		if(caller == null || resource == null)
			throw new IllegalArgumentException();
		this.caller = caller;
		this.resource = resource;
		this.ref = resource.ref();
		
		this.successful = successful;
		this.premature = premature;
		this.failure = failure;
	}
	
	public static class Success extends Result {
		protected final boolean isVariable;
		protected final boolean isOneToOne;

		public final String key;
		public final String value;
		public Success(AbstractResult caller, AbstractResource resource, String key, String value) throws FatalExecutionException {
			super(caller, resource, true, false, false);
			if(key == null || value == null)
				throw new IllegalArgumentException();
			this.key = key;
			this.value = value;
			messages.put(KEY, this.key);
			messages.put(VALUE, this.value);
			this.isOneToOne = !resource.branchesResults();

			this.isVariable = resource.isVariable();
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
	}
	
	public static class Premature extends Result {
		public final MissingVariable error;
		public Premature(AbstractResult caller, AbstractResource resource, MissingVariable error) throws FatalExecutionException {
			super(caller, resource, false, true, false);
			this.error = error;
			messages.put(MISSING_VARIABLE, error.missing_tag);
		}
	}
	
	public static class Failure extends Result {
		public final Throwable error;
		public Failure(AbstractResult caller, AbstractResource resource, Throwable error) throws FatalExecutionException {
			super(caller, resource, false, false, true);
			this.error = error;
			messages.put(ERROR, error.getMessage());
		}
	}
}
