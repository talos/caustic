/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.http.Cookies;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;

/**
 * @author talos
 *
 */
public class AndroidRequester implements Loggable {

	private final ExecutorService loadSvc = Executors.newSingleThreadExecutor();
	private final ExecutorService findSvc = Executors.newSingleThreadExecutor();
	
	private final Scraper scraper = new DefaultScraper();
	private final URI rootURL;
	private final Set<String> requestedAddresses = new HashSet<String>();
	
	private final Database db;
	private final MultiLog log = new MultiLog();
	
	public AndroidRequester(String rootURL, Database db) {
		this.rootURL = URI.create(rootURL);
		this.db = db;
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void request(BartlebyAddress address) {
		String id = address.getID().toString();
		
		// only request addresses that have not already been requested
		if(!requestedAddresses.contains(id)) {
			
			// TODO: shouldn't hit sqlite on UI thread
			Map<String, String> addressData = address.getMap();
			for(Map.Entry<String, String> entry : addressData.entrySet()) {
				db.saveTag(id, entry.getKey(), entry.getValue());
			}
			
			request(id, rootURL.resolve(address.getPath()).toString(),
					"", null, true); // immediately force load on these.
		}
	}

	public void request(String id, String instruction, String uri, String input, boolean force) {
		StringMap tags = new CollectionStringMap(db.getData(id, FindDescription.INTERNAL));
		Cookies cookies = db.getCookies(id);
		request(new Request(id, instruction, uri, input, tags, cookies, force));
	}
	
	public void request(Request request) {
		RunnableRequest rRequest = new RunnableRequest(this, scraper, request);
		if(request.force) {
			log.i(StringUtils.quote(request.toString()) + " requested on loadSvc.");
			loadSvc.submit(rRequest); // save laggy loads for this thread.
		} else {
			log.i(StringUtils.quote(request.toString()) + " requested on findSvc.");
			findSvc.submit(rRequest);
		}
	}
	
	public void finished(Request request, Response response) {
		log.i(StringUtils.quote(request.toString()) + " is finished: " + StringUtils.quote(response.serialize()));
		switch(response.getStatus()) {
		case Response.DONE_FIND:
			handleFindResponse(request, (Response.DoneFind) response);
			break;
		case Response.DONE_LOAD:
			handleLoadResponse(request, (Response.DoneLoad) response);
			break;
		case Response.REFERENCE:
			handleReferenceResponse(request, (Response.Reference) response);
			break;
		case Response.WAIT:
			handleWaitResponse(request, (Response.Wait) response);
			break;
		case Response.MISSING_TAGS:
			handleMissingTags(request, (Response.MissingTags) response);
			break;
		case Response.FAILED:
			handleFailure(request, (Response.Failed) response);
			break;
		default:
			throw new RuntimeException("Invalid response: " + response.serialize());
		}
	}
	
	private void handleFindResponse(Request request, Response.DoneFind response) {
		final boolean isBranch = response.getValues().length > 1;
		final FindDescription desc = new FindDescription(response);
		for(String value : response.getValues()) {
			final String id;
			
			// if this is a branch, generate new id, branch cookies, and branch tags
			if(isBranch) {
				id = uuid();
				db.saveRelationship(id, request.id, response.getName(), value, desc);
			} else {
				id = request.id;
			}
			db.saveFind(id, response.getName(), value, desc);
			
			//output.print(id, request.id, response.name, response.values[i]);
			for(String child : response.getChildren()) {
				request(id, child, response.uri, value, false);
			}
		}
		
		// retry stuck instructions that may now be un-stuck.
		List<Request> retry = db.popMissingTags(request.id);
		for(Request retryRequest : retry) {
			request(retryRequest.id, retryRequest.instruction, retryRequest.uri, retryRequest.input, false);
		}
	}
	
	private void handleWaitResponse(Request request, Response.Wait response) {
		db.saveWait(request.id, request.instruction, request.uri, response.getName());
	}
	
	private void handleLoadResponse(Request request, Response.DoneLoad response) {
		db.saveCookies(request.id, response.getCookies());
		for(String child : response.getChildren()) {
			request(request.id, child, response.uri, response.getContent(), false);
		}		
	}
	
	private void handleReferenceResponse(Request request, Response.Reference response) {
		for(String child : response.getReferenced()) {
			// follow the response's uri, but keep everything else the same as request.
			request(request.id, child, response.uri, request.input, request.force);
		}
	}
	
	private void handleMissingTags(Request request, Response.MissingTags response) {
		db.saveMissingTags(request.id, request.instruction, request.uri, request.input, response.getMissingTags());
	}
	
	private void handleFailure(Request request, Response.Failed response) { }

	public void interrupt(Throwable why) {
		log.e(why);
		//loadSvc.shutdownNow();
		//findSvc.shutdownNow();
	}

	/**
	 * 
	 * @return A {@link String} UUID.
	 */
	private String uuid() {
		return UUID.randomUUID().toString();
	}
}
