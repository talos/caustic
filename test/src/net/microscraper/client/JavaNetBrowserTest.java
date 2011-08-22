package net.microscraper.client;

import net.microscraper.browser.JavaNetBrowser;

public class JavaNetBrowserTest extends BrowserTest {

	@Override
	protected Browser getBrowser() {
		return new JavaNetBrowser();
	}

}
