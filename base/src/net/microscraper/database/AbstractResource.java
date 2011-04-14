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
	private Hashtable results;
	
	public Reference ref() {
		return ref;
	}
	public boolean isVariable() {
		return false;
	}
	public boolean branchesResults() {
		return false;
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
	protected abstract Result[] execute(Result calling_result)
			throws TemplateException, MissingVariable, ResourceNotFoundException, InterruptedException, BrowserException;
	
	public Result[] getValue(Result calling_result)
			throws ResourceNotFoundException, TemplateException, MissingVariable, InterruptedException, BrowserException {
		Client.context().log.i("Result '" + calling_result.toString() + "' calling '" + ref().toString() + "'");
		
		// Catch if this has already been from that calling_result.
		if(this.results.containsKey(calling_result)) {
			return (Result[]) results.get(calling_result); // (Result) results.get(calling_result);
		} else {
			Result[] results = execute(calling_result);
			this.results.put(calling_result, results);
			return results;
		}
	}
}
