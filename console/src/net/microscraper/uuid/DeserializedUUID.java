package net.microscraper.uuid;

/**
 * {@link DeserializedUUID} can be used to recreate a {@link UUID}
 * stored in a string.
 * @author talos
 *
 */
public class DeserializedUUID implements UUID {
	private final String serializedUUID;
	
	/**
	 * Construct a {@link UUID} from a serialized {@link String}.
	 * @param serializedUUID The {@link UUID} stored as a {@link String}.
	 */
	public DeserializedUUID(String serializedUUID) {
		this.serializedUUID = serializedUUID;
	}

	@Override
	public String asString() {
		return serializedUUID;
	}
	
	@Override
	public int hashCode() {
		return serializedUUID.hashCode();
	}
}
