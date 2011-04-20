package net.microscraper.database.schema;

import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.FatalExecutionException;
import net.microscraper.database.Execution.ResourceExecution;
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
	
	private static class AbstractHeaderResult implements Result {
		private final String name;
		private final String value;
		protected AbstractHeaderResult(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public Object value() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private class AbstractHeaderExecution extends ResourceExecution {
		public AbstractHeaderExecution(Resource resource, Execution caller) {
			super(resource, caller);
		}

		public Result getResult() throws MissingVariable {
			try {
				return new AbstractHeaderResult(
					attributes.get(NAME, caller.variables()),
					attributes.get(VALUE, caller.variables())
					);
			} catch(TemplateException e) {
				throw new FatalExecutionException(e);
			}
		}
	}
	
	public ResourceExecution getExecution(Execution caller)
			throws MissingVariable, FatalExecutionException {
		return new AbstractHeaderExecution(this, caller);
	}
}