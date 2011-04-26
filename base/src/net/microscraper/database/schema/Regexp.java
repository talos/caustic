package net.microscraper.database.schema;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
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
	public RegexpExecution getExecution(Execution caller)
			throws ResourceNotFoundException {
		return new RegexpExecution(caller);
	}
	public final class RegexpExecution extends ResourceExecution {
		private Pattern pattern;
		private final Integer matchNumber;
		protected RegexpExecution(Execution caller) throws ResourceNotFoundException {
			super(caller);
			String matchNumberString = getAttributeValueRaw(MATCH_NUMBER);
			if(matchNumberString == null) {
				matchNumber = null;
			} else {
				matchNumber = new Integer(matchNumberString);
			}
		}
		
		protected boolean isOneToMany() {
			return isRegexpOneToMany();
		}
		
		protected Variables getLocalVariables() {
			return new Variables();
		}

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			try {
				pattern = Client.regexp.compile(getAttributeValue(REGEXP));
			} catch (TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
		
		public boolean matches(String input) {
			if(matchNumber != null) {
				try {
					pattern.match(input, matchNumber.intValue());
					return true;
				} catch(NoMatches e) {
					return false;
				}
			} else {
				return pattern.matches(input);
			}
		}
		
		public String[] allMatches(String input) throws NoMatches {
			if(matchNumber != null) {
				return new String[] { pattern.match(input, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input);
			}
		}
	}
}