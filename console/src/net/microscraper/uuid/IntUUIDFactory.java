package net.microscraper.uuid;

public class IntUUIDFactory implements UUIDFactory {
	private int curId = -1;
	public UUID get() {
		synchronized(this) {
			curId++;
			return new IntUUID(curId);
		}
	}
}
