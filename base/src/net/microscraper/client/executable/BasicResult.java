package net.microscraper.client.executable;

import java.util.Hashtable;

import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.Instruction;

public class BasicResult implements Result {
	private final String name;
	private final String value;
	private final URIInterface uri;
	private final int number;
	private final URIInterface sourceUri;
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
		this.uri = executable.getResource().location;
		//this.number = generateNumber(getUri());
		this.number = generateNumber();
		if(executable.hasSource()) {
			this.sourceUri = executable.getSource().getUri();
			this.sourceNumber = new Integer(executable.getSource().getNumber());
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
		String key = getUri().toString();
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

	public int getNumber() {
		return number;
	}

	public URIInterface getUri() {
		return uri;
	}
	
	public void publishTo(Publisher publisher) throws PublisherException {
		publisher.publishResult(name, value, uri.toString(), number,
				sourceUri == null ? null : sourceUri.toString(),
				sourceNumber == null ? null : sourceNumber);
	}
}
