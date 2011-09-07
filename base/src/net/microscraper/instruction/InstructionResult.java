package net.microscraper.instruction;

import net.microscraper.template.MissingTags;

public class InstructionResult extends MissingTags {
	private final Executable[] children;
	private InstructionResult(Executable[] children) {
		this.children = children;
	}
	
	private InstructionResult(String[] missingTags) {
		super(missingTags);
		this.children = null;
	}
	
	public Executable[] getChildren() {
		return children;
	}
	
	public static InstructionResult newSuccess(Executable[] children) {
		return new InstructionResult(children);
	}
	
	public static InstructionResult newMissingTags(String[] missingTags) {
		return new InstructionResult(missingTags);
	}
	
}
