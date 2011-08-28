package net.microscraper.http;

public interface ResponseHeaders {
	
	public String[] getHeaderNames();
	
	public String[] getHeaderValues(String headerName);
}
