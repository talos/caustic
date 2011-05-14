package net.microscraper.resources;

public final class ContingentResult extends Result {
	public Result input;
	public ContingentResult(Result input, String output) {
		super(output);
		this.input = input;
	}
}
