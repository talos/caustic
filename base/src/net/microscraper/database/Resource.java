package net.microscraper.database;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.database.Attribute.Attributes;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.ResourceExecution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.Relationships;

public abstract class Resource {
	protected Reference ref = Reference.blank(this);
	protected Attributes attributes;
	protected Relationships relationships;
	
	public Reference ref() {
		return ref;
	}
	
	public boolean isVariable() {
		return false;
	}
	
	public Resource initialize(String key, Attributes attributes, Relationships relationships) {
		this.ref = new Reference(Model.get(getClass()), key);
		this.attributes = attributes;
		this.relationships = relationships;
		return this;
	}
	
	public abstract ModelDefinition definition();
	public abstract ResourceExecution getExecution(Execution caller) throws MissingVariable, FatalExecutionException;
}
