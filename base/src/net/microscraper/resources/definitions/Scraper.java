package net.microscraper.resources.definitions;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;
import net.microscraper.resources.Result;
import net.microscraper.resources.Status;

public class Scraper {
	private final Link[] links;
	public Scraper(Link[] links) {
		this.links = links;
	}
	private Status lastStatus = new Status();
	public Status execute(ExecutionContext context) throws ExecutionFatality {
		Vector linksVector = new Vector();
		Utils.arrayIntoVector(links, linksVector);
		Status status = new Status();
		for(int i = 0 ; i < linksVector.size() ; i ++) {
			Link link = (Link) linksVector.elementAt(i);
			try {
				Result[] results = link.execute(context);
				for(int j = 0 ; j < results.length ; j ++) {
					status.addSuccess(results[j]);
				}
			} catch (ExecutionDelay e) {
				linksVector.removeElementAt(i);
				i--;
				linksVector.add(link);
				status.addDelay(e);
			} catch (ExecutionFailure e) {
				linksVector.removeElementAt(i);
				i--;
				status.addFailure(e);
			}
		}
		if(status.hasProgressedSince(lastStatus) && status.hasDelay()) {
			return execute(context); // loop back.
		} else {
			return status;
		}
	}
}