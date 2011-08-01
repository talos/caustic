package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.Client;
import net.microscraper.executable.SpawnedScraperExecutable;
import net.microscraper.executable.Status;
import net.microscraper.executable.SpawnedScraperExecutable.ExecutionProblem;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.json.JSONInterface;

public class ThreadSafeJSONPublisher implements Database {
	private final List<JSONInterfaceStringer> executions = Collections.synchronizedList(new ArrayList<JSONInterfaceStringer>());
	private Integer pos = -1;
	private final JSONInterface json;
	public ThreadSafeJSONPublisher(JSONInterface json) {
		this.json = json;
	}
	private static void appendProblemToJSON(ExecutionProblem problem, JSONInterfaceWriter writer) throws JSONInterfaceException {
		writer.object()
			.key(SOURCE_RESULT_ID)
			.value(problem.callerExecution().id)
			.key(TYPE)
			.value(problem.problemClass().getSimpleName())
			.key(VALUE)
			.value(problem.reason())
			.endObject();
	}
	
	@Override
	public void publish(SpawnedScraperExecutable execution, Status status) throws DatabaseException {
		try {
			JSONInterfaceStringer stringer = json.getStringer();
			stringer.object()
				.key(ID).value(execution.id)
				.key(SOURCE_RESULT_ID).value(execution.getSourceExecution().id)
				.key(NAME).value(execution.getName());
			
			String[] successes = status.successes();
			stringer.key(SUCCESS).array();
			for(int i = 0 ; i < successes.length ; i ++) {
				stringer.value(successes[i]);
			}
			stringer.endArray();
			
			ExecutionProblem[] delays = status.delays();
			stringer.key(DELAY).array();
			for(int i = 0 ; i < delays.length ; i ++) {
				appendProblemToJSON(delays[i], stringer);
			}
			stringer.endArray();
			
			ExecutionProblem[] failures = status.failures();
			stringer.key(FAILURE).array();
			for(int i = 0 ; i < failures.length ; i ++) {
				appendProblemToJSON(failures[i], stringer);
			}
			stringer.endArray();
			
			stringer.endObject();
			synchronized(executions) {
				executions.add(stringer);
			}
		} catch(JSONInterfaceException e) {
			throw new DatabaseException(e);
		}
	}
	public void resetIterator() {
		synchronized(pos) {
			pos = -1;
		}
	}
	public boolean hasNext() {
		synchronized(executions) {
			synchronized(pos) {
				return executions.size() > pos + 1;
			}
		}
	}
	public JSONInterfaceStringer next() {
		synchronized(executions) {
			synchronized(pos) {
				pos++;
				return executions.get(pos);
			}
		}
	}
}
