/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package com.invisiblearchitecture.scraper;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author realest
 *
 */
public class Utils {
	public static class Capitalize {
	    public static String Word(String s) {
	        if (s.length() == 0) return s;
	        s = s.trim();
	        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	    }

	    public static String Words(String input) {
	    	/*String output = "";
	    	int endWordIndex = 0;
	    	input = input.trim();
	    	while((endWordIndex = input.indexOf(" ", endWordIndex)) != -1) {
	    		String word = input.substring(0, endWordIndex);
	    		output += Capitalize.Word(word) + " ";
	    	}
	    	return output.trim();*/
	    	String[] words = Utils.splitBySpaces(input);
	    	for(int i = 0; i < words.length; i++) {
	    		words[i] = Utils.Capitalize.Word(words[i]);
	    	}
	    	return Utils.join(words, " ");
	    }
	}
	
	/**
	 * Trim every member of an array of Strings.
	 * @param list
	 * @return
	 */
	public static String[] trimStringList(String[] list) {
		if(list == null)
			return null;
		String[] trimmedList = new String[list.length];
		for(int i = 0; i < list.length; i++) {
			try {
				trimmedList[i] = list[i].trim();
			} catch (NullPointerException e) {}
		}
		return trimmedList;
	}
	
	/**
	 * Join an array of strings with a joinString.
	 * @param strings
	 * @param joinString
	 * @return
	 */
	public static String join(String[] strings, String joinString) {
		String joined = "";
		for(int i = 0; i < strings.length; i++) {
			joined += strings[i];
			if(i < strings.length -1)
				joined += joinString;
		}
		return joined;
	}
	
	/**
	 * Join an array of strings with a comma, then an and.
	 * @param strings
	 * @return
	 */
	public static String joinWithCommasAnd(String[] strings) {
		String joined = "";
		for(int i = 0; i < strings.length; i++) {
			joined += strings[i];
			if(i < strings.length - 2) {
				joined += ", ";
			} else if(i < strings.length - 1) {
				joined += ", and ";
			}
		}
		return joined;
	}
	
	/**
	 * Split a string into words based off of spaces without using regex or .split().
	 * @param input
	 * @return
	 */
	public static String[] splitBySpaces(String input) {
		int spaceLoc = 0;
		String wordsString = input.trim();
		Vector splitString = new Vector();
    	do {
    		spaceLoc = wordsString.indexOf(" ");
    		String word;
    		switch(spaceLoc) {
    			case 0:
        			wordsString = wordsString.substring(1);
        			continue;
    			case -1:
    				word = wordsString;
    				break;
    			default:
        			word = wordsString.substring(0, spaceLoc);
        			wordsString = wordsString.substring(spaceLoc);
    		}
    		splitString.addElement(word);
    	} while(spaceLoc != -1);
    	
    	String[] output = new String[splitString.size()];
    	splitString.copyInto(output);
    	return output;
	}
	

	private static String[] streetDirs = {"N", "NW", "W", "SW", "S", "SE", "E", "NE"};
	// From http://www.usps.com/ncsc/lookups/abbr_suffix.txt . Groovy.
	private static String[] streetSuffixes = {"ALY", "ANX", "ARC", "AV", "AVE", "AVN",
		"BCH", "BND", "BLF", "BLFS", "BTM", "BLVD", "BYU", "BR", "BRG", "BRK", "BRKS", "BG", "BGS",
		"BYP", "CP", "CMP", "CYN", "CPE", "CSWY", "CTR", "CEN", "CNTR", "CTRS", "CIR", "CRCL", "CIRC",
		"CLF", "CLB", "CMN", "COR", "CORS", "CRSE", "CT", "CRT", "CTS", "CV", "CK", "CR", "CRK", 
		"CRES", "CRST", "XING", "XRD", "CURV", "DL", "DM", "DV", "DVD", "DR", "DRV", "EST",
		"ESTS", "EXPY", "EXP", "EXT", "EXTS", "EXTN", "FLS", "FRY", "FLD", "FLDS", "FLT", "FLTS", 
		"FRD", "FRDS", "FRST", "FRG", "FRGS", "FRK", "FRKS", "FT", "FRT", "FWY", "FRWY", 
		"GDN", "GRDN", "GDNS", "GRDNS", "GTWY", "GLN", "GLNS", "GRN", "GRNS", "GRV", 
		"HBR", "HBRS", "HVN", "HTS", "HGTS", "HT", "HWY", "HWAY", "HIWY", "HL", "HLS", "HLLW", "HOLW", 
		"INLT", "IS", "ISS", "ISLE", "JCT", "JCTN", "JCTION", "JCTNS", "JCTS", "KY", "KYS", "KNL",
		"KNLS", "LK", "LKS", "LNDG", "LN", /*"LANE",*/ "LGT", "LGTS", "LF", "LCK", "LCKS", "LDG",
		"LP", /*"LOOP", "MALL",*/ "MNR", "MNRS", "MDW", "MDWS", /*"MEWS",*/ "ML", "MLS", "MSN", "MTWY",
		"MT", "MNT", "MTN", "MTNS", "MNTNS", "NCK", "ORCH", /*"OVAL",*/ "PK", "PRK", /*"PARK",*/ "PKWY",
		"PKY", "PKWAY", "PKWYS", /*"PATH", "PIKE", */ "PNE", "PNES", "PL", "PLN", "PLNS", "PLZ",
		"PLZA", "PT", "PTS", "PRT", "PRTS", "PR", "PRR", "RADL", "RAD", /*"RAMP", */ "RNCH", 
		"RPD", "RPDS", "RST", "RDG", "RDGE", "RDGS", "RIV", "RVR", "RIVR", "RD", "ROAD", "RDS", 
		"RUE", "SHL", "SHLS", "SHR", "SHRS", "SKWY", "SPG", "SPGS", "SPNG", "SPRNG", "SQ", "SQR",
		"SQRE", "SQRS", "SQS", "STA", "STN", "STATN", "STRA", "STRVN", "STRVNUE", "STRM", 
		"ST", "STR", "STRT", "STS", "SMT", "TRWY", "TER", "TERR", "TRAK", "TRK", "TRKS", "TRFY",
		"TR", "TRL", "TRLS", "TUNL", "TUNLS", "TPK", "TPKE", "TRNPK", "TRPK", "UPAS", "UN", "UN",
		"UNS", "VLY", "VLLY", "VLYS", "VDCT", "VIA", "VW", "VWS", "VLG", "VL", "VIS", "WAY", "WAYS",
		"WL", "WLS"};
	private static String addressLineToStreetNum(String[] addressSplit) {
		return addressSplit[0];
	}
	private static String addressLineToStreetDir(String[] addressSplit) {
		if(addressSplit.length < 3) // "1242 Evergreen" would be tossed, "1242 Evergreen Tr" would be investigated (and rejected.)
			return "";
		String streetDirCandidate = addressSplit[1];
		if(streetDirCandidate.length() < 3) {
			for(int j = 0; j < streetDirs.length; j++) {
				if(streetDirCandidate.equalsIgnoreCase(streetDirs[j]))
					return streetDirCandidate;
			}
		}
		return "";
	}
	private static String addressLineToStreetSuffix(String[] addressSplit) {
		if(addressSplit.length < 3) // "1242 Main" would be tossed, "1242 W Main" would be investigated (and rejected.)
			return "";
		
		try {
			String streetSuffixCandidate = addressSplit[addressSplit.length -1];
			if(streetSuffixCandidate.length() < 6) {
				for(int j = 0; j < streetSuffixes.length; j++) {
					if(streetSuffixCandidate.equalsIgnoreCase(streetSuffixes[j]))
						return streetSuffixCandidate;
				}
			}
		} catch(IndexOutOfBoundsException e) {}
		return "";
	}
	
