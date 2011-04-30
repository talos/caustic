package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Mustache;
import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Attribute.Attributes;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Relationship.Relationships;
import net.microscraper.database.schema.Data.DataExecution;

public abstract class Resource {
	protected Reference ref = Reference.blank(this);
	
	private Attributes attributes = new Attributes();
	private Relationships relationships = new Relationships();
	//private Hashtable executionsByCaller = new Hashtable();
	
	public Resource initialize(String key, Attributes attributes, Relationships relationships) {
		this.ref = new Reference(Model.get(getClass()), key);
		this.attributes = attributes;
		this.relationships = relationships;
		return this;
	}
	public Reference ref() {
		return ref;
	}
	public String getStringAttribute(AttributeDefinition def) {
		return attributes.getString(def);
	}
	public Integer getIntegerAttribute(AttributeDefinition def) {
		return attributes.getInteger(def);
	}
	protected Resource[] getRelatedResources(RelationshipDefinition def) throws ResourceNotFoundException {
		return relationships.get(def);
	}
	protected int getNumberOfRelatedResources(RelationshipDefinition def) {
		return relationships.getSize(def);
	}
	
	public abstract ModelDefinition definition();
	
	public abstract static class OneToOneResource extends Resource {
		private final HashtableWithNulls executions = new HashtableWithNulls();
		public ResourceExecution executionFromExecution(Execution caller) throws ExecutionFatality {
			ResourceExecution exc = (ResourceExecution) executions.get(caller);
			if(exc == null) {
				exc = generateExecution(caller);
				executions.put(caller, exc);
			}
			return exc;
		}
		public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
			Execution exc = executionFromExecution(null);
			exc.addVariables(extraVariables);
			return exc;
		}
		protected abstract ResourceExecution generateExecution(Execution caller) throws ExecutionFatality;
		public final boolean isOneToMany() {
			return false;
		}
	}
	
	public abstract static class OneToManyResource extends Resource {
		public abstract Execution[] executionsFromVariables(Variables extraVariables) throws ExecutionFatality;
		public abstract Execution[] executionsFromExecution(Execution caller) throws ExecutionFatality;
	}
}
