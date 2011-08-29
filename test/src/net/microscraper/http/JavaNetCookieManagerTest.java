package net.microscraper.http;

public class JavaNetCookieManagerTest extends CookieManagerTest {

	@Override
	protected CookieManager getCookieManager() throws Exception {
		return new JavaNetCookieManager();
	}
}
