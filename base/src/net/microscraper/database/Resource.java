package net.microscraper.database;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Mustache;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Attribute.Attributes;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Relationship.Relationships;

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
	
	protected String getAttributeValueRaw(AttributeDefinition def) {
		return attributes.get(def);
	}
	protected Resource[] getRelatedResources(RelationshipDefinition def) throws ResourceNotFoundException {
		return relationships.get(def);
	}
	protected int getNumberOfRelatedResources(RelationshipDefinition def) {
		return relationships.getSize(def);
	}
	
	public abstract ModelDefinition definition();
	public abstract void execute(Execution caller) throws ResourceNotFoundException;
	//protected abstract ResourceExecution getExecution(Execution caller) throws ResourceNotFoundException;
	
	protected abstract class ResourceExecution extends Execution {
		protected ResourceExecution(Execution caller) {
			super(caller);
		}

		protected final String getAttributeValue(AttributeDefinition def)
					throws TemplateException, MissingVariable {
			return (String) Mustache.compile(getAttributeValueRaw(def), getVariables());
		}
	}
}
