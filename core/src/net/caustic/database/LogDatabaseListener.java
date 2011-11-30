package net.caustic.database;

import net.caustic.instruction.Instruction;
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
		log.i("New scope " + StringUtils.quote(scope) + " in " + StringUtils.quote(parent) + " with " +
			" value " + value);
	}

	public void onAddCookie(Scope scope, String host, String name, String value) {
		log.i("Adding cookie " + StringUtils.quote(name) + "=" + StringUtils.quote(value) + " in scope " +
				StringUtils.quote(scope) + " for host " + StringUtils.quote(host));
		
	}
	
	public void onPutReady(Scope scope, String source, Instruction instruction) {
		log.i("Ready to scrape " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope));
	}

	public void onPutMissing(Scope scope, String source,
			Instruction instruction, String[] missingTags) {
		log.i("Instruction " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope) + " is missing " +
				StringUtils.quoteJoin(missingTags, ","));
		
	}

	public void onPutFailed(Scope scope, String source,
			Instruction instruction, String failedBecause) {
		log.i("Instruction " + StringUtils.quote(instruction) +
				"in scope " + StringUtils.quote(scope) + " has failed because of " +
				StringUtils.quote(failedBecause));
		
	}
	
	public void register(Logger logger) {
		this.log.register(logger);
	}
}
