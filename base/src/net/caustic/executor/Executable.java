package net.caustic.executor;

import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.util.Result;
import net.caustic.util.StringUtils;

/**
 * {@link Executable} binds an {@link Instruction} to a {@link String}
 * source, {@link DatabaseView}, and {@link HttpBrowser}, 
 * allowing it to be tried and retried.
 * A successful {@link #execute()} supplies children {@link Executable}s
 * that should be tried next.
 * @author realest
 *
 */
final class Executable implements Result {

	private final Instruction instruction;
	private final DatabaseView view;
	private HttpBrowser browser;
	private String source;
	
	private InstructionResult result;

	/**
	 * Determine whether this {@link CoreScraper} is stuck.
	 * @return <code>true</code> if the {@link Executable}'s {@link DatabaseView}
	 * is still missing the tags it was last time it ran.
	 */
	public boolean isStuck() throws DatabaseReadException {
		boolean isStuck = false;
		if(result != null) { // only test if we've run before
			String[] missingTags = getMissingTags();
			for(int i = 0 ; i < missingTags.length ; i ++) {
				if(view.get(missingTags[i]) == null) { // still missing a tag
					isStuck = true;
				}
			}
		}
		return isStuck;
	}
	
	public Executable(Instruction instruction, DatabaseView view, String source, 
			HttpBrowser browser) {
		this.instruction = instruction;
		this.source = source;
		this.view = view;
		this.browser = browser;
	}
	
	private Executable[] generateChildren() throws DatabaseException {
		final String name = result.getName();
		final String[] results = result.getResults();
		final boolean shouldStoreValues = result.shouldStoreValues();
		final Instruction[] childInstructions = result.getChildren();
		
		// create children.
		Executable[] children = new Executable[childInstructions.length * results.length];
		for(int i = 0 ; i < results.length ; i ++) {
			final DatabaseView childView;
			// generate result views.
			final String resultValue = results[i];
			if(results.length == 1) { // don't spawn a new result for single match
				childView = view;
				if(shouldStoreValues) {
					childView.put(name, resultValue);
				}
			} else {
				if(shouldStoreValues) {
					childView = view.spawnChild(name, resultValue);
				} else {
					childView = view.spawnChild(name);							
				}
			}
			for(int j = 0 ; j < childInstructions.length ; j ++) {
				children[i * childInstructions.length + j] =
					new Executable(childInstructions[j], childView, results[i],
						browser.copy());
			}
		}
		return children;
	}
	
	/**
	 * Returns a {@link String} containing information about the {@link Instruction}
	 * this {@link CoreScraper} executes, in addition to the state of its {@link DatabaseView}
	 * and source, if any.
	 */
	public String toString() {
		return  StringUtils.simpleClassName(instruction) + " " +
				StringUtils.quote(instruction.toString()) + " with tags substituted from " +
				StringUtils.quote(view.toString());
	}
	
	/**
	 * 
	 * @return An array of {@link Executable}s that can execute this {@link Executable}s
	 * children, or <code>null</code> if this was not a successful execution.
	 * @throws DatabaseException
	 * @throws InterruptedException
	 */
	public Executable[] execute() throws DatabaseException, InterruptedException {		
		// attempt to scrape
		if(result == null) {
			result = instruction.execute(source, view, browser);
		} else if(result.isMissingTags()) { // retry if missing tags
			result = instruction.execute(source, view, browser);
		} else { // either previous success or failure.
			throw new IllegalStateException("Should not re-execute this executable.");
		}
		
		if(result.isSuccess()) {
			return generateChildren();
		} else {
			return null;
		}
	}
	
	public boolean isMissingTags() {
		return result.isMissingTags();
	}

	public String[] getMissingTags() {
		return result.getMissingTags();
	}
	
	public String getFailedBecause() {
		return result.getFailedBecause();
	}
	
	/**
	 * 
	 * @param executables An array of {@link Executable}s to check.
	 * @return <code>true</code> if all <code>executables</code> are stuck,
	 * <code>false</code> otherwise.
	 */
	static boolean allAreStuck(Executable[] executables) throws DatabaseReadException {
		for(int i = 0 ; i < executables.length ; i++) {
			if(!executables[i].isStuck()) {
				return false;
			}
		}
		return true;
	}
}
