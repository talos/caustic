package net.caustic.scope;


/**
 * Factory interface to generate {@link Scope}s.
 * @author realest
 *
 */
public interface ScopeFactory {
	
	/**
	 * 
	 * @return A new {@link Scope}.
	 */
	public Scope get();
}
