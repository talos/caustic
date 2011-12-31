package net.caustic.http;

import net.caustic.http.HttpBrowser;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.RateLimitManager;

/**
 * A default {@link HttpBrowser}.  Uses a {@link JavaNetHttpRequester}
 * along with {@link JavaNetHttpUtils} and {@link JavaNetCookieManager}.
 * @author realest
 *
 */
public class DefaultHttpBrowser extends HttpBrowser {

	public DefaultHttpBrowser() {
		super(new JavaNetHttpRequester(), new RateLimitManager(new JavaNetHttpUtils()), new JavaNetHttpUtils());
	}
}
