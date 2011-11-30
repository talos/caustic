package net.caustic;

import net.caustic.database.DatabaseListener;
import net.caustic.instruction.Instruction;
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
	 * @param instruction The {@link Instruction} that was paused.
	 * @param resume A {@link Resume} that, when {@link Resume#run()}, will resume the instruction.
	 */
	public abstract void onPause(Scope scope, Instruction instruction, Resume resume);
		
	/**
	 * 
	 * @param instruction
	 * @param scope
	 * @param parent
	 * @param source
	 * @param e
	 */
	public abstract void onCrash(Scope scope, Instruction instruction, Throwable e);
}
