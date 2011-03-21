package com.invisiblearchitecture.scraper;

public interface Collector {
	public void collect(Information information) throws InterruptedException;
}
