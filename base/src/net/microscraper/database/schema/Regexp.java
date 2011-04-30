package net.microscraper.database.schema;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;

public class Regexp extends OneToOneResource {
	private static final AttributeDefinition REGEXP = new AttributeDefinition("regexp");
	private static final AttributeDefinition MATCH_NUMBER = new AttributeDefinition("match_number");
		
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

	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new RegexpExecution(this, caller);
	}
	
	public static final class RegexpExecution extends Execution {
		private Pattern pattern;
		private final Integer matchNumber;
		protected RegexpExecution(Resource resource, Execution caller) {
			super(resource, caller);
			matchNumber = resource.getIntegerAttribute(MATCH_NUMBER);
		}
		
		protected boolean isOneToMany() {
			if(matchNumber.equals(null))
				return true;
			return false;
		}
		
		protected Variables getLocalVariables() {
			return new Variables();
		}
		
		protected String privateExecute() throws TemplateException, MissingVariable {
			String patternString = getAttributeValue(REGEXP);
			pattern = Client.regexp.compile(patternString);
			return patternString;
		}
		
		public boolean matches(String input) {
			if(!matchNumber.equals(null)) {
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
			if(!matchNumber.equals(null)) {
				return new String[] { pattern.match(input, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input);
			}
		}
	}

}