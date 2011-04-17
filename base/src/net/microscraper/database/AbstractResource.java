package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.Result;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;

public abstract class AbstractResource {
	private Reference ref;
	private Hashtable attributes;
	private Hashtable relationships;
	private Database db;
	
	/*
	 * Hashtable of result[] keyed off of calling_result.
	 */
	private Hashtable results = new Hashtable();
	
	public Reference ref() {
		return ref;
	}
	public boolean isVariable() {
		return false;
	}
	public boolean branchesResults() {
		return false;
	}
	public AbstractResource() {
		this.ref = Reference.blank(this);
	}
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
	protected abstract Result[] execute(AbstractResult caller)
			throws TemplateException, ResourceNotFoundException, InterruptedException ;
	
	public Result[] getValue(AbstractResult caller)
			throws ResourceNotFoundException, TemplateException, InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException("Interrupted " + ref.toString());
		
		Client.context().log.i("Result '" + caller.toString() + "' calling '" + ref.toString() + "'");		
		// Catch if this has already been called from this caller.
		if(this.results.containsKey(caller)) {
			return (Result[]) results.get(caller); // (Result) results.get(calling_result);
		} else {
			Result[] results = execute(caller);
			this.results.put(caller, results);
			return results;
		}
	}
}
