package net.microscraper.database.schema;

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

	public Status execute(Variables extraVariables) throws ResourceNotFoundException {
		try {
			AbstractHeaderExecution exc = getExecution(null);
			exc.addVariables(extraVariables);
			exc.execute();	
			return Status.SUCCESSFUL;
		} catch(MissingVariable e) {
			return Status.IN_PROGRESS;
		} catch(FatalExecutionException e) {
			return Status.FAILURE;
		}
	}
	
	protected static class AbstractHeaderExecution extends ResourceExecution {
		private String name = null;
		private String value = null;
		private Status status = Status.IN_PROGRESS;
		public AbstractHeaderExecution(Resource resource, Execution caller) throws ResourceNotFoundException {
			super(resource, caller);
		}
		
		public boolean isOneToMany() {
			return false;
		}
		
		protected Variables getLocalVariables() {
			return new Variables();
		}
		
		protected void execute() throws MissingVariable, FatalExecutionException {
			if(status == Status.IN_PROGRESS) {
				try {
					name = getAttributeValue(NAME);
					value = getAttributeValue(VALUE);
					status = Status.SUCCESSFUL;
				} catch(TemplateException e) {
					status = Status.FAILURE;
					throw new FatalExecutionException(e);
				}
			}
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}

		public Status getStatus() {
			return status;
		}
	}
}