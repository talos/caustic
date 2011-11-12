package net.caustic.scope;

/**
 * An implementation of {@link Scope} wrapping
 * around an <code>int</code>.
 * @author talos
 *
 */
final class IntScope implements Scope {

	private final int id;
	protected IntScope(int id) {
		this.id = id;
	}

	public String asString() {
		return Integer.toString(id);
	}

	public int hashCode() {
		return id;
	}
	
	public String toString() {
		return asString();
	}
	
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		} else if(obj instanceof IntScope) {
			IntScope that = (IntScope) obj;
			return this.id == that.id;
		} else {
			return false;
		}
	}
}
