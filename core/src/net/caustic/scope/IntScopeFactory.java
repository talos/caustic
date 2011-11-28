package net.caustic.scope;

/**
 * Implementation of {@link ScopeFactory} using an incremented
 * integer.  Is synchronized.
 * @author realest
 *
 */
public final class IntScopeFactory implements ScopeFactory {
	private int curId;
	
	public Scope get(String name) {
		synchronized(this) {
			curId++;
			return new IntScope(curId, name);
		}
	}
	
	/**
	 * Construct {@link IntScopeFactory} where the first {@link
	 * IntScope} is <code>0</code>.
	 */
	public IntScopeFactory() {
		curId = -1;
	}
	
	/**
	 * Construct {@link IntScopeFactory} where the first {@link
	 * IntScope} is <code>firstId</code>.
	 * @param the starting <code>int</code> in {@link IntScope}.
	 */
	public IntScopeFactory(int firstId) {
		curId = firstId -1;
	}
}
