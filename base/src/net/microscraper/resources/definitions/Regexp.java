package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.Execution;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Execution.ExecutionDelay;
import net.microscraper.resources.Execution.ExecutionFatality;
import net.microscraper.resources.Resource.OneToOneResource;

public class Regexp extends OneToOneResource {
	private static final AttributeDefinition REGEXP = new AttributeDefinition("regexp");
	private static final AttributeDefinition REPLACEMENT = new AttributeDefinition("replacement");
	private static final AttributeDefinition MATCH_NUMBER = new AttributeDefinition("match_number");
	private static final AttributeDefinition CASE_INSENSITIVE = new AttributeDefinition("case_insensitive");
	private static final AttributeDefinition MULTILINE = new AttributeDefinition("multiline");
	private static final AttributeDefinition DOT_MATCHES_NEWLINE = new AttributeDefinition("dot_matches_newline");
	
	public AttributeDefinition[] getAttributeDefinitions() {
		return new AttributeDefinition[] {
				REGEXP, MATCH_NUMBER, REPLACEMENT,
				CASE_INSENSITIVE, MULTILINE, DOT_MATCHES_NEWLINE };
	}
	public RelationshipDefinition[] getRelationshipDefinitions() {
		return new RelationshipDefinition[] { };
	}

	protected Execution generateExecution(Execution caller) throws ExecutionFatality {
		return new RegexpExecution(this, caller);
	}
	
	public static final class RegexpExecution extends Execution {
		private final Integer matchNumber;
		protected RegexpExecution(Resource resource, Execution caller) {
			super(resource, caller);
			matchNumber = resource.getIntegerAttribute(MATCH_NUMBER);
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFatality {
			return getAttributeValue(REGEXP);
		}
		public Pattern getPattern() throws ExecutionDelay, ExecutionFatality {
			return client.regexp.compile(
					getAttributeValue(REGEXP),
					getBooleanAttribute(CASE_INSENSITIVE),
					getBooleanAttribute(MULTILINE),
					getBooleanAttribute(DOT_MATCHES_NEWLINE));
		}
		private String getReplacement() throws ExecutionDelay, ExecutionFatality {
			return getAttributeValue(REPLACEMENT);
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
			String replacement = getReplacement();
			if(matchNumber != null) {
				return new String[] { pattern.match(input, replacement, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input, replacement);
			}
		}
	}

}