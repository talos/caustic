package net.microscraper.database.schema;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Resource.OneToOneResource;

public class Regexp extends OneToOneResource {
	private static final AttributeDefinition REGEXP = new AttributeDefinition("regexp");
	private static final AttributeDefinition SUBSTITUTION = new AttributeDefinition("substitution");
	private static final AttributeDefinition MATCH_NUMBER = new AttributeDefinition("match_number");
	private static final AttributeDefinition CASE_INSENSITIVE = new AttributeDefinition("case_insensitive");
	private static final AttributeDefinition MULTILINE = new AttributeDefinition("multiline");
	private static final AttributeDefinition DOT_MATCHES_NEWLINE = new AttributeDefinition("dot_matches_newline");
	
	public ModelDefinition definition() {
		return new ModelDefinition() {	
			public AttributeDefinition[] attributes() {
				return new AttributeDefinition[] {
						REGEXP, MATCH_NUMBER, SUBSTITUTION,
						CASE_INSENSITIVE, MULTILINE, DOT_MATCHES_NEWLINE };
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
		//private Pattern pattern;
		private final Integer matchNumber;
		protected RegexpExecution(Resource resource, Execution caller) {
			super(resource, caller);
			matchNumber = resource.getIntegerAttribute(MATCH_NUMBER);
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFatality {
			String patternString = getAttributeValue(REGEXP);
			//pattern = Client.regexp.compile(patternString);
			return patternString;
		}
		public Pattern getPattern() throws ExecutionDelay, ExecutionFatality {
			return Client.regexp.compile(
					getAttributeValue(REGEXP),
					getBooleanAttribute(CASE_INSENSITIVE),
					getBooleanAttribute(MULTILINE),
					getBooleanAttribute(DOT_MATCHES_NEWLINE));
		}
		private String getSubstitution() throws ExecutionDelay, ExecutionFatality {
			return getAttributeValue(SUBSTITUTION);
		}
		public boolean matches(String input) throws ExecutionDelay, ExecutionFatality {
			Pattern pattern = getPattern();
			if(matchNumber != null) {
				return pattern.matches(input, matchNumber.intValue());
			} else {
				return pattern.matches(input);
			}
		}
		public String[] allMatches(String input) throws NoMatches, ExecutionDelay, ExecutionFatality, MissingGroup {
			Pattern pattern = getPattern();
			String substitution = getSubstitution();
			if(matchNumber != null) {
				return new String[] { pattern.match(input, substitution, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input, substitution);
			}
		}
	}

}