package net.caustic.http;

public class BrowserResponse {

	public final String content;
	public final Cookies cookies;
	
	BrowserResponse(String content, Cookies cookies) {
		this.content = content;
		this.cookies = cookies;
	}
}
