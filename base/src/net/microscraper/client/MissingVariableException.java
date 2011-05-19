package net.microscraper.client;

import net.microscraper.execution.HasVariableExecutions;

public class MissingVariableException extends Exception {
	public final String name;
	public MissingVariableException(HasVariableExecutions variables, String missingVariableName) {
		this.name  = missingVariableName;
	}
}
