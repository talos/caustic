package net.caustic.database;

import au.com.bytecode.opencsv.CSVWriter;

import net.caustic.database.DatabaseListener;
import net.caustic.database.DatabaseListenerException;
import net.caustic.scope.Scope;

/**
 * Record database transactions into a CSV.
 * @author talos
 *
 */
public class CSVDatabaseListener implements DatabaseListener {

	private final CSVWriter writer;	
	public CSVDatabaseListener(char separator) {
		
		//writer = new CSVWriter(new FileWriter(file), separator);
		writer = new CSVWriter(new SystemOutWriter(), separator);
		
		writer.writeNext(new String[] { "source", "scope", "name", "value" });
	}
	
	@Override
	public void onPut(Scope scope, String key, String value)
			throws DatabaseListenerException {
		write(null, scope, key, value);
	}

	@Override
	public void onNewScope(Scope scope) throws DatabaseListenerException {

	}

	@Override
	public void onNewScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		write(parent, child, key, null);
	}

	@Override
	public void onNewScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		write(parent, child, key, value);
	}
	
	private void write(Scope parent, Scope child, String name, String value) {
		writer.writeNext(new String[] {
				parent == null ? null : parent.asString(),
				child == null ? null  : child.asString(),
				name,
				value } );
	}
}
