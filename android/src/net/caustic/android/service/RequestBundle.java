package net.caustic.android.service;

import android.os.Bundle;

public class RequestBundle {

	private static final String ID = "id";
	private static final String INSTRUCTION = "instruction";
	private static final String URI = "uri";
	private static final String INPUT = "input";
	private static final String FORCE = "force";
	
	private final String id;
	private final String instruction;
	private final String uri;
	private final String input;
	private final boolean force;
	
	RequestBundle(String id, String instruction, String uri, String input, boolean force) {
		this.id = id;
		this.instruction = instruction;
		this.uri = uri;
		this.force = force;
		this.input = input;
	}

	RequestBundle(Bundle bundle) {
		this.id = bundle.getString(ID);
		this.instruction = bundle.getString(INSTRUCTION);
		this.uri = bundle.getString(URI);
		this.force = bundle.getBoolean(FORCE);
		this.input = bundle.getString(INPUT);
	}

	String getID() {
		return id;
	}

	String getInstruction() {
		return instruction;
	}
	
	String getURI() {
		return uri;
	}
	
	String getInput() {
		return input;
	}
	
	boolean getForce() {
		return force;
	}

	Bundle pack() {
		Bundle bundle = new Bundle();
		bundle.putString(ID, id);
		bundle.putString(INSTRUCTION, instruction);
		bundle.putString(URI, uri);
		bundle.putString(INPUT, input);
		bundle.putBoolean(FORCE, force);
		return bundle;
	}
}
