package net.microscraper.executable;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Regexp;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

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
	
	//private final Executable enclosingExecutable;
	
	public FindExecutable(Find find, RegexpCompiler compiler, Browser browser,
			Result source, Database database) {
		super(find, compiler, browser, source, database);
		//this.enclosingExecutable = enclosingExecutable;
	}
	
}
