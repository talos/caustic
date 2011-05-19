package net.microscraper.execution;

import net.microscraper.model.Resource;

public interface Execution extends Runnable {
	public abstract boolean hasCaller();
	public abstract Execution getCaller();
	public abstract Resource getResource();
	public abstract boolean isStuck();
	public abstract boolean hasFailed();
	public abstract boolean isComplete();
	public abstract Execution[] getChildren();
	public abstract int getId();
	public abstract boolean hasPublishName();
	public abstract String getPublishName();
	public abstract boolean hasPublishValue();
	public abstract String getPublishValue();
}
