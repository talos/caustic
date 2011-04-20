package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Variables;

public abstract class Execution {
	protected final static class Status {
		public static Status SUCCESSFUL = new Status();
		public static Status IN_PROGRESS = new Status();
		public static Status FAILURE = new Status();
	}

	private class SuccessMatrix {
		private int successes = 0;
		private int in_progresses = 0;
		private int failures = 0;
		public void add(Status status) {
			if(status == Status.SUCCESSFUL)
				successes++;
			if(status == Status.IN_PROGRESS)
				in_progresses++;
			if(status == Status.FAILURE)
				failures++;
		}
		public Status summarize() {
			if(in_progresses == 0 && failures == 0)
				return Status.SUCCESSFUL;
			if(failures > 0)
				return Status.FAILURE;
			return Status.IN_PROGRESS;
		}
		private boolean equals(SuccessMatrix other) {
			if(successes == other.successes)
				return true;
			return false;
		}
	}
	
	private static int count = 0;
	public final int id;
	private final Vector call = new Vector();
	private final SuccessMatrix lastMatrix = new SuccessMatrix();
	
	public Execution() {
		id = count++;
	}
	
	public void add(Resource resource) {
		if(!call.contains(resource.ref)) {
			call.addElement(resource.ref);
		}
	}
	
	public Status execute() throws FatalExecutionException {
		SuccessMatrix matrix = new SuccessMatrix();
		for(int i = 0 ; i < call.size() ; i ++) {
			Resource resource = (Resource) call.elementAt(i);
			try {
				resource.execute(this);
				matrix.add(Status.SUCCESSFUL);
			} catch(MissingVariable e) {
				matrix.add(Status.IN_PROGRESS);
			}
		}
		if(matrix.equals(lastMatrix))
			return matrix.summarize();
		return execute(); // loop back if matrix has changed
	}
	
	public Result[] results() {
		
	}
	
	public final Variables variables() {
		//TODO
		return null;
	}
	
	/**
	 * Overridden for those few executions that are one-to-many.
	 * @return
	 */
	public boolean isOneToMany() {
		return false;
	}
	
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
		
	}
	
	public static final class FatalExecutionException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -567465590016762238L;
		public FatalExecutionException(Throwable e) { super(e); }
	}
}
