package net.microscraper.execution;

public interface Execution extends Runnable {
	public abstract boolean isStuck();
	public abstract boolean hasFailed();
	public abstract boolean isComplete();
	//public abstract Execution[] children();
}
