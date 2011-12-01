package net.caustic.instruction;

import net.caustic.json.JsonMEParser;
import net.caustic.regexp.DefaultRegexpCompiler;
import net.caustic.uri.DefaultURILoader;
import net.caustic.uri.JavaNetUriResolver;

/**
 * A default implementation of {@link JSONDeserializer}, using
 * {@link JsonMEParser}, {@link DefaultRegexpCompiler}, {@link JavaNetUriResolver},
 * and {@link DefaultURILoader}.
 * @author realest
 *
 */
public class DefaultJSONDeserializer extends JSONDeserializer {
	
	public DefaultJSONDeserializer() {
		super(
				new JsonMEParser(),
				new DefaultRegexpCompiler(),
				new JavaNetUriResolver(),
				new DefaultURILoader());
	}
}
