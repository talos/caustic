package net.microscraper.database;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;

public abstract class Status {
	private Vector successes = new Vector();
	private Vector delays = new Vector();
	private Vector failures = new Vector();
	
	public String toString() {
		return getClass().getName();
	}
	public abstract String getResult();
	/*
	public static final class NotYetStarted extends Status {
		public String getResult() {
			return null;
		}
	}
	public static final class Successful extends Status {
		private final String result;
		public Successful(String result) {
			this.result = result;
		}
		public String getResult() {
			return result;
		}
	}
	public static final class Delayed extends Status {
		Vector delays = new Vector();
		public Delayed(ExecutionDelay e) {
			delays.addElement(e);
		}
		public String getResult() {
		}
	}
	public static final class Failed extends Status {
		Vector throwables = new Vector();
		public Failed(ExecutionFailure e) {
			throwables.addElement(e);
		}
		public String getResult() {
			return null;
		}
	}*/
}