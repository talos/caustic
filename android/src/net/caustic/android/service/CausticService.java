package net.caustic.android.service;

import java.io.FileDescriptor;
import java.util.List;
import java.util.UUID;

import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.android.service.CausticIntent.CausticRefreshIntent;
import net.caustic.android.service.CausticIntent.CausticRequestIntent;
import net.caustic.http.Cookies;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringMap;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public class CausticService extends IntentService {
	
	private static final String REQUEST = "request";
	private static String NAME = "CAUSTIC_SERVICE";
	
	private Scraper scraper;
	private Database db;
	
	public CausticService() {
		super(NAME);
	}
	
	@Override
	public void onCreate() {
		this.db = new Database(getBaseContext()); // TODO ?
		this.scraper = new DefaultScraper();
	}
	
	@Override
	public void onDestroy() {
		this.db.close();
		this.scraper = null;
	}

	@Override
	protected void onHandleIntent(Intent rawIntent) {
		try {
			if(rawIntent.getAction().equals(CausticIntent.ACTION_REQUEST)) {
				CausticRequestIntent intent = new CausticRequestIntent(rawIntent);
				request(intent.getID(), intent.getURI(), intent.getInstruction(), null, intent.getForce());
			}
		} catch(Throwable e) {
			e.printStackTrace(); // TODO
		}
	}
	
	private void request(String id, String uri, String instruction, String input, boolean force)
			throws InterruptedException {
		StringMap tags = new CollectionStringMap(db.getData(id, FindDescription.INTERNAL));
		Cookies cookies = db.getCookies(id);
		Request request = new Request(id, instruction, uri, input, tags, cookies, force);
		Response response = scraper.scrape(request);
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
	
		// TODO best location for this?
		sendBroadcast(id);
	}
	
	private void handleFindResponse(Request request, Response.DoneFind response) throws InterruptedException {
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
	
	private void handleLoadResponse(Request request, Response.DoneLoad response) throws InterruptedException {
		db.saveCookies(request.id, response.getCookies());
		for(String child : response.getChildren()) {
			request(request.id, child, response.uri, response.getContent(), false);
		}		
	}
	
	private void handleReferenceResponse(Request request, Response.Reference response) throws InterruptedException {
		for(String child : response.getReferenced()) {
			// follow the response's uri, but keep everything else the same as request.
			request(request.id, child, response.uri, request.input, request.force);
		}
	}
	
	private void handleMissingTags(Request request, Response.MissingTags response) throws InterruptedException {
		db.saveMissingTags(request.id, request.instruction, request.uri, request.input,
				response.getMissingTags());
	}
	
	private void handleFailure(Request request, Response.Failed response) { }

	private void interrupt(Throwable why) {
		why.printStackTrace();
		//log.e(why);
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
	
	private void sendBroadcast(String id) {
		sendBroadcast(CausticRefreshIntent.newResponse(id,
				db.getData(id, FindDescription.EXTERNAL), 
				db.getWait(id),
				db.getChildren(id)));

	}
}
