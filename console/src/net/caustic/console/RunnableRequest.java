package net.caustic.console;

import java.util.Arrays;
import java.util.List;

import net.caustic.Request;
import net.caustic.Response;
import net.caustic.util.CollectionStringMap;

class RunnableRequest implements Runnable {
	/**
	 * 
	 */
	private final Requester requester;
	private final CollectionStringMap tags;
	
	private List<String> lastMissingTags;
	
	public final Request request;

	RunnableRequest(Requester requester, String id, String instruction, String uri, String input,
			CollectionStringMap tags, String[] cookies, boolean force) {
		this.requester = requester;
		this.tags = tags;
		this.request = new Request(id, instruction, uri, input, tags, cookies, force);
	}
	
	/**
	 * Construct a duplicate RunnableRequest with force enabled.
	 * @param req
	 */
	private RunnableRequest(RunnableRequest req) {
		this.requester = req.requester;
		this.tags = req.tags;
		this.request = new Request(req.request.id, req.request.instruction, req.request.uri, req.request.input, req.request.tags,
				req.request.cookies, true);
	}

	public void run() {
		try {
			Response response = this.requester.scraper.scrape(request);
			if(response.children != null) {
				this.requester.finished(request, tags, response);
			} else if (response.wait) {
				this.requester.queue(new RunnableRequest(this));
			} else if (response.missingTags != null) {
				if(lastMissingTags != null) {
					if(lastMissingTags.containsAll(Arrays.asList(response.missingTags))) {
						this.requester.stuck(request, response);
					} else {
						retry(response);
					}
				} else {
					retry(response);
				}
			} else {
				this.requester.failed(request, response);
			}
		} catch(InterruptedException e) {
			this.requester.interrupt(e);
		} catch(Throwable e) {
			this.requester.interrupt(e);
		}
	}
	
	private void retry(Response response) {
		this.lastMissingTags = Arrays.asList(response.missingTags);
		this.requester.queue(this);
	}
}