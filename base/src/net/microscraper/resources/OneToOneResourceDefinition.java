package net.microscraper.resources;

import net.microscraper.client.Client;
import net.microscraper.client.Utils.HashtableWithNulls;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;

public abstract class OneToOneResourceDefinition extends ResourceDefinition {
	private final HashtableWithNulls executions = new HashtableWithNulls();
	public Execution executionFromExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality {
		Execution exc = (Execution) executions.get(caller);
		if(exc == null) {
			exc = generateExecution(caller);
			executions.put(caller, exc);
		}
		return exc;
	}
	/*
	public Execution executionFromVariables(Variables extraVariables) throws ExecutionFatality {
		Execution exc = executionFromExecution(null);
		exc.addVariables(extraVariables);
		return exc;
	}
	public Status execute(Variables extraVariables) throws ExecutionFatality {
		return executionFromVariables(extraVariables).safeExecute();
	}
	*/
	protected abstract Execution generateExecution(Client client, Resource resource, Execution caller) throws ExecutionFatality;
	public final boolean isOneToMany() {
		return false;
	}
}
