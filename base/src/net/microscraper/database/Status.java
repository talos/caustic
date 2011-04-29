package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Mustache.MissingVariable;
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
	
	private static String SUCCESSFUL_STRING = "successful";
	private static String IN_PROGRESS_STRING = "in progress";
	private static String FAILURE_STRING = "failure";
	
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
		Vector missingVariables = new Vector();
		public InProgress() {
			super(IN_PROGRESS_CODE, IN_PROGRESS_STRING);
		}
		public InProgress(MissingVariable e) {
			super(IN_PROGRESS_CODE, IN_PROGRESS_STRING);
			missingVariables.addElement(e);
		}
		public Status merge(Status other) {
			if(other.code == FAILURE_CODE) // Failure trumps all.
				return other;
			if(other.code == IN_PROGRESS_CODE) {
				Utils.arrayIntoVector(((InProgress) other).getMissingVariables(), this.missingVariables);
			}
			if(other.code == SUCCESSFUL_CODE && !isMissingVariables() )
				return other;
			return this;
		}
		public MissingVariable[] getMissingVariables() {
			MissingVariable[] missingVariables = new MissingVariable[this.missingVariables.size()];
			this.missingVariables.copyInto(missingVariables);
			return missingVariables;
		}
		public boolean isMissingVariables() {
			if(missingVariables.size() > 0 ) {
				return true;
			}
			Client.log.i("false");
			return false;
		}
		public String toString() {
			String string = super.toString();
			if(isMissingVariables()) {
				string += ", missing: " + Utils.join(getMissingVariables(), ",");
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
		public String toString() {
			String string = super.toString() + ", error: " + Utils.join(getThrowables(), ",");
			return string;
		}
	}
}