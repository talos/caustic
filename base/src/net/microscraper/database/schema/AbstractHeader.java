package net.microscraper.database.schema;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;
import net.microscraper.database.Model.ModelDefinition;
import net.microscraper.database.Relationship.RelationshipDefinition;
import net.microscraper.database.Resource;

public class AbstractHeader extends Resource {
	public static final AttributeDefinition NAME = new AttributeDefinition("name");
	public static final AttributeDefinition VALUE = new AttributeDefinition("value");

	public ModelDefinition definition() {
		return new ModelDefinition() {
			public AttributeDefinition[] attributes() { return new AttributeDefinition[] { NAME, VALUE }; }
			public RelationshipDefinition[] relationships() { return new RelationshipDefinition[] {}; }
		};
	}

	public AbstractHeaderExecution getExecution(Execution caller)
			throws ResourceNotFoundException {
		return new AbstractHeaderExecution(this, caller);
	}

	public Status execute(Variables extraVariables) throws ResourceNotFoundException, InterruptedException {
		AbstractHeaderExecution exc = getExecution(null);
		exc.addVariables(extraVariables);
		return exc.execute();
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
			return new Variables();
		}
		
		protected Status privateExecute() {
			try {
				name = getAttributeValue(NAME);
				value = getAttributeValue(VALUE);
				return Status.SUCCESSFUL;
			} catch(MissingVariable e) {
				return Status.IN_PROGRESS;
			} catch(TemplateException e) {
				return Status.FAILURE;
			}
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
		public String getPublishValue() {
			return getName() + '=' + getValue();
		}
	}
}