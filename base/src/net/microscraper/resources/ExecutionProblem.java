package net.microscraper.resources;

public interface ExecutionProblem {
	public Execution callerExecution();
	public Class problemClass();
	public String reason();
	public boolean equals(Object obj);
}
