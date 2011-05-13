package net.microscraper.resources.definitions;

import net.microscraper.resources.Executable;
import net.microscraper.resources.ExecutionContext;

public abstract class AbstractHeader implements Executable {
	private final MustacheableString name;
	private final MustacheableString value;
	public AbstractHeader(MustacheableString name, MustacheableString value) {
		this.name = name;
		this.value = value;
	}
	public Object execute(ExecutionContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}