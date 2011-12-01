package net.caustic.database;

import java.util.Vector;

import net.caustic.instruction.Find;
import net.caustic.instruction.Load;
import net.caustic.scope.Scope;
import net.caustic.util.VectorUtils;

class StuckExecution {

	private final Vector missingTags = new Vector();
	
	private final String source;
	
	private String instruction;
	private String uri;
	
	private Load load;
	private Find find;
	
	StuckExecution(String source, String instruction, String uri, String[] missingTags) {
		VectorUtils.arrayIntoVector(missingTags, this.missingTags);
		this.instruction = instruction;
		this.uri = uri;
		this.source = source;
	}
	
	StuckExecution(String source, Load load) {
		this.source = source;
		this.load = load;
	}
	
	StuckExecution(String source, Find find) {
		this.source = source;
		this.find = find;
	}
	
	/**
	 * Inform {@link StuckExecution} that a tag with name <code>name</code>
	 * has appeared in a scope that affects it.
	 * @param name The {@link String} name of the tag.
	 * @return <code>True</code> if this is no longer stuck, <code>false</code>
	 * otherwise.
	 */
	boolean isReady(String name) {
		missingTags.removeElement(name);
		if(missingTags.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Re-post this {@link StuckExecution}.
	 * @param db the {@link Database} to re-post to.
	 * @param scope the {@link Scope} in <code>db</code> to re-post to.
	 * @throws DatabaseException
	 */
	void retry(Database db, Scope scope) throws DatabaseException {
		if(instruction != null) {
			db.putInstruction(scope, source, instruction, uri);
		} else if(load != null) {
			db.putLoad(scope, source, load);
		} else {
			db.putFind(scope, source, find);
		}
	}
}
