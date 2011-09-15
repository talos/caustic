package net.microscraper.uuid;

final class IntUUID implements UUID {

	private final int id;
	public IntUUID(int id) {
		this.id = id;
	}
	
	public String asString() {
		return Integer.toString(id);
	}
	
	public int hashCode() {
		return id;
	}
}
