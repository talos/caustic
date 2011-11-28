package net.caustic.scope;

/**
 * An implementation of {@link Scope} wrapping
 * around an <code>int</code>.
 * @author talos
 *
 */
final class IntScope implements Scope {

	private final int id;
	private final String name;
	IntScope(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
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
		} else if(obj instanceof Scope) {
			Scope that = (Scope) obj;
			return this.asString().equals(that.asString());
		} else {
			return false;
		}
	}
}
