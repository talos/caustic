package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;

public class Status {
	private Vector delays = new Vector();
	private Vector failures = new Vector();

	public void addDelay(ExecutionDelay e) {
		delays.addElement(e);
	}
	public void addFailure(ExecutionFailure e) {
		failures.addElement(e);
	}
	public void merge(Status other) {
		Utils.vectorIntoVector(other.delays, this.delays);
		Utils.vectorIntoVector(other.failures, this.failures);
	}
	public boolean hasDelay() {
		return delays.size() > 0;
	}
	public boolean hasFailure() {
		return failures.size() > 0;
	}
	public boolean shouldRetry() {
		if(hasDelay() && !hasFailure()) {
			return true;
		}
		return false;
	}
	
	// If any previous delay is no longer in our status, we have made progress.
	public boolean progressSince(Status lastStatus) {
		for(int i = 0 ; i < lastStatus.delays.size() ; i ++ ) {
			ExecutionDelay previousDelay = (ExecutionDelay) lastStatus.delays.elementAt(i);
			if(!this.delays.contains(previousDelay))
				return false;
		}
		return true;
	}
}