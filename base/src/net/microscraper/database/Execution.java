package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Mustache;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Variables;
import net.microscraper.database.Attribute.AttributeDefinition;
import net.microscraper.database.Database.ResourceNotFoundException;

public abstract class Execution {
	private static int count = 0;

	private final Vector calledExecutions = new Vector();
	private final Variables extraVariables = new Variables();
	private final Execution caller;	
	private final Resource resource;
	private final String publishName;

	private Status lastStatus = new Status.NotYetStarted();
	public final int id;
	

	protected Execution(Resource resource, Execution caller) {
		id = count++;
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
	
	protected final Variables getVariables() {
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

	public void addVariables(Variables extraVariables) {
		this.extraVariables.merge(extraVariables);
	}
	
	protected abstract boolean isOneToMany();
	protected Variables getLocalVariables() {
		return null;
	}
	
	protected final Execution callResource(Resource resource) throws ExecutionFatality {
		return resource.executionFromExecution(getSourceExecution());
	}
	public final Status getStatus() {
		return lastStatus;
	}
	
	public String getPublishName() {
		return resource.ref().toString();
	}
	protected final String getAttributeValue(AttributeDefinition def)
				throws TemplateException, MissingVariable {
		return (String) Mustache.compile(this, resource.getStringAttribute(def), getVariables());
	}
	// TODO if we're publishing, and we're successful, add to local variables list.
	/*protected Variables getLocalVariables() {
		Variables variables = new Variables();
		variables.put(scraper.ref().title, match);
		return variables;
	}*/
	
	/*private final Status execute() throws ResourceNotFoundException, InterruptedException {
		if(lastStatus.isInProgress()) {
			Status status;
			try {
				status = privateExecute();
			} catch(WaitingForExecution e) {
				status = new Status.InProgress(e);
			} catch(MissingVariable e) {
				status = new Status.InProgress(e);
			} catch(TemplateException e) {
				status = new Status.Failure(e);
			} catch(BrowserException e) {
				status = new Status.Failure(e);
			}
			try {
				Client.publisher.publish(this);
			} catch(PublisherException e) {
				Client.log.e(e);
			}
			Client.log.i("Executing " + getPublishName() + " resulted in " + status.toString());
			lastStatus = status;
		}
		return lastStatus;
	}*/
	public final Status safeExecute() throws ExecutionFatality {
		
	}
	public final String unsafeExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		
	}
	
	protected abstract String privateExecute() throws ExecutionDelay, ExecutionFailure, ExecutionFatality;
	
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
	
	public static interface ExecutionProblem {
		public Execution callerExecution();
		public String reason();
		public boolean equals(Object obj);
	}
	
	public static abstract class DefaultExecutionProblem extends Exception implements ExecutionProblem {
		private Execution caller;
		public DefaultExecutionProblem(Execution caller) {
			this.caller = caller;
		}
		public Execution callerExecution() {
			return caller;
		}
		public final boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof ExecutionProblem))
				return false;
			ExecutionProblem that = (ExecutionProblem) obj;
			if(this.callerExecution().equals(that.callerExecution()) && this.reason().equals(that.reason()))
				return true;
			return false;
		}
	}
	
	public static class ExecutionDelay extends DefaultExecutionProblem {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1887704359270171496L;
	}
	
	public static class ExecutionFailure extends DefaultExecutionProblem {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5646674827768905150L;
	}
	
	public static class ExecutionFatality extends DefaultExecutionProblem {
		public ExecutionFatality(Throwable e, Execution caller) {
			super(caller);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -5646674827768905150L;
	}
}
