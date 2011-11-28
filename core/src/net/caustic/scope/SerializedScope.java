package net.caustic.scope;

/**
 * {@link SerializedScope} can be used to recreate a {@link Scope}
 * stored in a string.
 * @author talos
 *
 */
public class SerializedScope implements Scope {
	private final String serializedUUID;
	private final String name;
	
	/**
	 * Construct a {@link Scope} from a serialized {@link String}.
	 * @param serializedUUID The {@link Scope} stored as a {@link String}.
	 * @param name The {@link String} name of the scope.
	 */
	public SerializedScope(String serializedUUID, String name) {
		this.serializedUUID = serializedUUID;
		this.name = name;
	}

	public String asString() {
		return serializedUUID;
	}
	
	public String toString() {
		return asString();
	}
	
	public int hashCode() {
		return serializedUUID.hashCode();
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

	public String getName() {
		return name;
	}
}
