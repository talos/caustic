package net.microscraper.database;

import java.io.IOException;

import net.microscraper.uuid.UUID;

public interface IODatabase extends Database {
	public String get(String tableName, UUID id, String columnName);
	
	public void insertOneToOne(UUID id, String resultTableName, String name) throws TableManipulationException;
	public void insertOneToOne(UUID id, String resultTableName, String name, String value) throws TableManipulationException;
	public DatabaseView insertOneToMany(UUID source, String resultTableName, String name) throws TableManipulationException, IOException;
	public DatabaseView insertOneToMany(UUID source, String resultTableName, String name, String value) throws TableManipulationException, IOException;

}
