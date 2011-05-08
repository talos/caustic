package net.microscraper.resources;

import net.microscraper.client.Client;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;

public abstract class OneToOneResourceDefinition implements ResourceDefinition {
	public abstract Execution generateExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality;
	public final Execution[] generateExecutions(Client client, Resource resource, Execution caller) throws ExecutionFatality {
		return new Execution[] { generateExecution(client, resource, caller) };
	}
	public boolean isOneToMany() {
		return false;
	}
}
