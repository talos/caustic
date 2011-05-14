package net.microscraper.resources.definitions;

import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

public class StringDefault implements Variable, Stringable, Executable {
	private final Reference ref;
	private final MustacheTemplate template;

	/**
	 * @param ref {@link Reference} A ref to uniquely identify the StringDefault.
	 * @param template {@link MustacheTemplate} The template compiled to obtain the String default.
	 */
	public StringDefault(Reference ref, MustacheTemplate template) {
		this.ref = ref;
		this.template = template;
	}
	public Reference getRef() {
		return ref;
	}
	public String getString(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		return template.getString(context);
	}
}
