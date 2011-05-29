package net.microscraper.client.executable;

import net.microscraper.server.Resource;

public class BasicResult implements Result {
	//private final Resource resource;
	private final String name;
	private final String value;
	private final int id;
	private static int count = 0;
	
	/**
	 * Construct a {@link BasicResult} without an explicit value for {@link #getName}.
	 * Defaults to the location of the executed {@link Resource}.
	 * @param resource the executed {@link Resource}.
	 * @param value The String result value.
	 */
	public BasicResult(Resource resource, String value) {
		this.id = count;
		count ++;
		
		this.name = resource.location.toString();
		this.value = value;
	}

	/**
	 * Construct a {@link BasicResult} with an explicit value for {@link #getName}.
	 * @parm name The String result name.  Used in {@link Variables}.
	 * @param value The String result value.
	 */
	public BasicResult(String name, String value) {
		this.id = count;
		count++;
		
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
/*
	public Executable getExecutable() {
		// TODO Auto-generated method stub
		return null;
	}
*/
	/**
	 * Starts at <code>0</code> and increments up.
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return A {@link BasicResult} as an initial result.
	 */
	public static BasicResult Root() {
		return new BasicResult("", "");
	}
}
