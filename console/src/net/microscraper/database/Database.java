package net.microscraper.database;

import java.io.IOException;

public interface Database {
	
	public void open() throws IOException;
	public DatabaseView newView();
	public void close() throws IOException;
}
