package net.caustic.instruction;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.deserializer.Deserializer;
import net.caustic.deserializer.DeserializerResult;
import net.caustic.http.HttpBrowser;
import net.caustic.scope.Scope;

/**
 * An {@link Instruction} that has yet to be deserialized.  This allows for lazy evaluation
 * of children, in particular where a child cannot be deserialized until a variable is available
 * in the database.
 * @author talos
 *
 */
public abstract class Instruction {

	private final Deserializer deserializer;
	private final String serializedString;
	private final String uri;
	
	public Instruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
		this.uri = uri;
	}

	public boolean shouldConfirm() {
		return false;
	}
	
	public void execute(String source, Database db, Scope scope,
			HttpBrowser browser) throws InterruptedException, DatabaseException {
		DeserializerResult result = deserializer.deserialize(serializedString, db, scope, uri);
		
		if(result.isMissingTags()) {
			db.putMissing(scope, source, this, result.getMissingTags());
		} else if(result.getInstruction() != null) {
			//db.putReady(scope, source, result.getInstruction());
			result.getInstruction().execute(source, db, scope, browser);
		} else {
			db.putFailed(scope, source, this, result.getFailedBecause());
		}
	}
	
	public String toString() {
		return serializedString;
	}
}
