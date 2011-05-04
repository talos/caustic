package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;
import net.microscraper.database.Execution.ExecutionProblem;

public class Status {
	private Vector successes = new Vector();
	private Vector delays = new Vector();
	private Vector failures = new Vector();
	
	public void addSuccess(String success ){
		successes.addElement(success);
	}
	public void addDelay(ExecutionDelay e) {
		delays.addElement(e);
	}
	public void addFailure(ExecutionFailure e) {
		failures.addElement(e);
	}
	public void merge(Status other) {
		Utils.vectorIntoVector(other.successes, this.successes);
		Utils.vectorIntoVector(other.delays, this.delays);
		Utils.vectorIntoVector(other.failures, this.failures);
	}
	public boolean hasDelay() {
		return delays.size() > 0;
	}
	public boolean hasFailure() {
		return failures.size() > 0;
	}
	public String[] successes() {
		String[] successes = new String[this.successes.size()];
		this.successes.copyInto(successes);
		return successes;
	}
	// convenience method
	private static ExecutionProblem[] vectorToExecutionDelayArray(Vector vector) {
		ExecutionProblem[] delays = new ExecutionDelay[vector.size()];
		vector.copyInto(delays);
		return delays;
	}
	public ExecutionProblem[] delays() {
		return vectorToExecutionDelayArray(delays);
	}
	public ExecutionProblem[] failures() {
		return vectorToExecutionDelayArray(failures);
	}
	
	// If any previous delay is no longer in our status, we have made progress.
	public boolean hasProgressedSince(Status lastStatus) {
		for(int i = 0 ; i < lastStatus.delays.size() ; i ++ ) {
			ExecutionDelay previousDelay = (ExecutionDelay) lastStatus.delays.elementAt(i);
			if(!this.delays.contains(previousDelay))
				return true;
		}
		return false;
	}
}