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
	 * @param resultNum The 0-based {@link int} index of this {@link Result} within its 
	 * {@link Executable}.
	 * @param shouldSaveValue Whether or not <code>value</code> should be saved in the 
	 * {@link Database}.  It may be better to avoid saving the values of certain large,
	 * redundant results -- entire pages, for example.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(String name, String value,
			int resultNum, boolean shouldSaveValue) throws DatabaseException;
	
	/**
	 * Store a name and value with a source {@link Result} in the {@link Database}.
	 * @param source The {@link Result} source for the stored <code>name</code> and <code>
	 * value</code>.
	 * @param name A {@link String} name to store this value under.
	 * @param value A {@link String} value.
	 * @param resultNum The 0-based {@link int} index of this {@link Result} within its 
	 * {@link Executable}.
	 * @param shouldSaveValue Whether or not <code>value</code> should be saved in the 
	 * {@link Database}.  It may be better to avoid saving the values of certain large,
	 * redundant results -- entire pages, for example.
	 * @return A {@link Result} for use as a source.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public Result store(Result source, String name, String value,
			int resultNum, boolean shouldSaveValue) throws DatabaseException;
	
	/**
	 * Close up the {@link Database}, performing whatever cleaning actions should be performed
	 * on it before the {@link IOConnection} is closed.
	 * @throws DatabaseException If the {@link Database} experiences an exception.
	 */
	public void close() throws DatabaseException;
	
}
