package net.microscraper.resources.definitions;

public class LinkToOne extends Link {
	private final ParserOneToOne to;
	public LinkToOne(Variable from, ParserOneToOne to) {
		super(from);
		this.to = to;
	}
	public ParserOneToOne to() {
		return to;
	}
}
