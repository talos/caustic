package net.microscraper.database;


public interface DatabaseViewHook {
	public void put(String name, String value);
	public void spawnChild(String name, DatabaseView child);
	public void spawnChild(String name, String value, DatabaseView child);
}
