package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Variables;
import net.microscraper.database.Database.ResourceNotFoundException;

public abstract class Execution {
	private static int count = 0;

	private final Vector calledExecutions = new Vector();
	private final Variables extraVariables = new Variables();
	private final Execution caller;	
	private Status status = Status.IN_PROGRESS;
	public final int id;
	protected Execution(Execution caller) {
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
	protected abstract Variables getLocalVariables();
	public abstract String getPublishName();
	public abstract String getPublishValue();
	public final Status getStatus() {
		return status;
	}
	public final Status execute() throws ResourceNotFoundException {
		if(status == Status.IN_PROGRESS) {
			status = privateExecute();
		}
		try {
			Client.publisher.publish(this);
		} catch(PublisherException e) {
			Client.log.e(e);
		}
		return status;
	}
	protected abstract Status privateExecute() throws ResourceNotFoundException;
	
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
	
	public final static class Status {
		public final int code;
		private Status(int code) {
			this.code = code;
		}
		public static Status SUCCESSFUL = new Status(0);
		public static Status IN_PROGRESS = new Status(1);
		public static Status FAILURE = new Status(2);
		public Status join(Status other) {
			if(this == Status.FAILURE || other == Status.FAILURE)
				return Status.FAILURE;
			if(this == Status.IN_PROGRESS || other == Status.IN_PROGRESS)
				return Status.IN_PROGRESS;
			return Status.SUCCESSFUL;
		}
	}
}
