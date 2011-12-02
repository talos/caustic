package net.caustic.database;

import au.com.bytecode.opencsv.CSVWriter;

import net.caustic.database.DatabaseListener;
import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
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

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutInstruction(Scope scope, String source,
			String instruction, String uri) {		
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutMissing(Scope scope, String source, String instruction,
			String uri, String[] missingTags) {
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutFailed(Scope scope, String source, String instruction,
			String uri, String failedBecause) {
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutSuccess(Scope scope, String source, String instruction,
			String uri) {
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onScopeComplete(Scope scope, int successes, int stuck,
			int failed) {
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutLoad(Scope scope, String source, Load load) {
	}

	/**
	 * Not mentioned on CSV.
	 */
	@Override
	public void onPutFind(Scope scope, String source, Find find) {
	}
	
}
