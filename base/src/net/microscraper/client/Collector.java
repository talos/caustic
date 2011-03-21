package net.microscraper.client;

public interface Collector {
	public void collect(Information information) throws InterruptedException;
}
