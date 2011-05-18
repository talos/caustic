package net.microscraper.execution;

import net.microscraper.model.Executable;

public abstract class Execution {
	private final Executable executable;
	public Execution(Executable executable) {
		this.executable = executable;
	}
	
	public abstract boolean isComplete();
	public abstract boolean isStuck();
}
