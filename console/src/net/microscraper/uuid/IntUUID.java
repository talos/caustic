package net.microscraper.uuid;

/**
 * An implementation of {@link UUID} wrapping
 * around an <code>int</code>.
 * @author talos
 *
 */
final class IntUUID implements UUID {

	private final int id;
	protected IntUUID(int id) {
		this.id = id;
	}

	@Override
	public String asString() {
		return Integer.toString(id);
	}

	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		return asString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		} else if(obj instanceof IntUUID) {
			IntUUID that = (IntUUID) obj;
			return this.id == that.id;
		} else {
			return false;
		}
	}
}
