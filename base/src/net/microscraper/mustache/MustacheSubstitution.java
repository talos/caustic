package net.microscraper.mustache;


public class MustacheSubstitution {
	
	private final String text;
	private final boolean isSuccessful;
	private MustacheSubstitution(boolean isSuccessful, String text) {
		this.text = text;
		this.isSuccessful = isSuccessful;
	}
	
	public static MustacheSubstitution success(String substitutedText) {
		return new MustacheSubstitution(true, substitutedText);
	}
	
	public static MustacheSubstitution fail(String missingTag) {
		return new MustacheSubstitution(false, missingTag);
	}
	
	public boolean isSuccessful() {
		return isSuccessful;
	}
	
	public String getMissingTag() {
		if(isSuccessful == true) {
			throw new IllegalStateException("Template was successful.");
		}
		return text;
	}
	
	public String getSubbed() {
		if(isSuccessful == false) {
			throw new IllegalStateException("Template was not successful.");
		}
		return text;
	}
}
