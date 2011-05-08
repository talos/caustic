package net.microscraper.resources;

import java.util.Hashtable;

import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Object;
import net.microscraper.client.Resources;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;

public final class OneToManyResource extends Resource {
	public OneToManyResource(Resources resources,
			OneToManyResourceDefinition definition, String fullName, Object jsonObject)
			throws JSONInterfaceException, InstantiationException,
			IllegalAccessException {
		super(resources, definition, fullName, jsonObject);
	}
	protected final Hashtable executions = new Hashtable();	
	public final Execution[] executionsFromExecution(Execution caller) throws ExecutionFatality {
		if(!executions.containsKey(caller)) {
			executions.put(caller, generateExecutions(caller));
		}
		return (Execution[]) executions.get(caller);
	}
}
