package net.microscraper.database;

import net.microscraper.database.DatabaseException.PrematureRevivalException;

public interface Collection {
	public AbstractModel model();
	public Resource get(Reference ref) throws PrematureRevivalException;
	public Resource[] all() throws PrematureRevivalException;
}
