package net.microscraper.resources.definitions;

public final class Cookie extends EncodedHeader {
	public Cookie(MustacheableString name, MustacheableString value) {
		super(name, value);
	}
}