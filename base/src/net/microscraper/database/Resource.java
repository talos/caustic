package net.microscraper.database;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Mustache;
import net.microscraper.client.Utils;
import net.microscraper.client.Variables;
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
	private Hashtable executionsByCaller = new Hashtable();
	
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
	
	protected Execution[] callRelatedResources(Execution caller, RelationshipDefinition def) throws ResourceNotFoundException {
		Resource[] relatedResources = getRelatedResources(def);
		Vector executions = new Vector();
		for(int i = 0 ; i < relatedResources.length ; i ++) {
			Utils.arrayIntoVector(caller.call(relatedResources[i]), executions);
		}
		Execution[] executionsAry = new Execution[executions.size()];
		executions.copyInto(executionsAry);
		return executionsAry;
	}
	public abstract ModelDefinition definition();
	
	public final Execution[] callFrom(Execution caller) throws ResourceNotFoundException {
		if(!executionsByCaller.containsKey(caller)) {
			return (Execution[]) executionsByCaller.get(caller);
		} else {
			Execution[] executions = generateExecutions(caller);
			executionsByCaller.put(caller, executions);
			return executions;
		}
	}
	protected abstract ResourceExecution[] generateExecutions(Execution caller)
			throws ResourceNotFoundException,
			MissingVariable; // this happens when a missingvariable prevents us from knowing how many executions would be spawned
	
	protected abstract class ResourceExecution extends Execution {
		private final Execution source;
		private String name;
		private String value;
		protected ResourceExecution(Execution caller) throws ResourceNotFoundException {
			if(isOneToMany()) {
				this.source = this;
			} else {
				this.source = caller;
			}
			/*
			RelationshipDefinition[] defs = definition().relationships();
			for(int i = 0 ; i < defs.length ; i ++ ) {
				Resource resources[] = getResourcesToCall(defs[i]);
				for(int j = 0 ; j < resources.length ; j ++) {
					call(resources[j]); // Resource caches these, we won't get duplicates.
				}
			}
			*/
		}
		
		protected final Execution getSourceExecution() {
			return source;
		}

		protected final String getName() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			if(name != null)
				return name;
			return generateName();
		}
		
		protected final String getValue() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			if(value != null)
				return value;
			return generateValue();
		}
		
		protected final String getAttributeValue(AttributeDefinition def)
					throws TemplateException, MissingVariable {
			return (String) Mustache.compile(getAttributeValueRaw(def), getVariables());
		}
		
		protected abstract String generateName()
			throws MissingVariable, BrowserException, FatalExecutionException, NoMatches;
		protected abstract String generateValue()
			throws MissingVariable, BrowserException, FatalExecutionException, NoMatches;
		protected abstract boolean isOneToMany();
		protected abstract Variables getLocalVariables();
	}
}
