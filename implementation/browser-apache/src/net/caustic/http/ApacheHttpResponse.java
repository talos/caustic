package net.caustic.http;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;

class ApacheHttpResponse implements HttpResponse {

	private final org.apache.http.HttpResponse response;
	
	public ApacheHttpResponse(org.apache.http.HttpResponse response) {
		this.response = response;
	}
	
	@Override
	public InputStreamReader getContentStream() {
		HttpEntity entity = response.getEntity();
		new InputStreamReader(entity.getContent());
	}

	@Override
	public boolean isSuccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseHeaders getResponseHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRedirect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRedirectLocation() throws BadURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getResponseCode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
