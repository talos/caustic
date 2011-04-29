package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;

public abstract class Status {
	public final int code;
	private final String string;
	private Status(int code, String string) {
		this.code = code;
		this.string = string;
	}
	public String toString() {
		return string;
	}
	public abstract Status merge(Status other);
	public abstract String getResult();
	public final boolean isSuccessful() {
		return code == SUCCESSFUL_CODE;
	}
	public final boolean isInProgress() {
		return code == IN_PROGRESS_CODE;
	}
	public final boolean isFailure() {
		return code == FAILURE_CODE;
	}
	private static int SUCCESSFUL_CODE = 0;
	private static int IN_PROGRESS_CODE = 1;
	private static int FAILURE_CODE = 2;
	
	private static String SUCCESSFUL_STRING = "Successful";
	private static String IN_PROGRESS_STRING = "InProgress";
	private static String FAILURE_STRING = "Failure";
	
	public static final class Successful extends Status {
		private final String result;
		private final int merges;
		public Successful(String result) {
			super(SUCCESSFUL_CODE, SUCCESSFUL_STRING);
			this.result = result;
			this.merges = 0;
		}
		public String getResult() {
			return result;
		}
		public Successful(int merges) {
			super(SUCCESSFUL_CODE, SUCCESSFUL_STRING);
			this.merges = merges;
			this.result = Integer.toString(merges) + " successful merges.";
		}
		public Status merge(Status other) {
			if(other.code == SUCCESSFUL_CODE)
				return new Successful(((Successful) other).merges + this.merges);
			return other.merge(this); // Successful is weak.
		};
		public String toString() {
			return super.toString() + ": " + result;
		}
	}
	public static final class InProgress extends Status {
		Vector delays = new Vector();
		public InProgress() {
			super(IN_PROGRESS_CODE, IN_PROGRESS_STRING);
		}
		public InProgress(DelayExecution e) {
			super(IN_PROGRESS_CODE, IN_PROGRESS_STRING);
			delays.addElement(e);
		}
		public Status merge(Status other) {
			if(other.code == FAILURE_CODE) // Failure trumps all.
				return other;
			if(other.code == IN_PROGRESS_CODE) {
				Utils.arrayIntoVector(((InProgress) other).getDelays(), this.delays);
			}
			if(other.code == SUCCESSFUL_CODE && !isDelayed() )
				return other;
			return this;
		}
		public DelayExecution[] getDelays() {
			DelayExecution[] delays = new DelayExecution[numDelays()];
			this.delays.copyInto(delays);
			return delays;
		}
		public int numDelays() {
			return delays.size();
		}
		public boolean isDelayed() {
			if(numDelays() > 0 ) {
				return true;
			}
			return false;
		}
		public String getResult() {
			String string = "";
			if(isDelayed()) {
				string += "Delayed: " + Utils.join(getDelays(), ",");
			}
			return string;
		}
	}
	public static final class Failure extends Status {
		Vector throwables = new Vector();
		public Failure(Throwable e) {
			super(FAILURE_CODE, FAILURE_STRING);
			throwables.addElement(e);
		}
		public Status merge(Status other) {
			if(other.code != FAILURE_CODE)
				return this;
			Utils.arrayIntoVector(((Failure) other).getThrowables(), this.throwables);
			return this;
		}
		public Throwable[] getThrowables() {
			Throwable[] throwables = new Throwable[this.throwables.size()];
			this.throwables.copyInto(throwables);
			return throwables;
		}
		public String getResult() {
			String string = super.toString() + ", error: " + Utils.join(getThrowables(), ",");
			return string;
		}
	}
	public static interface DelayExecution {
		public Execution callerExecution();
		public String reason();
		public boolean equals(Object obj);
	}
	public static class WaitingForPrerequisite implements DelayExecution {
		private final Resource prerequisite;
		private final Execution caller;
		public WaitingForPrerequisite(Execution caller, Resource prerequisite) {
			this.prerequisite = prerequisite;
			this.caller = caller;
		}
		public Execution callerExecution() {
			return caller;
		}
		public String reason() {
			return "Waiting for " + prerequisite.ref().toString();
		}
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(!(obj instanceof WaitingForPrerequisite))
				return false;
			WaitingForPrerequisite other = (WaitingForPrerequisite) obj;
			if(this.caller.equals(other.caller) && this.prerequisite.ref().equals(other.prerequisite.ref()))
				return true;
			return false;
		}
	}
}