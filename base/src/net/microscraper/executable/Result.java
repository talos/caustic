package net.microscraper.executable;

import net.microscraper.NameValuePair;

/**
 * {@link Result}s are {@link NameValuePair}s that store results from {@link Executable}s.
 * @see NameValuePair
 * @author john
 *
 */
public interface Result extends NameValuePair {
	/*
	private final Result source;
	private final String name;
	private final String value;
	private final int id;
	*/
	/**
	 * Construct a {@link Result} with an explicit {@link String} {@link #getName()}.
	 * @param source The {@link Result} source.  Should be <code>null</code> if there is no
	 * source for this {@link Result}.
	 * @param name The {@link String} result name for {@link #getName()}.
	 * @param value The {@link String} result value.
	 * @param database The {@link Database} that will store this {@link Result}.
	 * @throws DatabaseException if the {@link Database} encounters an exception.
	 */
	/*public Result(Result source, String name, String value, Database database) 
			throws DatabaseException {
		this.source = source;
		this.name = name;	
		this.value = value;
		this.id = database.store(this);
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	*/
	/**
	 * 
	 * @return A unique <code>int</code> identifier for this {@link Result}.
	 */
	public int getId(); /*{
		return id;
	}*/
	
	/**
	 * 
	 * @return <code>true</code> if {@link #getSource()} is not <code>null</code>,
	 * <code>false</code> otherwise.
	 * @see #getSource()
	 */
	/*public boolean hasSource() {
		return source == null ? false : true;
	}*/
	
	/**
	 * 
	 * @return The {@link Result} that spawned this {@link Result}.  Can be <code>
	 * null</code> if this was not spawned from a {@link Result}.
	 * @see #hasSource()
	 */
	/*public Result getSource() {
		return source;
	}*/
	
	/**
	 * Two {@link Result}s are equal if their IDs, names, values and sources (or
	 * lack of source) are the same.
	 */
	/*public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(!(obj instanceof Result))
			return false;
		Result that = (Result) obj;
		if(this.hasSource()) {
			return this.getId() == that.getId() &&
					this.getName().equals(that.getName()) &&
					this.getValue().equals(that.getValue()) &&
					this.getSource().equals(that.getSource());
		} else {
			return this.getId() == that.getId() &&
					this.getName().equals(that.getName()) &&
					this.getValue().equals(that.getValue()) &&
					that.hasSource() == false;
		}
	}*/
}
