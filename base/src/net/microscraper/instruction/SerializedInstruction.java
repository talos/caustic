package net.microscraper.instruction;

import net.microscraper.client.Deserializer;
import net.microscraper.database.Database;
import net.microscraper.template.DependsOnTemplate;
import net.microscraper.template.MissingTags;

public class SerializedInstruction implements DependsOnTemplate {
	private final String serializedString;
	private final Deserializer deserializer;
	private final String uri;
	
	public SerializedInstruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
		this.uri = uri;
	}
	
	/**
	 * 
	 * @return <code>true</code> if deserialization was successful, <code>false</code> otherwise.
	 */
	public boolean deserialize(Database database, String scope) {
		deserializer.deserialize(scope, database, scope, uri);
	}
	
	public Instruction getInstruction() {
		
	}
	
	public boolean isMissingTags() {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getMissingTags() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
