package net.caustic.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.caustic.util.Decoder;
import net.caustic.util.FormEncodedFormatException;
import net.caustic.util.StringUtils;

public class MapUtils {
	
	/**
	 * Turn a form-encoded {@link String} into {@link Map}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.  It must be 
	 * correctly formatted.
	 * @return A {@link Map}.
	 */
	public static Map<String, String> fromFormEncoded(Decoder decoder, String formEncodedData)
			throws UnsupportedEncodingException, FormEncodedFormatException {
		String[] splitByAmpersands = StringUtils.split(formEncodedData, "&");
		Map<String, String> result = new HashMap<String, String>();
		for(int i = 0 ; i < splitByAmpersands.length; i++) {
			String[] pair = StringUtils.split(splitByAmpersands[i], "=");
			if(pair.length == 2) {
				result.put(decoder.decode(pair[0]),
						decoder.decode(pair[1]));
			} else {
				throw new FormEncodedFormatException(
						StringUtils.quote(splitByAmpersands[i]) + " is not a valid name-value pair.");
			}
		}
		return result;
	}
}
