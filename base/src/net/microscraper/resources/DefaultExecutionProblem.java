package net.microscraper.resources;


public abstract class DefaultExecutionProblem extends Exception implements ExecutionProblem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1875711681202830444L;
	private Execution caller;
	private Throwable throwable;
	public DefaultExecutionProblem(Execution caller, Throwable e) {
		this.caller = caller;
		this.throwable = e;
	}
	public Execution callerExecution() {
		return caller;
	}
	public Class problemClass() {
		return throwable.getClass();
	}
	public String reason() {
		return throwable.getMessage();
	}
	public String getMessage() {
		if(caller != null)
			return caller.toString() + " caused " + problemClass().toString() + " because " + reason();
		return problemClass().toString() + " because " + reason();
	}
	public final boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof ExecutionProblem))
			return false;
		ExecutionProblem that = (ExecutionProblem) obj;
		if(this.callerExecution() == null && that.callerExecution() == null && this.reason().equals(that.reason()))
			return true;
		if(this.callerExecution() == null || that.callerExecution() == null)
			return false;
		if(this.callerExecution().equals(that.callerExecution()) && this.reason().equals(that.reason()))
			return true;
		return false;
	}

	public static class ExecutionDelay extends DefaultExecutionProblem {
		public ExecutionDelay(Execution caller, Throwable e) {
			super(caller, e);
		}
		private static final long serialVersionUID = 1887704359270171496L;
	}

	public static class ExecutionFailure extends DefaultExecutionProblem {
		public ExecutionFailure(Execution caller, Throwable e) {
			super(caller, e);
		}
		private static final long serialVersionUID = -5646674827768905150L;
	}

	public static class ExecutionFatality extends DefaultExecutionProblem {
		public ExecutionFatality(Execution caller, Throwable e) {
			super(caller, e);
		}
		private static final long serialVersionUID = -5646674827768905150L;
	}

	// A way of tossing status through exceptions.
	public static class StatusException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3155033955368165156L;
		private final Status status;
		public StatusException(Status status) {
			this.status = status;
		}
		public Status getStatus() {
			return status;
		}
	}
}
