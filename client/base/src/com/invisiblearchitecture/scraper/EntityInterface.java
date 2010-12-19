package com.invisiblearchitecture.scraper;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface that must be able to both obtain an InputStream and ConsumeContent.
 */
public interface EntityInterface {
	public abstract InputStream getInputStream() throws IllegalStateException, IOException;
	public abstract void consumeContent() throws IOException;
}

