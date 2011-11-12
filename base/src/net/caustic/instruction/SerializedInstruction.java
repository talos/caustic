package net.caustic.instruction;

import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseView;
import net.caustic.deserializer.Deserializer;
import net.caustic.deserializer.DeserializerResult;
import net.caustic.http.HttpBrowser;

/**
 * An {@link Instruction} that has yet to be deserialized.  This allows for lazy evaluation
 * of children, in particular where a child cannot be deserialized until a variable is available
 * in the database.
 * @author talos
 *
 */
public class SerializedInstruction extends Instruction {

	private final Deserializer deserializer;
	private final String serializedString;
	private final String uri;
	
	public SerializedInstruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
		this.uri = uri;
	}
	
	public InstructionResult execute(String source, DatabaseView view,
			HttpBrowser browser) throws InterruptedException, DatabaseException {
		DeserializerResult deserializerResult = deserializer.deserialize(serializedString, view, uri);
		
		if(deserializerResult.isMissingTags()) {
			return InstructionResult.missingTags(deserializerResult.getMissingTags());
		} else if(deserializerResult.getInstruction() != null) {
			return deserializerResult.getInstruction().execute(source, view, browser);
		} else {
			return InstructionResult.failed(deserializerResult);
		}
	}

}
