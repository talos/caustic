package net.microscraper.database;

import net.microscraper.console.UUID;

public interface WritableDatabase extends Database {
	public void insertOneToOne(UUID id, String name) throws TableManipulationException;
	public void insertOneToOne(UUID id, String name, String value) throws TableManipulationException;
	public UUID insertOneToMany(UUID source, String name) throws TableManipulationException;
	public UUID insertOneToMany(UUID source, String name, String value) throws TableManipulationException;
}
