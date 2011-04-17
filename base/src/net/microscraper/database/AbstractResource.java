package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Result;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public abstract class AbstractResource {
	protected Reference ref = Reference.blank(this);
	private Hashtable attributes;
	private Hashtable relationships;
	private Database db;
	
	public Reference ref() {
		return ref;
	}
	
	public boolean isVariable() {
		return false;
	}
	protected abstract boolean branchesResults() throws FatalExecutionException;
	
	public AbstractResource initialize(Database db, String key, Hashtable attributes, Hashtable relationships) {
		this.db = db;
		this.ref = new Reference(Model.get(getClass()), key);
		this.attributes = attributes;
		this.relationships = relationships;
		return this;
	}
	
	protected String attribute_get(String name) {
		return (String) attributes.get(name);
	}
	
	/**
	 * Retrieve all the resources related through a specific Relationship.
	 * @param relationship
	 * @return
	 * @throws ModelNotFoundException 
	 * @throws ResourceNotFoundException
	 */
	protected AbstractResource[] relationship(RelationshipDefinition relationship)
				throws ResourceNotFoundException {
		Reference[] references = (Reference[]) relationships.get(relationship.key);
		AbstractResource[] resources = new AbstractResource[references.length];
		for(int i = 0; i < references.length ; i ++) {
			resources[i] = db.get(references[i]);
		}
		return resources;
	}
	
	public abstract ModelDefinition definition();
	public abstract Result[] getResults(AbstractResult caller) throws FatalExecutionException;
	
	public static abstract class Simple extends AbstractResource {
		/*
		 * Hashtable of result[] keyed off of calling_result.
		 */
		private Hashtable successes = new Hashtable();

		protected boolean branchesResults() {
			return false;
		}
		
		/**
		 * Return a result for a caller.
		 * @param caller
		 * @return
		 * @throws TemplateException
		 * @throws ResourceNotFoundException
		 * @throws InterruptedException
		 * @throws MissingVariable
		 * @throws ResourceFailedException
		 * @throws NoMatches 
		 * @throws FatalExecutionException 
		 */
		protected abstract String getName(AbstractResult caller)
			throws TemplateException, ResourceNotFoundException, InterruptedException, MissingVariable, NoMatches, FatalExecutionException;
		protected abstract String getValue(AbstractResult caller)
			throws TemplateException, ResourceNotFoundException, InterruptedException, MissingVariable, NoMatches, FatalExecutionException;
		
		public Result.Success getSuccess(AbstractResult caller)
			throws TemplateException, ResourceNotFoundException, InterruptedException, MissingVariable, NoMatches, FatalExecutionException {
			Client.context().log.i("Result '" + caller.toString() + "' calling '" + ref.toString() + "'");
			
			Result.Success result;
			if(successes.containsKey(caller)) { // If we have already executed for this caller, check our results.
				result = (Result.Success) successes.get(caller);
			}
			result = new Result.Success(caller, this, getName(caller), getValue(caller));
			successes.put(caller, result);
			return result;
		}
		
		// Elevate certain exceptions, and generate Premature and Failure results otherwise.
		public Result getResult(AbstractResult caller) throws FatalExecutionException {
			try {
				if(Thread.interrupted())
					throw new InterruptedException("Interrupted " + ref.toString());
				return getSuccess(caller);
			} catch(MissingVariable e) {
				return new Result.Premature(caller, this, e);
			} catch (NoMatches e) {
				return new Result.Failure(caller, this, e);
			} catch (ResourceNotFoundException e) {
				throw new FatalExecutionException(e);
			} catch (TemplateException e) {
				throw new FatalExecutionException(e);
			} catch (InterruptedException e) {
				throw new FatalExecutionException(e);
			} 
		}
		
		public Result[] getResults(AbstractResult caller) throws FatalExecutionException {
			return new Result[] { getResult(caller) };
		}
	}
	/*
	public static abstract class Complex extends AbstractResource {
		protected abstract AbstractResource hasMoreSources(AbstractResult caller);
		
		public Result[] getValues(AbstractResult caller) throws FatalExecutionException {
			Client.context().log.i("Result '" + caller.toString() + "' calling '" + ref.toString() + "'");
			
			Result[] results;
			if(results_hsh.containsKey(caller)) { // If we have already executed for this caller, check our results.
				results = (Result[]) results_hsh.get(caller);
				for(int i = 0 ; i < results.length ; i ++) {
					if(results[i].premature) {
						results[i] = ((Result.Premature) results[i]).retry();
					}
				}
			} else { // Otherwise, execute for the first time.
				Vector executions = new Vector();
				AbstractResource source;
				while((source = hasMoreSources(caller)) != null) {
					executions.addElement(tryExecution(caller, source));
				}
				results = new Result[executions.size()];
				executions.copyInto(results);
			}
			return results;
		}
	}
	*/
	public static class FatalExecutionException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6849313338870437772L;

		public FatalExecutionException(Throwable e) {
			super(e);
		}
	}
}
