package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Variables;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Mustache.MissingVariable;

public abstract class Execution {
	
	private static int count = 0;
	private final Vector calledExecutions = new Vector();
	public final int id;
		
	//private final Execution source;
	private final Execution caller;
	protected Execution(Execution caller) {
		id = count++;
		this.caller = caller;
		if(caller != null) {
			this.caller.addCalledExecution(this);
		}
	}
	
	protected abstract boolean isOneToMany();
	
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
	
	public String toJSON() {
		return "";
	}
	
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
		Variables variables = new Variables();
		public Root() {
			super(null);
		}

		protected boolean isOneToMany() {
			return true;
		}

		protected Variables getLocalVariables() {
			return variables;
		}

		protected void execute() throws MissingVariable, BrowserException,
				FatalExecutionException, NoMatches {
			// TODO Auto-generated method stub
			
		}
		public void addVariables(Variables extraVariables) {
			variables.merge(extraVariables);
		}
	}
	
	public final static class Status {
		public static Status SUCCESSFUL = new Status();
		public static Status IN_PROGRESS = new Status();
		public static Status FAILURE = new Status();
	}
	
	public static final class FatalExecutionException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -567465590016762238L;
		public FatalExecutionException(Throwable e) { super(e); }
	}
}
