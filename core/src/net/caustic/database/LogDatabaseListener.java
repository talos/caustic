package net.caustic.database;

import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A {@link DatabaseListener} implementation that logs {@link DatabaseListener} events.
 * @author talos
 *
 */
public class LogDatabaseListener implements DatabaseListener, Loggable {
	
	protected final MultiLog log = new MultiLog();
	
	public void onPut(Scope scope, String key, String value) {
		log.i("Mapped " + StringUtils.quote(key) + ":" +StringUtils.quote(value)
				+ " in " + StringUtils.quote(scope));
	}
	
	public void onNewDefaultScope(Scope scope) {
		log.i("New default scope " + StringUtils.quote(scope));
	}

	public void onNewScope(Scope parent, Scope scope) {
		log.i("New scope " + StringUtils.quote(scope) + " in " + StringUtils.quote(parent));
		
	}

	public void onNewScope(Scope parent, Scope scope, String value) {
		log.i("New scope " + StringUtils.quote(scope) + " in " + StringUtils.quote(parent) +
				" with value " + value);
	}

	public void onAddCookie(Scope scope, String host, String name, String value) {
		log.i("Adding cookie " + StringUtils.quote(name) + "=" + StringUtils.quote(value) +
				" in scope " + StringUtils.quote(scope) + " for host " + StringUtils.quote(host));
		
	}
	/*
	public void onPutReady(Scope scope, String source, String instruction, String uri) {
		log.i("Ready to scrape " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope));
	}*/
	
	public void onPutInstruction(Scope scope, String source,
			String instruction, String uri) {
		log.i("Parsing " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope));
	}
	
	public void onPutLoad(Scope scope, String source, Load load) {
		log.i("Ready to load " + StringUtils.quote(load.serialized) +
				"in scope " + StringUtils.quote(scope));
		
	}

	public void onPutFind(Scope scope, String source, Find find) {
		log.i("Ready to find with " + StringUtils.quote(find.serialized) +
				"in scope " + StringUtils.quote(scope));
		
	}

	public void onPutSuccess(Scope scope, String source, String instruction, String uri) {
		log.i("Successfully scraped  " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope));
		
	}
	public void onPutMissing(Scope scope, String source,
			String instruction, String uri, String[] missingTags) {
		log.i("Instruction " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope) + " is missing " +
				StringUtils.quoteJoin(missingTags, ","));
		
	}

	public void onPutFailed(Scope scope, String source,
			String instruction, String uri, String failedBecause) {
		log.i("Instruction " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope) + " has failed because of " +
				StringUtils.quote(failedBecause));
	}
	
	public void onScopeComplete(Scope scope, int successes, int stuck, int failed) {
		log.i("Scope " + StringUtils.quote(scope) + " is complete." + 
				" There were " + StringUtils.quote(successes) + " successful " +
						" instructions, " + StringUtils.quote(stuck) + " stuck " +
						" instructions, and " + StringUtils.quote(failed) + " failed instructions");
	}
	
	public void register(Logger logger) {
		this.log.register(logger);
	}



}
