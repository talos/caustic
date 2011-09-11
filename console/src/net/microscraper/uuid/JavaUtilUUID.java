package net.microscraper.uuid;


/**
 * Implementation of {@link UUID} that uses {@link java.util.UUID} to generate
 * unique identifiers.
 * @author talos
 *
 */
final class JavaUtilUUID implements UUID {
	private final java.util.UUID uuid = java.util.UUID.randomUUID();
	
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
}