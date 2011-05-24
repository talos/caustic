package net.microscraper.client.executable;

import net.microscraper.client.ExecutionContext;
import net.microscraper.server.resource.Find;

/**
 * {@link FindExecutable}s are the {@link BasicExecutable} implementation of {@link Find}s, and are contained inside
 * {@link Scraper}s but link to a {@link Regexp} resource.  If one-to-many,
 * they are subclassed as {@link FindManyExecutable}; if one-to-one, they are
 * subclassed as {@link FindOneExecutable}.
 * @see LeafExectuion
 * @see FindOneExecutable
 * @see BasicExecutable
 * @see Executable
 * @author john
 * 
 */
public abstract class FindExecutable extends BasicExecutable {
	public FindExecutable(ExecutionContext context, Find find,
			Executable caller) {
		super(context, find, caller);
	}
}