	public static final int STREET_NUM = 0;
	public static final int STREET_DIR = 1;
	public static final int STREET_NAME = 2;
	public static final int STREET_SUFFIX = 3;
	/**
	 * This takes an unformatted address line, and returns an array with four elements.
	 * @param addressLine
	 * @return An array of strings with four elements: the first is the street number, the second
	 * is the street direction, the third is the street name, and the last is the street suffix.
	 */
	public static String[] addressLineSplitter(String addressLine) {
		String[] addressSplit = Utils.splitBySpaces(addressLine);
		
		String streetNum = addressLineToStreetNum(addressSplit);
		String streetDir = addressLineToStreetDir(addressSplit);
		String streetSuffix = addressLineToStreetSuffix(addressSplit);
		
		String streetName = "";
		for(int i = 1; i < addressSplit.length; i++) {
			String word = addressSplit[i];
			if(i == 1 && !streetDir.equals("")) {
				continue;
			} else if(i == addressSplit.length -1 && !streetSuffix.equals("")) {
				continue;
			}
			streetName += word + " ";
		}
		String[] result = {streetNum, streetDir, streetName.trim(), streetSuffix};
		return result;
	}
	
	/**
	 * Copy one vector into another.
	 * @param vector1
	 * @param vector2
	 */
	public static final void vectorIntoVector(Vector vector1, Vector vector2) {
		for(int i = 0; i < vector1.size(); i++) {
			vector2.addElement(vector1.elementAt(i));
		}
	}
	
	/**
	 * Copy an array into a vector.
	 * @param array
	 * @param vector
	 */
	public static final void arrayIntoVector(Object[] array, Vector vector) {
		for(int i = 0; i < array.length; i++) {
			vector.addElement(array[i]);
		}
	}
	
	/**
	 * Copy one Hashtable into another. Preexisting keys in hashtable2 will be overwritten.
	 * @param hashtable1
	 * @param hashtable2
	 */
	public static final void hashtableIntoHashtable(Hashtable hashtable1, Hashtable hashtable2) {
		Enumeration keys = hashtable1.keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = hashtable1.get(key);
			hashtable2.put(key, value);
		}
	}
	
	/**
	 * Attempt to take a string and turn it into a float.  Returns null
	 * if there are not numbers at all in the string; cuts off after the first non-digit if it has already
	 * reached a decimal.
	 * @param string
	 * @return
	 */
	public static final Float stringToFloat(String string) {
		if(string == null) return null;
		String output = "";
		boolean foundDecimal = false;
		for(int i = 0; i < string.length(); i++) {
			char character = string.charAt(i);
			if(character == '.') {
				foundDecimal = true;
				output += character;
			} else if(character > 47 && character < 58) { // is a digit
				output += character;
			} else if(foundDecimal == true) { // break if we've previously found a decimal and this is not a digit.
				break;
			}
		}
		try {
			return Float.parseFloat(output);
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
