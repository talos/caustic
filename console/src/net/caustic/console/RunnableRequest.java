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
			switch(response.getStatus()) {
			case Response.DONE:
				requester.finished(this, (Response.Done) response);
				break;
			case Response.WAIT:
				requester.loadQueue(this, (Response.Wait) response);
				break;
			case Response.MISSING_TAGS:
				Response.MissingTags missingTags = (Response.MissingTags) response;
				if(lastMissingTags != null) {
					if(lastMissingTags.containsAll(Arrays.asList(missingTags.getMissingTags()))) {
						requester.stuck(this, missingTags);
					} else {
						retry(missingTags);
					}
				} else {
					retry(missingTags);
				}
				break;
			case Response.FAILED:
				this.requester.failed(this, (Response.Failed) response);
				break;
			default:
				throw new RuntimeException("Invalid response: " + response.serialize());
			}
		} catch(InterruptedException e) {
			requester.interrupt(e);
		} catch(Throwable e) {
			requester.interrupt(e);
		}
	}
	
	private void retry(Response.MissingTags response) {
		this.lastMissingTags = Arrays.asList(response.getMissingTags());
		this.requester.missingTagsQueue(this, response);
	}
}