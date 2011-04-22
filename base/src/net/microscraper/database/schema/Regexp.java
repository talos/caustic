package net.microscraper.database.schema;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Regexp extends Resource {
	private static final AttributeDefinition REGEXP = new AttributeDefinition("regexp");
	private static final AttributeDefinition MATCH_NUMBER = new AttributeDefinition("match_number");

	//public Regexp() { }
	private final boolean isRegexpOneToMany() {
		if(getAttributeValueRaw(MATCH_NUMBER) == null)
			return true;
		return false;
	}
	
	public ModelDefinition definition() {
		return new ModelDefinition() {	
			public AttributeDefinition[] attributes() {
				return new AttributeDefinition[] { REGEXP, MATCH_NUMBER };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] { };
			}
		};
	}
	protected ResourceExecution[] generateExecutions(Execution caller)
			throws ResourceNotFoundException, MissingVariable {
		
	}
	private final class RegexpExecution extends ResourceExecution {
		protected RegexpExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
			
		}
		
		protected boolean isOneToMany() {
			return isRegexpOneToMany();
		}
		
		protected Variables getLocalVariables() {
			return new Variables();
		}

		protected String generateName() throws MissingVariable,
				BrowserException, FatalExecutionException, NoMatches {
			return ref().toString();
		}
		
		protected String generateValue() throws FatalExecutionException, MissingVariable {
			try {
				return getAttributeValue(REGEXP);
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
	}
}