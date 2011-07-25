package net.microscraper.executable;

import java.util.Hashtable;

import net.microscraper.instruction.Instruction;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.interfaces.publisher.Publisher;
import net.microscraper.interfaces.publisher.PublisherException;

public class BasicResult implements Result {
	private final String name;
	private final String value;
	private final Instruction instruction;
	private final int number;
	private final JSONLocation sourceUri;
	private final Integer sourceNumber;
	
	/**
	 * Keeps track of how many times each {@link Instruction} has generated a {@link Result}.
	 */
	private static final Hashtable countsForResource = new Hashtable();

	/**
	 * Construct a {@link BasicResult} with an explicit value for {@link #getName}.
	 * @param executable The {@link Executable} that created this {@link Result}.
	 * @param name The String result name.  Used in {@link Variables}. Can be <code>null</code>.
	 * @param value The String result value.
	 */
	public BasicResult(Executable executable, String name, String value) {
		this.name = name;
		this.value = value;
		this.instruction = executable.getInstruction();
		//this.number = generateNumber(getUri());
		this.number = generateNumber();
		if(executable.hasSource()) {
			this.sourceUri = executable.getSource().getInstruction().getLocation();
			this.sourceNumber = new Integer(executable.getSource().getInstructionNumber());
		} else {
			this.sourceUri = null;
			this.sourceNumber = null;
		}
	}
	
	/**
	 * 
	 * @return How many times {@link Instruction} has generated a {@link Result}.
	 */
	private int generateNumber() {
		String key = getInstruction().getLocation().toString();
		if(countsForResource.containsKey(key)) {
			int id = ((Integer) countsForResource.get(key)).intValue();
			countsForResource.put(key, new Integer(id + 1));
			return id;
		} else {
			int id = 0;
			countsForResource.put(key, new Integer(id + 1));
			return id;
		}
	}
	
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int getInstructionNumber() {
		return number;
	}

	public Instruction getInstruction() {
		return instruction;
	}
	
	public void publishTo(Publisher publisher) throws PublisherException {
		publisher.publishResult(name, value, instruction.getLocation(), number,
				sourceUri == null ? null : sourceUri,
				sourceNumber == null ? null : sourceNumber);
	}

	public boolean hasName() {
		if(getName() != null) {
			return true;
		} else {
			return false;
		}
	}
}
