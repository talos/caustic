package net.caustic;

import net.caustic.database.Database;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * Implement the {@link ScraperListener} interface to access
 * data and execution information as it's happening from {@link AbstractScraper}.
 * @author talos
 *
 */
public interface ScraperListener {

	/**
	 * This fires when an <code>instruction</code> is frozen.
	 * @param instruction The {@link Instruction} that is being frozen.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 */
	public abstract void onFreeze(Instruction instruction, Scope scope, String source);
	
	/**
	 * This fires when an <code>instruction</code> first starts to be scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 */
	public abstract void onScrape(Instruction instruction, Scope scope, String source);
	
	/**
	 * This fires when a single <code>instruction</code> is successfully scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param source The {@link String} source for the scrape.  This is <code>null</code>
	 * if <code>instruction</code> is a {@link Load}.
	 */
	public abstract void onSuccess(Instruction instruction, Scope scope, String source);
	
	/**
	 * This fires when an <code>instruction</code> cannot currently be scraped
	 * because some tags cannot be substituted.  It will be tried again if
	 * <code>missingTags</code> appear in <code>scope</code>.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param missingTags An array of {@link String}s that are missing tags.
	 */
	public abstract void onMissingTags(Instruction instruction, Scope scope,
			String source, String[] missingTags);
	
	/**
	 * This fires when an <code>instruction</code> cannot be scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param failedBecause A {@link String} explaining why this <code>instruction</code>
	 * cannot be scraped.
	 */
	public abstract void onFailed(Instruction instruction, Scope scope, String source,
			String failedBecause);
	
	/**
	 *
	 * @param successful
	 * @param stuck
	 * @param failed
	 */
	public abstract void onFinish(int successful, int stuck, int failed);
	
	/**
	 * 
	 * @param instruction
	 * @param scope
	 * @param parent
	 * @param source
	 * @param e
	 */
	public abstract void onCrashed(Instruction instruction, Scope scope, String source, Throwable e);
}
