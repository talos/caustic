package net.microscraper.util;

import java.net.MalformedURLException;
import java.net.URL;

import net.microscraper.http.BadURLException;

public class JavaNetHttpUtils implements HttpUtils {

	@Override
	public String getHost(String urlStr) throws BadURLException {
		try {
			return new URL(urlStr).getHost();
		} catch(MalformedURLException e) {
			throw new BadURLException(e);
		}
	}

}
