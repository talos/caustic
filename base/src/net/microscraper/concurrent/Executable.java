package net.microscraper.concurrent;

import net.microscraper.client.Scraper;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.template.StringSubstitution;
import net.microscraper.util.Result;
import net.microscraper.util.StringUtils;

/**
 * {@link Executable} binds an {@link Instruction} to a {@link String}
 * source, {@link DatabaseView}, and {@link HttpBrowser}, 
 * allowing it to be tried and retried.
 * A successful {@link #execute()} supplies children {@link Executable}s
 * that should be tried next.
 * @author realest
 *
 */
public final class Executable implements Result {

	private final Instruction instruction;
	private final DatabaseView view;
	private HttpBrowser browser;
	private String source;
	
	private InstructionResult result;
	private InstructionResult lastResult;

	/**
	 * Determine whether this {@link Scraper} is stuck.
	 * @return <code>true</code> if, in two consecutive runs, this {@link Scraper}
	 * has generated a {@link ScraperResult} missing identical sets of tags.
	 */
	private boolean isStuck() {
		if(result != null && lastResult != null) {
			return StringSubstitution.isMissingSameTags(result, lastResult);
		}
		return false;
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
	 * this {@link Scraper} executes, in addition to the state of its {@link DatabaseView}
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
		lastResult = result;
		
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
	static boolean allAreStuck(Executable[] executables) {
		for(int i = 0 ; i < executables.length ; i++) {
			if(!executables[i].isStuck()) {
				return false;
			}
		}
		return true;
	}
}
