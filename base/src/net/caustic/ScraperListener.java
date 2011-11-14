package net.caustic;

import net.caustic.database.DatabaseListener;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * Implement the {@link ScraperListener} interface to access
 * data and execution information as it's happening from {@link ScraperInterface}.
 * @author talos
 *
 */
public interface ScraperListener extends DatabaseListener {

	public abstract void scrape(Instruction instruction, Scope scope,
			String source, HttpBrowser browser);
	public abstract void success(Instruction instruction, Scope scope,
			String source, HttpBrowser browser);
	public abstract void missing(Instruction instruction, Scope scope,
			String source, HttpBrowser browser, String[] missingTags);
	public abstract void failed(Instruction instruction, Scope scope,
			String source, String failedBecause);
	
	public abstract void terminated(int successful, int missing, int failed);
	public abstract void crashed(Instruction instruction, Scope scope,
			String source, Throwable e);
}
