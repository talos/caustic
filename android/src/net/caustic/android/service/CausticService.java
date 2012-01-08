package net.caustic.android.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.android.service.CausticServiceIntent.CausticForceIntent;
import net.caustic.android.service.CausticServiceIntent.CausticRefreshIntent;
import net.caustic.android.service.CausticServiceIntent.CausticRequestIntent;
import net.caustic.android.service.CausticServiceIntent.CausticResponseIntent;
import net.caustic.http.Cookies;
import net.caustic.log.AndroidLogger;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class CausticService extends IntentService {
	
	private static String NAME = "CAUSTIC_SERVICE";
	
	private AndroidLogger logger;
	private Scraper scraper;
	private Database db;
	
	public CausticService() {
		super(NAME);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.logger = new AndroidLogger(this);
		this.db = new Database(this); // TODO ?
		this.scraper = new DefaultScraper();
	}
	
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
	}*/
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.db.close();
		this.scraper = null;
	}

	@Override
	protected void onHandleIntent(Intent rawIntent) {
		try {						
			String action = rawIntent.getAction();
			if(action.equals(CausticServiceIntent.REQUEST_INTENT)) {
				CausticRequestIntent intent = new CausticRequestIntent(rawIntent);
				
				db.saveRelationship(null, intent.getId(), intent.getInstruction());
				
				Map<String, String> tags = intent.getTags();
				for(Map.Entry<String, String> entry : tags.entrySet()) {
					db.saveTag(intent.getScope(), entry.getKey(), entry.getValue());
				}
				
				request(new Request(intent.getScope(), intent.getInstruction(),
						intent.getURI(), null, db.getTags(intent.getScope()), db.getCookies(intent.getScope()),
						intent.getForce()));
				
				db.saveRequestRoot(intent.get, instruction)
				
				sendBroadcast(intent.getScope());
			} else if(action.equals(CausticServiceIntent.REFRESH_INTENT)) {
				CausticRefreshIntent intent = new CausticRefreshIntent(rawIntent);
				
				sendBroadcast(intent.getScope());
			} else if(action.equals(CausticServiceIntent.FORCE_INTENT)) {
				CausticForceIntent intent = new CausticForceIntent(rawIntent);
				Request request = db.getWaitByID(intent.getId());
				request(request);
				
				
				
				sendBroadcast(request.id);
			}
			
		} catch(Throwable e) {
			e.printStackTrace(); // TODO
		}
	}
	
	private void request(Request request)
			throws InterruptedException {
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
	}
	
	private void handleFindResponse(Request request, Response.DoneFind response) throws InterruptedException {
		final boolean isBranch = response.getValues().length > 1;
		final FindDescription desc = new FindDescription(response);
		for(String value : response.getValues()) {
			final String id;
			
			// if this is a branch, generate new id, branch cookies, and branch tags
			if(isBranch) {
				id = db.saveRelationship(request.id, response.getName(), value, desc);
			} else {
				id = request.id;
			}
			db.saveFind(id, response.getName(), value, desc);
			
			StringMap tags = null;
			Cookies cookies = null;
			for(String child : response.getChildren()) {
				// only hit the DB for cookies/tags if we haven't yet for this ID.
				tags    = tags    == null ? db.getTags(id) : tags;
				cookies = cookies == null ? db.getCookies(id) : cookies;
				request(new Request(id, child, response.uri, value, tags,
						cookies, false));
			}
		}
		
		// retry stuck instructions that may now be un-stuck.
		List<Request> retry = db.popMissingTags(request.id);
		for(Request retryRequest : retry) {
			StringMap tags = db.getTags(retryRequest.id);
			Cookies cookies = db.getCookies(retryRequest.id);
			request(new Request(retryRequest.id, retryRequest.instruction,
					retryRequest.uri, retryRequest.input, tags, cookies, false));
		}
	}
	
	private void handleWaitResponse(Request request, Response.Wait response) {
		db.saveWait(request.id, request.instruction, request.uri, response.getName());
	}
	
	private void handleLoadResponse(Request request, Response.DoneLoad response) throws InterruptedException {
		db.saveCookies(request.id, response.getCookies());
		Cookies cookies = db.getCookies(request.id); // reload cookies, these may have changed.  Tags could not have.
		for(String child : response.getChildren()) {
			request(new Request(request.id,child, response.uri, response.getContent(), request.tags, cookies, false));
		}		
	}
	
	private void handleReferenceResponse(Request request, Response.Reference response) throws InterruptedException {
		for(String child : response.getReferenced()) {
			// follow the response's uri, but keep everything else the same as request.
			request(new Request(
					request.id, child, response.uri, request.input, request.tags, request.cookies, request.force));
		}
	}
	
	private void handleMissingTags(Request request, Response.MissingTags response) throws InterruptedException {
		db.saveMissingTags(request.id, request.instruction, request.uri, request.input,
				response.getMissingTags());
	}
	
	private void handleFailure(Request request, Response.Failed response) {
		logger.i("Request " + request.toString() + " failed: " + StringUtils.quote(response.getReason()));
	}

	private void broadcastId(String scope) {
		
	}
	
	private void broadcastData(String scope) {
		
	}
	
	/*private void sendBroadcast(String scope) {
		Intent responseIntent = CausticResponseIntent.newResponse(scope,
				db.getData(scope, FindDescription.EXTERNAL), 
				db.getWaitsInScope(scope),
				db.getChildren(scope));
		Log.i("caustic-service", "broadcasting response intent: " + responseIntent.getAction());
		Log.i("caustic-service", db.getData(scope, FindDescription.EXTERNAL).toString());
		Log.i(NAME, db.getChildren(scope).toString());
		sendBroadcast(responseIntent);
	}*/
}
