package net.caustic;

import net.caustic.database.Database;
import net.caustic.instruction.Instruction;
import net.caustic.log.Logger;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A {@link ScraperListener} that logs all events.  Subclass and make sure to call
 * <code>super()</code> to get logging for all {@link ScraperListener} events.
 * @author talos
 *
 */
public class LogScraperListener implements ScraperListener {

	private final Logger log;
	public LogScraperListener(Logger log) {
		this.log = log;
	}

	public void onReady(Instruction instruction, String name, Database db, Scope scope,
			Scope parent, String source, Runnable start) {
		log.i("Ready to scrape " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
	}

	public void onScrape(Instruction instruction, Database db, Scope scope,
			Scope parent, String source) {
		log.i("Scraping " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
		
	}
	
	public void onSuccess(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, String key, String[] results) {
		log.i("Finished " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope));
	}
	
	public void onMissingTags(Instruction instruction, Database db,
			Scope scope, Scope parent, String source,
			String[] missingTags) {
		log.i("Stuck on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because missing tags " + StringUtils.quoteJoin(missingTags, ", "));

	}

	public void onFailed(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, String failedBecause) {
		log.i("Failed on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because of " + StringUtils.quote(failedBecause));

	}

	public void onFinish(int successful, int stuck, int failed) {
		log.i("Finished scraping, there were " + successful + " executions, " + stuck + " that " +
				"were missing tags, and " + failed + " failures.");
	}

	public void onCrashed(Instruction instruction, Scope scope, Scope parent,
			String source, Throwable e) {
		log.i("Crashed on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because of " + StringUtils.quote(e.toString()));

	}

}
