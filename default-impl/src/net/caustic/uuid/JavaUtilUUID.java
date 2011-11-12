package net.caustic.uuid;

import net.caustic.scope.Scope;


/**
 * Implementation of {@link Scope} that uses {@link java.util.UUID} to generate
 * unique identifiers.
 * @author talos
 *
 */
final class JavaUtilUUID implements Scope {
	private final java.util.UUID uuid = java.util.UUID.randomUUID();
	
	protected JavaUtilUUID() { };
	
	@Override
	public String asString() {
		return uuid.toString();
	}
	
	@Override
	public String toString() {
		return asString();
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		} else if(obj instanceof JavaUtilUUID) {
			JavaUtilUUID that = (JavaUtilUUID) obj;
			return this.uuid.equals(that.uuid);
		} else {
			return false;
		}
	}
}