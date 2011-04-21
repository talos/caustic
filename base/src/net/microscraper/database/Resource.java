package net.microscraper.database;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Attribute.Attributes;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.Relationships;

public abstract class Resource {
	protected Reference ref = Reference.blank(this);
	protected Attributes attributes;
	protected Relationships relationships;
	private Hashtable executionsByCaller = new Hashtable();
	
	public Resource initialize(String key, Attributes attributes, Relationships relationships) {
		this.ref = new Reference(Model.get(getClass()), key);
		this.attributes = attributes;
		this.relationships = relationships;
		return this;
	}
	
	protected String getAttribute(AttributeDefinition def) {
		return attributes.get(def);
	}
	
	public abstract ModelDefinition definition();
	
	
	public final ResourceExecution getExecution(Execution caller) throws FatalExecutionException {
		if(executionsByCaller.containsKey(caller))
			return (ResourceExecution) executionsByCaller.get(caller);
		ResourceExecution exc = generateExecution(caller);
		executionsByCaller.put(caller, exc);
		return exc;
	}
	protected abstract ResourceExecution generateExecution(Execution caller) throws FatalExecutionException;
	
	protected abstract class ResourceExecution extends Execution {
		protected ResourceExecution(Execution caller) {
			//this.caller = caller;
			if(isOneToMany()) {
				this.source = this;
			} else {
				this.source = caller;
			}
		}
		
		public abstract Result getResult() throws MissingVariable, FatalExecutionException;
		
		protected abstract Status execute() throws FatalExecutionException;
		
		protected String getAttribute(AttributeDefinition def) throws TemplateException, MissingVariable {
			return attributes.get(def, getVariables());
		}
		
		public abstract boolean isOneToMany();
		
		private Variables getVariables() {
			
		}
	}
}
