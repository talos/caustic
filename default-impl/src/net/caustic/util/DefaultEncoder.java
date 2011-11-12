package net.caustic.util;

import java.io.UnsupportedEncodingException;

import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

/**
 * A default encoder.  Is a {@link JavaNetEncoder} using
 * UTF-8.
 * @author realest
 *
 */
public class DefaultEncoder implements Encoder {
	private final Encoder encoder;
	
	public DefaultEncoder() {
		try {
			this.encoder = new JavaNetEncoder(Encoder.UTF_8);
		} catch(UnsupportedEncodingException e) {
			throw new InstantiationError("Default encoder requires UTF-8 support.");
		}
	}
	
	@Override
	public String encode(String stringToEncode) {
		return encoder.encode(stringToEncode);
	}

}
