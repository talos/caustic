package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.database.Database.ResourceNotFoundException;
import net.microscraper.database.Resource.ResourceExecution;

public abstract class Execution {
	
	private static int count = 0;
	//private final ExecutionMatrix matrix = new ExecutionMatrix();
	private final Vector calledExecutions = new Vector();
	public final int id;
		
	private final Execution source;
	protected Execution(Execution caller) {
		id = count++;
		if(isOneToMany()) {
			this.source = this;
		} else {
			this.source = caller;
		}
	}
	protected abstract boolean isOneToMany();

	protected final Execution getSourceExecution() {
		return source;
	}
	
	public final void addCalledExecution(Execution called) {
		calledExecutions.addElement(called);
	}
	
	protected final Variables getVariables() {
		Variables variables = new Variables();
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
	
	protected abstract Variables getLocalVariables();
	/*
	public Status getStatus() {
		return new StatusMatrix().summarize();
	}
	*/
	protected abstract void execute()
			throws MissingVariable, BrowserException, FatalExecutionException, NoMatches;

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
	
	public static final class Root extends Execution {

		public Root() {
			super(null);
		}

		protected boolean isOneToMany() {
			return true;
		}

		protected Variables getLocalVariables() {
			return null;
		}

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			
		}
	}
	
	protected final static class Status {
		public static Status SUCCESSFUL = new Status();
		public static Status IN_PROGRESS = new Status();
		public static Status FAILURE = new Status();
	}
	
	/*
	private final class StatusMatrix {
		private static final int SUCCESSFUL = 0;
		private static final int IN_PROGRESS = 1;
		private static final int FAILURE = 2;
		protected Status summarize() {
			Execution[] executions = getCalledExecutions();
			for(int i = 0 ; i < executions.length ; i ++) {
				if(executions[i].getStatus() == Status.IN_PROGRESS)
					return Status.IN_PROGRESS;
				if(executions[i].getStatus() == Status.FAILURE)
					return Status.FAILURE;
			}
			return Status.SUCCESSFUL;
		}
		protected boolean hasBeenProgressSince(StatusMatrix comparison) {
			int[] status = status();
			int[] comparisonStatus = comparison.status();
			if(status[SUCCESSFUL] > comparisonStatus[SUCCESSFUL])
				return true;
			if(status[FAILURE] > comparisonStatus[FAILURE])
				return true;
			return false;
		}
		private int[] status() {
			Execution[] executions = getCalledExecutions();
			int[] status = new int[3];
			status[SUCCESSFUL] = 0;
			status[IN_PROGRESS] = 0;
			status[FAILURE] = 0;
			for(int i = 0 ; i < executions.length ; i ++) {
				if(executions[i].getStatus() == Status.SUCCESSFUL)
					status[SUCCESSFUL]++;
				if(executions[i].getStatus() == Status.IN_PROGRESS)
					status[IN_PROGRESS]++;
				if(executions[i].getStatus() == Status.FAILURE)
					status[FAILURE]++;
			}
			return status;
		}
	}
*/
	public static final class FatalExecutionException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -567465590016762238L;
		public FatalExecutionException(Throwable e) { super(e); }
	}
}
