package net.caustic;

import net.caustic.database.Database;
import net.caustic.http.HttpBrowser;
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
	 * This fires when an <code>instruction</code> first starts to be scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param name The {@link String} name of the <code>instruction</code> to be scraped.
	 * @param db The {@link Database} that <code>instruction</code> is working from.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param parent The {@link Scope} of the parent of this scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param browser The {@link HttpBrowser} that is being used for this scrape.
	 * @param start A {@link Runnable} whose {@link Runnable#run()} method will begin
	 * the scraping.  If the scraping has already started, {@link Runnable#run()} is
	 * a no-op.
	 */
	public abstract void onReady(Instruction instruction, String name, Database db, Scope scope,
			Scope parent, String source, HttpBrowser browser, Runnable start);
	
	/**
	 * This fires when an <code>instruction</code> first starts to be scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param db The {@link Database} that <code>instruction</code> is working from.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param parent The {@link Scope} of the parent of this scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param browser The {@link HttpBrowser} that is being used for this scrape.
	 */
	public abstract void onScrape(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, HttpBrowser browser);
	
	/**
	 * This fires when a single <code>instruction</code> is successfully scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param db The {@link Database} that <code>instruction</code> is working from.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param parent The {@link Scope} of the parent of this scrape.
	 * @param source The {@link String} source for the scrape.  This is <code>null</code>
	 * if <code>instruction</code> is a {@link Load}.
	 * @param key The {@link String} key for this <code>instruction</code>'s
	 * results.  Blank if there was none.
	 * @param results An array of {@link String} results for <code>instruction</code>
	 */
	public abstract void onSuccess(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, String key, String[] results);
	
	/**
	 * This fires when an <code>instruction</code> cannot currently be scraped
	 * because some tags cannot be substituted.  It will be tried again if
	 * <code>missingTags</code> appear in <code>scope</code>.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param db The {@link Database} that <code>instruction</code> is working from.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param parent The {@link Scope} of the parent of this scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param missingTags An array of {@link String}s that are missing tags.
	 */
	public abstract void onMissingTags(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, HttpBrowser browser, String[] missingTags);
	
	/**
	 * This fires when an <code>instruction</code> cannot be scraped.
	 * @param instruction The {@link Instruction} that is being scraped.
	 * @param db The {@link Database} that <code>instruction</code> is working from.
	 * @param scope The {@link Scope} of this particular scrape.
	 * @param parent The {@link Scope} of the parent of this scrape.
	 * @param source A {@link String} source that <code>instruction</code> is
	 * working from.  This is <code>null</code> if <code>instruction</code>
	 * is a {@link Load}.
	 * @param failedBecause A {@link String} explaining why this <code>instruction</code>
	 * cannot be scraped.
	 */
	public abstract void onFailed(Instruction instruction, Database db, Scope scope,
			Scope parent, String source, String failedBecause);
	
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
	public abstract void onCrashed(Instruction instruction, Scope scope,
			Scope parent, String source, Throwable e);
}
