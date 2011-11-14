package net.caustic.util;

import java.io.UnsupportedEncodingException;


/**
 * Default wrapper for {@link Decoder}, uses UTF-8.
 * @author talos
 *
 */
public class DefaultDecoder implements Decoder {
	private final Decoder decoder;
	
	public DefaultDecoder() {
		try {
			this.decoder = new JavaNetDecoder(Encoder.UTF_8);
		} catch(UnsupportedEncodingException e) {
			throw new InstantiationError("Default decoder requires UTF-8 support.");
		}
	}
	
	@Override
	public String decode(String stringToDecode) {
		return decoder.decode(stringToDecode);
	}

}
