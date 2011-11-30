package net.caustic;

import net.caustic.database.LogDatabaseListener;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A {@link ScraperListener} that logs all events.  Subclass and make sure to call
 * <code>super()</code> to get logging for all {@link ScraperListener} events.
 * @author talos
 *
 */
public class LogScraperListener extends LogDatabaseListener implements ScraperListener {

	public void onCrash(Scope scope, Instruction instruction, Throwable e) {
		log.i("Crashed on " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) +
				" because of " + StringUtils.quote(e.toString()));
	}
	
	public void onPause(Scope scope, Instruction instruction, Resume resume) {
		log.i("Pausing " + StringUtils.quote(instruction) + " in scope " + StringUtils.quote(scope) + 
				" for later.");
	}

}
