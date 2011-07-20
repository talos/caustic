package net.microscraper.executable;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Regexp;
import net.microscraper.interfaces.regexp.PatternInterface;

/**
 * {@link FindExecutable}s are the {@link BasicExecutable} implementation of {@link Find}s, and are contained inside
 * {@link Scraper}s but link to a {@link Regexp} resource.  If one-to-many,
 * they are subclassed as {@link FindManyExecutable}; if one-to-one, they are
 * subclassed as {@link FindOneExecutable}.
 * @see {@link FindOneExecutable}
 * @see {@link FindManyExecutable}
 * @see {@link BasicExecutable}
 * @see {@link Executable}
 * @author john
 * 
 */
public abstract class FindExecutable extends BasicExecutable {
	
	/**
	 * The {@link Regexp} {@link Instruction} that is used to parse.
	 */
	private final Regexp regexp;
	
	private final ScraperExecutable enclosingScraperExecutable;
	
	public FindExecutable(Interfaces context,
			Find find, ScraperExecutable enclosingScraperExecutable, Result source) {
		super(context, find, source);
		this.regexp = find;
		this.enclosingScraperExecutable = enclosingScraperExecutable;
	}
	
	/**
	 * 
	 * @return The String replacement to use when executing.
	 * @throws MissingVariableException if a {@link Variable} is missing.
	 * @throws MustacheTemplateException if the {@link MustacheTemplate} for
	 * the pattern is invalid.
	 */
	protected String getReplacement()
			throws MissingVariableException, MustacheTemplateException {
		Find find = (Find) getInstruction();
		return find.getReplacement().compile(this);
	}
	
	/**
	 * 
	 * @return The {@link Find} {@link net.microscraper.instruction.Instruction}'s {@link Find#name}, compiled through
	 * {@link Mustache}.  Returns the {@link net.microscraper.instruction.Instruction#location} as a String if none is
	 * specified.
	 * @throws MustacheTemplateException If the {@link Find#name} is an invalid {@link MustacheTemplate}.
	 * @throws MissingVariableException If the {@link Find#name} cannot be compiled with {@link #getVariables()}.
	 */
	protected String getName() throws MissingVariableException, MustacheTemplateException {
		Find find = (Find) getInstruction();
		if(find.hasName()) {
			return find.getName().compile(this);
		} else {
			return find.getLocation().toString();
		}
	}
	
	/**
	 * 
	 * @return The {@link PatternInterface} that this {@link FindExecutable} can use to parse its
	 * {@link #sourceResult}.
	 * @throws MissingVariableException
	 * @throws MustacheTemplateException
	 */
	protected final PatternInterface getPattern() throws MissingVariableException, MustacheTemplateException {
		return new RegexpExecutable(getInterfaces(), regexp, this).getPattern();
	}
	

	public final String get(String key) throws MissingVariableException {
		return enclosingScraperExecutable.get(key);
	}
	
	public final boolean containsKey(String key) {
		return enclosingScraperExecutable.containsKey(key);
	}
	
	/**
	 * 
	 * @return The {@link ScraperExecutable} that this {@link FindExecutable} lives inside.
	 */
	/*protected final ScraperExecutable getScraperExecutable() {
		return scraperExecutable;
	}*/
}
