package net.microscraper.util;

public class IntUUIDFactory implements UUIDFactory {
	private int curId = -1;
	public synchronized UUID get() {
		curId++;
		return new IntUUID(curId);
	}
}
