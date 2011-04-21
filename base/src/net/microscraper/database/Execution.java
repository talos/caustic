package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.database.Resource.ResourceExecution;

public abstract class Execution {
	
	private static int count = 0;
	//private final ExecutionMatrix matrix = new ExecutionMatrix();
	private Vector calledExecutions = new Vector();
	public final int id;
	
	protected Execution() {
		id = count++;
	}
	
	public void call(Resource resource) throws FatalExecutionException {
		calledExecutions.add(resource.getExecution(this));
	}
	
	public ResourceExecution[] getCalledExecutions() {
		ResourceExecution[] executions = new ResourceExecution[calledExecutions.size()];
		calledExecutions.copyInto(executions);
		return executions;
	}
	
	public abstract Execution getSourceExecution();
	public abstract Status getStatus();
	public abstract Variables getVariables();
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Execution))
			return false;
		if(this.hashCode() == obj.hashCode())
			return true;
		return false;
	}
	public int hashCode() {
		return id;
	}
	
	public static final class Root extends Execution {
		public Status getStatus() {
			return new StatusMatrix().summarize();
		}
		
		public Variables getVariables() {
			Variables variables = new Variables();
			ResourceExecution[] executions = getCalledExecutions();
			for(int i = 0 ; i < executions.length ; i ++) {
				variables.merge(executions[i].getVariables());
			}
			return variables;
		}

		public Execution getSourceExecution() {
			return this;
		}
	}
	
	protected final static class Status {
		public static Status SUCCESSFUL = new Status();
		public static Status IN_PROGRESS = new Status();
		public static Status FAILURE = new Status();
	}
	
	protected final class StatusMatrix {
		public Status summarize() {
			for(int i = 0 ; i < calledExecutions.size() ; i ++) {
				ResourceExecution exc = (ResourceExecution) calledExecutions.elementAt(i);
				if(exc.getStatus() == Status.IN_PROGRESS)
					return Status.IN_PROGRESS;
				if(exc.getStatus() == Status.FAILURE)
					return Status.FAILURE;
			}
			return Status.SUCCESSFUL;
		}
		public boolean isProgressSince(StatusMatrix comparison) {
			
		}
	}

	public static final class FatalExecutionException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -567465590016762238L;
		public FatalExecutionException(Throwable e) { super(e); }
	}
}
