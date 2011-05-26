package net.microscraper.client.executable;

import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.Variables;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.PatternInterface;
import net.microscraper.server.resource.Find;
import net.microscraper.server.resource.Regexp;

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
	private final Regexp regexp;

	public FindExecutable(Interfaces context,
			Find find, Variables variables, Executable parent) {
		super(context, find, variables, parent);
		this.regexp = find;
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
		Find find = (Find) getResource();
		return find.replacement.compile(getVariables());
	}
	
	/**
	 * 
	 * @return The {@link Find} {@link net.microscraper.server.Resource}'s {@link Find#name}, compiled through
	 * {@link Mustache}.  Returns the {@link net.microscraper.server.Resource#location} as a String if none is
	 * specified.
	 * @throws MustacheTemplateException If the {@link Find#name} is an invalid {@link MustacheTemplate}.
	 * @throws MissingVariableException If the {@link Find#name} cannot be compiled with {@link #getVariables()}.
	 */
	protected String getName() throws MissingVariableException, MustacheTemplateException {
		Find find = (Find) getResource();
		if(find.hasName) {
			return find.name.compile(getVariables());
		} else {
			return find.location.toString();
		}
	}
	
	protected final PatternInterface getPattern() throws MissingVariableException, MustacheTemplateException {
		return new RegexpExecution(getContext(), regexp, getVariables()).getPattern();
	}
	
	/**
	 * {@link FindExecutable} overrides this, returns {@link Find#hasName}.
	 */
	/*public boolean hasName() {
		Find find = (Find) getResource();
		return find.hasName;
	}*/
	
	/**
	 * {@link FindExecutable} overrides this, returns {@link Find#getName}.
	 * @throws MustacheTemplateException If the {@link Find#name} cannot be compiled.
	 * @throws MissingVariableException If the {@link Find#name} needs another variable.
	 */
	/*public String getName() throws MissingVariableException, MustacheTemplateException {
		if(hasName()) {
			return ((Find) getResource()).name.compile(getVariables());
		}
		throw new NullPointerException();
	}*/
	
	/**
	 * {@link FindExecutable} overrides this, returns {@link BasicExecutable#generateResult}.
	 */
	/*public boolean hasValue() {
		if(isComplete()) {
			return true;
		}
		return false;
	}*/
	
	/**
	 * Defaults to throwing {@link NullPointerException}.
	 */
	/*public String getValue() {
		if(isComplete()) {
			throw new NullPointerException();
		} else {
			throw new IllegalStateException();
		}
	}*/
}
