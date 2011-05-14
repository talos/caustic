package net.microscraper.resources.definitions;

public final class LinkToMany extends Link {
	private final ParserOneToMany to;
	public LinkToMany(Variable from, ParserOneToMany to) {
		super(from);
		this.to = to;
	}
	public ParserOneToMany to() {
		return to;
	}
}
