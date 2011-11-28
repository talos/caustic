package net.caustic.scope;


/**
 * Factory interface to generate {@link Scope}s.
 * @author realest
 *
 */
public interface ScopeFactory {
	
	/**
	 * @param name A {@link String} name for the returned {@link Scope}.
	 * @return A new {@link Scope} of name <code>name</code>.
	 * 
	 */
	public Scope get(String name);
}
