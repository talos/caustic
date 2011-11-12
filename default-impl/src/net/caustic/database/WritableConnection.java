package net.caustic.database;

/**
 * A connection interface to create {@link WritableTable}s.
 * @author talos
 *
 */
public interface WritableConnection extends Connection {
	/**
	 * Obtain a new {@link WritableTable} using this {@link WritableConnection}.
	 * @param name The {@link String} name of the {@link WritableTable}.
	 * @param columnNames An array of {@link String} columns to include in this 
	 * {@link WritableTable}.
	 * @return A {@link WritableTable}.
	 * @throws ConnectionException if the {@link WritableTable} cannot be created.
	 */
	public abstract WritableTable newWritable(String name, String[] columnNames)
			throws ConnectionException;

}
