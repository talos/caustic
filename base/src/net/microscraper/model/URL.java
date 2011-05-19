package net.microscraper.model;

/**
 * The URL resource holds a string that can be mustached and used as a URL.
 * @author john
 *
 */
public class URL {
	public final MustacheTemplate urlTemplate;
	public URL(MustacheTemplate urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	/**
	 * Create a {@link URL} from a String.
	 * @param String Input string.
	 * @return A {@link URL} instance.
	 */
	public static URL fromString(String urlTemplate) {
		return new URL(new MustacheTemplate(urlTemplate));
	}
}
