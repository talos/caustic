package net.microscraper.database.schema;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Result;

public class AbstractHeader extends Resource {
	private String name;
	private String value;
	public AbstractHeader() { }
	public AbstractHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public static final AttributeDefinition NAME = new AttributeDefinition("name");
	public static final AttributeDefinition VALUE = new AttributeDefinition("value");

	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { NAME, VALUE }; }
			public RelationshipDefinition[] relationships() { return new RelationshipDefinition[] {}; }
		};
	}

	private class AbstractHeaderExecution extends ResourceExecution {
		public AbstractHeaderExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
		}
		
		public boolean isOneToMany() {
			return false;
		}

		protected String generateName() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}

		protected String generateValue() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			return null;
		}

		protected Variables getLocalVariables() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	protected ResourceExecution generateExecution(Execution caller)
			throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}