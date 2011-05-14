package net.microscraper.resources.definitions;

public abstract class AbstractHeader {
	protected final MustacheTemplate name;
	protected final MustacheTemplate value;
	public AbstractHeader(MustacheTemplate name, MustacheTemplate value) {
		this.name = name;
		this.value = value;
	}
}