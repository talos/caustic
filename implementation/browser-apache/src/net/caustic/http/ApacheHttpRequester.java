package net.caustic.http;

import java.util.Hashtable;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class ApacheHttpRequester implements HttpRequester {
	private final HttpClient client = new DefaultHttpClient();
	
	@Override
	public HttpResponse head(String url, Hashtable requestHeaders)
			throws InterruptedException, HttpRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse get(String url, Hashtable requestHeaders)
			throws InterruptedException, HttpRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse post(String url, Hashtable requestHeaders,
			String encodedPostData) throws InterruptedException,
			HttpRequestException {
		HttpPost post = new HttpPost(url);
		client.execute(post);
	}

	@Override
	public void setTimeout(int timeoutMilliseconds) {
		// TODO Auto-generated method stub

	}

}
