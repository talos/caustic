package net.caustic.database;

import au.com.bytecode.opencsv.CSVWriter;

import net.caustic.database.DatabaseListener;
import net.caustic.database.DatabaseListenerException;
import net.caustic.instruction.Instruction;
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
	public void onPut(Scope scope, String key, String value) {
		write(null, scope, key, value);
	}

	@Override
	public void onNewDefaultScope(Scope scope) {

	}

	@Override
	public void onNewScope(Scope parent, Scope scope) {
		write(parent, scope, scope.getName(), null);
	}

	@Override
	public void onNewScope(Scope parent, Scope scope, String value) {
		write(parent, scope, scope.getName(), value);
	}
	
	private void write(Scope parent, Scope scope, String name, String value) {
		writer.writeNext(new String[] {
				parent == null ? null : parent.asString(),
				scope.asString(),
				name,
				value } );
	}

	@Override
	public void onStop(Scope scope, String source, Instruction instruction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddCookie(Scope scope, String url, String name, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRestart(Scope scope, Instruction instruction) {
		// TODO Auto-generated method stub
		
	}
}
