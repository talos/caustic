package net.caustic;

import net.caustic.database.DatabaseListener;
import net.caustic.scope.Scope;

/**
 * Implement the {@link ScraperListener} interface to access
 * data and execution information as it's happening from {@link AbstractScraper}.
 * @author talos
 *
 */
public interface ScraperListener extends DatabaseListener {

	/**
	 * This fires when an <code>instruction</code> is paused.
	 * @param scope The {@link Scope} of the paused instruction.
	 * @param instruction The {@link String} serialization of the instruction that was paused.
	 * @param uri The {@link String} location of <code>instruction</code>'s location.
	 * @param resume A {@link Resume} that, when {@link Resume#run()}, will resume the instruction.
	 */
	public abstract void onPause(Scope scope, String instruction, String uri, Resume resume);
		
	/**
	 * 
	 * @param instruction
	 * @param scope
	 * @param parent
	 * @param source
	 * @param e
	 */
	public abstract void onCrash(Scope scope, String instruction, String uri, Throwable e);
}
