package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Stringer;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Publisher;
import net.microscraper.database.Execution;
import net.microscraper.database.Status;

public class ThreadSafeJSONPublisher implements Publisher {
	private int loc = 0;
	private final List<Execution> executions = Collections.synchronizedList(new ArrayList<Execution>());

	public boolean live() {
		return true;
	}
	public void publish(Execution execution) throws PublisherException {
		Client.log.i("publishing");
		//results.add(result.ref, result);
		executions.add(execution);
	}
	
	public int size() {
		return executions.size();
	}
	
	public String get(int executionNumber) throws PublisherException {
		try {
			Execution exc = (Execution) executions.get(executionNumber);
			Status status = exc.getStatus();
			Stringer stringer = Client.json.getStringer();
			stringer.object()
				.key(ID).value(exc.id)
				.key(SOURCE_ID).value(exc.getSourceExecution().id)
				.key(STATUS_STRING).value(status.toString());
			
			if(status.isSuccessful()) {
				Status.Successful successful = (Status.Successful) status;
				stringer.key(VALUE).value(successful.getResult());
			} else if (status.isInProgress()) {
				Status.InProgress inProgress = (Status.InProgress) status;
				if(inProgress.isMissingVariables()) {
					stringer.key(MISSING_VARIABLES).array();
					MissingVariable[] missingVariables = inProgress.getMissingVariables();
					for(int i = 0 ; i < missingVariables.length ; i ++) {
						stringer.value(missingVariables[i].missing_tag);
					}
					stringer.endArray();
				}
			} else if (status.isFailure()) {
				Status.Failure failure = (Status.Failure) status;
				stringer.key(ERRORS).array();
				Throwable[] throwables = failure.getThrowables();
				for(int i = 0 ; i < throwables.length ; i ++) {
					stringer.value(throwables[i].getMessage());
				}
				stringer.endArray();
			}
			stringer.endObject();
			return stringer.toString();
		} catch(JSONInterfaceException e) {
			throw new PublisherException(e);
		}
	}
	
	public String next() throws PublisherException {
		if(size() > loc) {
			loc++;
			return get(loc - 1);
		} else {
			loc = 0;
			return null;
		}
	}
}
