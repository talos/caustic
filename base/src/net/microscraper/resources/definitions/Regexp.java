package net.microscraper.resources.definitions;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Regexp.MissingGroup;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.resources.AttributeDefinition;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.Execution;
import net.microscraper.resources.OneToOneResourceDefinition;
import net.microscraper.resources.RelationshipDefinition;
import net.microscraper.resources.Resource;
import net.microscraper.resources.Result;

public class Regexp extends OneToOneResourceDefinition {
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

	public Execution generateExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
		return new RegexpExecution(client, resource, caller);
	}
	
	public static final class RegexpExecution extends Execution {
		private final Integer matchNumber;
		private final boolean caseInsensitive;
		private final boolean multiline;
		private final boolean dotMatchesNewline;
		private final Client client;
		public RegexpExecution(Client client, Resource resource, Execution caller) {
			super(client, resource, caller);
			this.client = client;
			matchNumber = resource.getIntegerAttribute(MATCH_NUMBER);
			caseInsensitive = resource.getBooleanAttribute(CASE_INSENSITIVE);
			multiline = resource.getBooleanAttribute(MULTILINE);
			dotMatchesNewline = resource.getBooleanAttribute(DOT_MATCHES_NEWLINE);
		}
		protected Result privateExecute() throws ExecutionDelay, ExecutionFatality {
			return new RegexpResult(
				client.regexp.compile(getStringAttributeValue(REGEXP), caseInsensitive, multiline, dotMatchesNewline),
				getStringAttributeValue(REPLACEMENT), matchNumber);
		}
	}
	public static class RegexpResult implements Result {
		private final Interfaces.Regexp.Pattern pattern;
		private final String replacement;
		private final Integer matchNumber;
		public RegexpResult(Interfaces.Regexp.Pattern pattern, String replacement, Integer matchNumber) {
			this.pattern = pattern;
			this.replacement = replacement;
			this.matchNumber = matchNumber;
		}
		public boolean matches(String input) throws ExecutionDelay, ExecutionFatality {
			if(matchNumber != null) {
				return pattern.matches(input, matchNumber.intValue());
			} else {
				return pattern.matches(input);
			}
		}
		public String[] allMatches(String input) throws NoMatches, ExecutionDelay, ExecutionFatality, MissingGroup {
			if(matchNumber != null) {
				return new String[] { pattern.match(input, replacement, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input, replacement);
			}
		}
	}
}