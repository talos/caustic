package net.microscraper.execution;

import java.net.URI;

public interface Execution extends Runnable {

	public abstract int getId();
	
	public abstract URI getResourceLocation();
	
	public abstract boolean hasCaller();
	public abstract Execution getCaller();
	public abstract Execution[] getChildren();
	
	
	public abstract boolean isStuck();
	public abstract boolean hasFailed();
	public abstract boolean isComplete();
	
	public abstract boolean hasPublishName();
	public abstract String getPublishName();
	public abstract boolean hasPublishValue();
	public abstract String getPublishValue();
}
