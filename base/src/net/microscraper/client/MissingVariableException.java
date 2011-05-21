package net.microscraper.client;


public class MissingVariableException extends Exception {
	public final String name;
	public MissingVariableException(Variables variables, String missingVariableName) {
		this.name  = missingVariableName;
	}
}
