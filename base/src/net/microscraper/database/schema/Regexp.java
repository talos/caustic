package net.microscraper.database.schema;

import java.util.Hashtable;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class Regexp extends Resource {
	private static final AttributeDefinition REGEXP = new AttributeDefinition("regexp");
	private static final AttributeDefinition MATCH_NUMBER = new AttributeDefinition("match_number");
	private final Hashtable executions = new Hashtable();
		
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
		if(!executions.containsKey(caller)) {
			executions.put(caller, new RegexpExecution(this, caller));
		}
		return (RegexpExecution) executions.get(caller);
	}
	
	public Status execute(Variables extraVariables) throws ResourceNotFoundException {
		try {
			RegexpExecution exc = getExecution(null);
			exc.addVariables(extraVariables);
			exc.execute();
			return Status.SUCCESSFUL;
		} catch(MissingVariable e) {
			return Status.IN_PROGRESS;
		} catch(FatalExecutionException e) {
			return Status.FAILURE;
		}
	}
	
	public static final class RegexpExecution extends ResourceExecution {
		private Pattern pattern;
		private final Integer matchNumber;
		protected RegexpExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
			String matchNumberString = resource.getAttributeValueRaw(MATCH_NUMBER);
			if(matchNumberString == null) {
				matchNumber = null;
			} else {
				matchNumber = new Integer(matchNumberString);
			}
		}
		
		protected boolean isOneToMany() {
			if(matchNumber == null)
				return true;
			return false;
		}
		
		protected Variables getLocalVariables() {
			return new Variables();
		}
		
		protected void execute() throws MissingVariable,
				FatalExecutionException {
			try {
				pattern = Client.regexp.compile(getAttributeValue(REGEXP));
			} catch (TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
		
		public boolean matches(String input) throws MissingVariable, FatalExecutionException {
			if(pattern == null)
				execute();
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
		
		public String[] allMatches(String input) throws NoMatches, MissingVariable, FatalExecutionException {
			if(pattern == null)
				execute();
			if(matchNumber != null) {
				return new String[] { pattern.match(input, matchNumber.intValue()) };
			} else {
				return pattern.allMatches(input);
			}
		}
	}
}