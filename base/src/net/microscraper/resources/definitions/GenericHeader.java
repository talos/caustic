package net.microscraper.resources.definitions;

import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * A generic header to add to a WebPage request.
 * @author john
 *
 */
public final class GenericHeader extends AbstractHeader {
	public GenericHeader(MustacheTemplate name, MustacheTemplate value) {
		super(name, value);
	}
	
	/**
	 * Generate an {@link UnencodedNameValuePair}.
	 * @param context
	 * @return an {@link UnencodedNameValuePair}.
	 * @throws ExecutionDelay
	 * @throws ExecutionFailure
	 * @throws ExecutionFatality
	 */
	public UnencodedNameValuePair getNameValuePair(ExecutionContext context)
			throws ExecutionDelay, ExecutionFatality {
		return new UnencodedNameValuePair(name.getString(context), value.getString(context));
	}
}