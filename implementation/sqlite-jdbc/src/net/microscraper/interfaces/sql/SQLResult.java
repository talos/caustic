package net.microscraper.interfaces.sql;

import net.microscraper.executable.Result;

/**
 * A SQL implementation of {@link Result}.
 * @author talos
 *
 */
public class SQLResult implements Result {
	private final int id;
	private final String name;
	private final String value;
	
	public SQLResult(int id, String name, String value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int getId() {
		return id;
	}
}
