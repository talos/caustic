package net.microscraper.resources;

import net.microscraper.client.Client;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;

public interface ResourceDefinition {
	public abstract AttributeDefinition[] getAttributeDefinitions();
	public abstract RelationshipDefinition[] getRelationshipDefinitions();
	public abstract Execution[] generateExecutions(Client client, Resource resource, Execution caller) throws ExecutionFatality;
	public abstract boolean isOneToMany();
}
