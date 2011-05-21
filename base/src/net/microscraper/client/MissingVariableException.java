package net.microscraper.client;

import net.microscraper.execution.Variables;

public class MissingVariableException extends Exception {
	public final String name;
	public MissingVariableException(Variables variables, String missingVariableName) {
		this.name  = missingVariableName;
	}
}
