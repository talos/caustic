package net.microscraper.http;

public class JavaNetHttpRequesterTest extends HttpRequesterTest {

	@Override
	protected HttpRequester getHttpRequester() throws Exception {
		return new JavaNetHttpRequester();
	}

}
