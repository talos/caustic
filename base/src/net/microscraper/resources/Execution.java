package net.microscraper.resources;

import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Resources.ResourceException;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFailure;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFatality;
import net.microscraper.resources.DefaultExecutionProblem.StatusException;
import net.microscraper.resources.definitions.Scraper.ScraperResult;

public abstract class Execution {
	private static int count = 0;

	private final Vector calledExecutions = new Vector();
	//private final Variables extraVariables = new Variables();
	private final Client client;
	private final Resource resource;
	private final Execution caller;	
	
	private Status lastStatus = new Status();
	private Result result;
	public final int id;
	
	public Execution(Client client, Resource resource, Execution caller) {
		id = count++;
		this.client = client;
		this.resource = resource;
		this.caller = caller;
		if(caller != null) {
			this.caller.addCalledExecution(this);
		}
	}
	
	public final Execution getSourceExecution() {
		if(isOneToMany() || caller == null) {
			return this;
		} else {
			return caller.getSourceExecution();
		}
	}
	private final void addCalledExecution(Execution called) {
		getSourceExecution().calledExecutions.addElement(called);
	}
	public final ScraperResult[] getScraperResults() {
		// TODO
		return null;
	}
	
	/*
	public final Variables getVariables() {
		Variables variables = new Variables().merge(extraVariables);
		for(int i = 0 ; i < calledExecutions.size() ; i ++) {
			Execution calledExecution = (Execution) calledExecutions.elementAt(i);
			if(!calledExecution.isOneToMany()) {
				variables.merge(calledExecution.getLocalVariables());
			}
		}
		// Ascend up the source tree.
		if(getSourceExecution() == this) {
			return variables;
		} else {
			return variables.merge(getSourceExecution().getVariables());
		}
	}
	*/
	/*
	public void addVariables(Variables extraVariables) {
		this.extraVariables.merge(extraVariables);
	}
	*/
	protected final boolean isOneToMany() {
		return resource.isOneToMany();
	}
	/*
	protected final Execution callResource(Resource resource) throws ExecutionFatality {
		return resource.executionFromExecution(getSourceExecution());
	}
	*/
	public String getPublishName() {
		return resource.ref().toString();
	}
	protected String getStringAttributeValue(AttributeDefinition def) throws ExecutionDelay, ExecutionFatality {
		try {
			return (String) Mustache.compile(resource.getRawStringAttribute(def), caller.getScraperResults());
		} catch(MissingVariable e) {
			throw new ExecutionDelay(caller, e);
		} catch(TemplateException e) {
			throw new ExecutionFatality(caller, e);
		}
	}
	
	protected Resource[] getRelatedResources(RelationshipDefinition def) throws ExecutionFatality {
		try {
			return resource.getRelatedResources(def);
		} catch(ResourceException e) {
			throw new ExecutionFatality(this, e);
		}
	}
	/*
	protected final Variables getLocalVariables() {
		if(result != null && resource.isPublishedToVariables()) {
			Variables variables = new Variables();
			variables.put(resource.ref().title, result);
			return variables;
		}
		return null;
	}
	*/
	public final Status safeExecute() throws ExecutionFatality {
		client.log.i("Safely executing " + resource.ref().toString() + ":" + Integer.toString(id));
		if(!lastStatus.hasFailure()) {
			Status status = new Status();
			try {
				try {
					result = privateExecute();
					status.addSuccess(result);
				} catch(ExecutionDelay e) {
					status.addDelay(e);
				} catch(ExecutionFailure e) {
					status.addFailure(e);
				} catch(StatusException e) {
					status.merge(e.getStatus());
				}
				lastStatus = status;
				client.publisher.publish(this, lastStatus);
			} catch(PublisherException e) {
				throw new ExecutionFatality(this, e);
			}
		}
		return lastStatus;
	}
	public final Result unsafeExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException {
		client.log.i("Unsafely executing " + resource.ref().toString() + ":" + Integer.toString(id));
		if(lastStatus.hasFailure())
			throw new StatusException(lastStatus);
		if(result == null) {
			result = privateExecute();
			lastStatus = new Status();
			lastStatus.addSuccess(result);
			try {
				client.publisher.publish(this, lastStatus);
			} catch(PublisherException e) {
				throw new ExecutionFatality(this, e);
			}
		}
		return result;
	}
	
	protected abstract Result privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality, StatusException;
	
	public final boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Execution))
			return false;
		if(this.hashCode() == obj.hashCode())
			return true;
		return false;
	}
	
	public final int hashCode() {
		return id;
	}
}
