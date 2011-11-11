package net.caustic.http;

import java.net.MalformedURLException;
import java.net.URL;

import net.caustic.http.BadURLException;
import net.caustic.http.HttpUtils;

public class JavaNetHttpUtils implements HttpUtils {

	@Override
	public String getHost(String urlStr) throws BadURLException {
		try {
			return new URL(urlStr).getHost();
		} catch(MalformedURLException e) {
			throw new BadURLException(urlStr, e.getMessage());
		}
	}

}
