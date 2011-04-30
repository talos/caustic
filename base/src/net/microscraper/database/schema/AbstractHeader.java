package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionFatality;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;
import net.microscraper.database.Status;

public class AbstractHeader extends Resource {
	public static final AttributeDefinition NAME = new AttributeDefinition("name");
	public static final AttributeDefinition VALUE = new AttributeDefinition("value");

	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { NAME, VALUE }; }
			public RelationshipDefinition[] relationships() { return new RelationshipDefinition[] {}; }
		};
	}
	
	public Execution executionFromExecution(Execution caller) throws ExecutionFatality {
		return new AbstractHeaderExecution(this, caller);
	}
	
	public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
		AbstractHeaderExecution exc = (AbstractHeaderExecution) executionFromExecution(null);
		exc.addVariables(extraVariables);
		return exc; //.execute();
	}
	
	protected static class AbstractHeaderExecution extends ResourceExecution {
		private String name = null;
		private String value = null;
		public AbstractHeaderExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
		}
		
		public boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			return null;
		}
		protected String privateExecute() throws ExecutionDelay, ExecutionFatality {
			name = getAttributeValue(NAME);
			value = getAttributeValue(VALUE);
			return getName() + '=' + getValue();
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
	}
}