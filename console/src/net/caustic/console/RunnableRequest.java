package net.caustic.console;

import java.util.Arrays;
import java.util.List;

import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.http.HashtableCookies;
import net.caustic.util.CollectionStringMap;

public class RunnableRequest implements Runnable {
	/**
	 * 
	 */
	private final Requester requester;
	private final Scraper scraper;
	private List<String> lastMissingTags;
	
	public final CollectionStringMap tags;
	public final Request request;
	public final HashtableCookies cookies;
	
	public RunnableRequest(Requester requester,
			Scraper scraper, String id, String instruction, String uri, String input,
			CollectionStringMap tags, HashtableCookies cookies, boolean force) {
		this.scraper = scraper;
		this.requester = requester;
		this.tags = tags;
		this.cookies = cookies;
		this.request = new Request(id, instruction, uri, input, tags, cookies, force);
	}
	
	public void run() {
		try {
			Response response = scraper.scrape(request);
						
			if(response.children != null) {
				requester.finished(this, response);
			} else if (response.wait) {
				requester.loadQueue(this, response);
			} else if (response.missingTags != null) {
				if(lastMissingTags != null) {
					if(lastMissingTags.containsAll(Arrays.asList(response.missingTags))) {
						requester.stuck(this, response);
					} else {
						retry(response);
					}
				} else {
					retry(response);
				}
			} else {
				this.requester.failed(this, response);
			}
		} catch(InterruptedException e) {
			requester.interrupt(e);
		} catch(Throwable e) {
			requester.interrupt(e);
		}
	}
	
	private void retry(Response response) {
		this.lastMissingTags = Arrays.asList(response.missingTags);
		this.requester.missingTagsQueue(this, response);
	}
}