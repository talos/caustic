package net.caustic.http;

public class BrowserResponse {

	public final String content;
	public final String[] cookies;
	
	BrowserResponse(String content, String[] cookies) {
		this.content = content;
		this.cookies = cookies;
	}
}
