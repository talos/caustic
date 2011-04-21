package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
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
	
	public static class AbstractHeaderResult implements Result {
		private final String name;
		private final String value;
		private AbstractHeaderResult(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
	}

	private class AbstractHeaderExecution extends ResourceExecution {
		private final Execution caller;
		public AbstractHeaderExecution(Execution caller) {
			this.caller = caller;
		}
		
		public Result getResult() throws MissingVariable, FatalExecutionException {
			try {
				return new AbstractHeaderResult(
					attributes.get(NAME, caller.variables()),
					attributes.get(VALUE, caller.variables())
					);
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}

		protected Status execute() throws FatalExecutionException {
			try {
				getResult();
				return Status.SUCCESSFUL;
			} catch(MissingVariable e) {
				return Status.IN_PROGRESS;
			}
		}

		public Variables variables() {
			return new Variables();
		}

		public boolean isOneToMany() {
			return false;
		}
	}
	
	public ResourceExecution getExecution(Execution caller)
			throws MissingVariable, FatalExecutionException {
		return new AbstractHeaderExecution(caller);
	}
}