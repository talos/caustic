package net.microscraper.resources.definitions;

public abstract class AbstractHeader {
	protected final MustacheableString name;
	protected final MustacheableString value;
	public AbstractHeader(MustacheableString name, MustacheableString value) {
		this.name = name;
		this.value = value;
	}
}