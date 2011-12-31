/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import net.caustic.Request;
import net.caustic.Scraper;
import net.caustic.android.AndroidRequester;
class RunnableRequest implements Runnable {
	
	private final AndroidRequester requester;
	private final Scraper scraper;
	private final Request request;
	
	RunnableRequest(AndroidRequester requester, Scraper scraper, Request request) {
		this.requester = requester;
		this.scraper = scraper;
		this.request = request;
	}
	
	public void run() {
		try {
			requester.finished(request, scraper.scrape(request));
		} catch(InterruptedException e) {
			requester.interrupt(e);
		} catch(Throwable e) {
			requester.interrupt(e);
		}
	}
}
