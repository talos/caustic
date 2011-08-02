package net.microscraper.interfaces.database;

import net.microscraper.executable.Result;

/**
 * Implementations of {@link Database} store the results of {@link Executable}s
 * and generate {@link Result} objects.
 * @see Result
 * @author john
 *
 */
public interface Database {
	
	/**
	 * Store a name and value without a source {@link Result} in the {@link Database}.
	 * @param name A {@link String} name to store this value under.
	 * @param value A {@link String} value.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(String name, String value) throws DatabaseException;
	
	/**
	 * Store a name and value with a source {@link Result} in the {@link Database}.
	 * @param source The {@link Result} source for the stored <code>name</code> and <code>
	 * value</code>.
	 * @param name A {@link String} name to store this value under.
	 * @param value A {@link String} value.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(Result source, String name, String value) throws DatabaseException;
	
}
