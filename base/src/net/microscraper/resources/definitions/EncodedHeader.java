package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

public class EncodedHeader extends AbstractHeader {
	public EncodedHeader(MustacheTemplate name, MustacheTemplate value) {
		super(name, value);
	}

	public EncodedNameValuePair getNameValuePair(ExecutionContext context)
			throws ExecutionDelay, ExecutionFatality, UnsupportedEncodingException {
		return new EncodedNameValuePair(name.getString(context), value.getString(context), context.getEncoding());
	}
}
