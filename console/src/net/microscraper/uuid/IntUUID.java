package net.microscraper.uuid;

final class IntUUID implements UUID {

	private final int id;
	public IntUUID(int id) {
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
}
