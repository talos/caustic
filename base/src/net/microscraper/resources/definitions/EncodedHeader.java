package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

public class EncodedHeader extends AbstractHeader {
	public EncodedHeader(MustacheableString name, MustacheableString value) {
		super(name, value);
	}

	public EncodedNameValuePair getNameValuePair(ExecutionContext context)
			throws ExecutionDelay, ExecutionFailure, ExecutionFatality, UnsupportedEncodingException {
		return new EncodedNameValuePair(name.parse(context), value.parse(context), context.encoding);
	}
}
