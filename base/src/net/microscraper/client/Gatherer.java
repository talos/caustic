/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package net.microscraper.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class generates a gatherer from gatherer input and output tables.
 * Hashtable and Vector are used instead of Map and List in order to make this
 * comply with Java 1.3.
 * 
 * Must implement attributesToStream to function.
 * 
 * @author john
 *
 */
public final class Gatherer {
	/**
	 * A not-necessarily-unique GathererId.
	 */
	public final String id;
	
	/**
	 * Multiple URLs means multiple requests.
	 */
	//private final Vector thisDefaultUrls = new Vector();
	//private final Vector thisFieldUrls = new Vector();
	private final Vector thisUrls = new Vector();
	
	/**
	 * Converted to NameValuePair on the fly, preventing duplicates.
	 */
	//private final Hashtable thisDefaultGets = new Hashtable();
	//private final Hashtable thisFieldGets = new Hashtable();
	private final Hashtable thisGets = new Hashtable();
	
	/**
	 * Converted to NameValuePair on the fly, preventing duplicates.
	 */
	//private final Hashtable thisDefaultPosts = new Hashtable();
	//private final Hashtable thisFieldPosts = new Hashtable();
	private final Hashtable thisPosts = new Hashtable();
	
	/**
	 * These cookies values are converted into cookies when we ask for the OutputStream.
	 */
	//private final Vector thisDefaultCookieNames = new Vector();
	//private final Vector thisDefaultCookieValues = new Vector();
	//private final Vector thisFieldCookieNames = new Vector();
	//private final Vector thisFieldCookieValues = new Vector();
	private final Vector thisCookieNames = new Vector();
	private final Vector thisCookieValues = new Vector();
	
	/**
	 * This does not allow for duplicate headers.
	 */
	//private final Hashtable thisDefaultHeaders = new Hashtable();
	//private final Hashtable thisFieldHeaders = new Hashtable();
	private final Hashtable thisHeaders = new Hashtable();
	
	/**
	 * Add parent gatherers.  Stored as a Vector of Gatherers.  All inputs and outputs from
	 * parents will be handled as if they had been in this Gatherer, including throwing of
	 * InsufficientInformationException.
	 */
	private final Vector parentGatherers = new Vector();
	
	/**
	 * Vector with patterns that will make this Gatherer terminate.
	 */
	private final Vector thisTerminatorPatterns = new Vector();
	
	/**
	 * Buffer size.
	 */
	private static final int bufferSize = 512;
	
	/**
	 * Buffer fills before running Readables.
	 */
	private static final int bufferFillsForRead = 16;
	
	/*
	 * Default header values.
	*/
	private static final String USER_AGENT_HEADER_NAME = "User-Agent";
	private static final String DEFAULT_USER_AGENT_HEADER_VALUE = "Mozilla/4.0 (compatible; MSIE 6.1; Windows XP)";
	private static final String ACCEPT_HEADER_NAME = "Accept";
	private static final String DEFAULT_ACCEPT_HEADER_VALUE = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	private static final String REFERER_HEADER_NAME = "Referer";
	
	/**
	 * The GeograpeCookieStore always used for gathering this information.
	 */
	//private final GeograpeCookieStore cookieStore;
	
	/**
	 * The GeograpeContext used to make requests.
	 */
	private final HttpInterface httpInterface;
		
	private final LogInterface logger;
	/**
	 * Create a new Gatherer.  Attributes are added later.
	 * @param ns The Gatherer's namespace.
	 * @param gId The Gatherer's ID.
	 * @param hi The HttpInterface, used to make requests.
	 * @param l The LogInterface, used for logging.
	 */
	public Gatherer(String gId, HttpInterface hi, RegexInterface ri, LogInterface l) {
		if(gId == null || hi == null || l == null) throw new NullPointerException("No nulls allowed in Gatherer constructor.");
		id = gId;
		httpInterface = hi;
		logger = l;
	}
	
	public void addUrl(String value) {
		if(value == null) value = "";
		thisUrls.addElement(value);
	}
	
	public void addGet(String name, String value) {
		if(value == null) value = "";
		thisGets.put(name, value);
	}
	
	public void addPost(String name, String value) {
		if(value == null) value = "";
		thisPosts.put(name, value);
	}
	
	public void addCookie(String name, String value) {
		if(value == null) value = "";
		thisCookieNames.addElement(name);
		thisCookieValues.addElement(value);
	}
	
	public void addHeader(String name, String value)  {
		if(value == null) value = "";
		thisHeaders.put(name, value);
	}
	
	/**
	 * Add a parent gatherer that must also be populated before this can run.
	 * @param parentGatherer
	 */
	public void addParentGatherer(Gatherer parentGatherer) {
		if(parentGatherer == null)
			throw new NullPointerException("Attempted to add NULL parentGatherer.");
		parentGatherers.addElement(parentGatherer);
	}
	
	/**
	 * Add readable Ids for readables that terminate the request.
	 * @param terminatorReadableId
	 */
	public void addTerminator(PatternInterface terminator) {
		if(terminator == null)
			throw new NullPointerException("Attempted to add NULL terminatorPattern.");
		thisTerminatorPatterns.addElement(terminator);
	}
	
	/**
	 * Return a new Hashtable with values populated by populateValue().
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws InsufficientInformationException 
	 */
	private Hashtable populate(Hashtable table, Information information) throws InsufficientInformationException, UnsupportedEncodingException {
		Hashtable populatedTable = new Hashtable(table.size(), 1);
		
		Enumeration keys = table.keys();
		
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) table.get(key);
			if(value == null) {
				throw new InsufficientInformationException(key);
			}
			populatedTable.put(key, populateValue(value, information));
		}

		return populatedTable;
	}
	
	/**
	 * Copy the loaded attributes into temporary, populate-able memory for execution.
	 * @param defaultUrls
	 * @param fieldUrls
	 * @param defaultGets
	 * @param fieldGets
	 * @param defaultPosts
	 * @param fieldPosts
	 * @param defaultHeaders
	 * @param fieldHeaders
	 * @param defaultCookieNames
	 * @param defaultCookieValues
	 * @param fieldCookieNames
	 * @param fieldCookieValues
	 */
	private final void copyAttributesInto(Vector urls, Hashtable gets,
			Hashtable posts, Hashtable headers, Vector cookieNames,
			Vector cookieValues, Hashtable fieldReaders,
			Vector terminatorPatterns) {
		
		for(int i = 0; i < parentGatherers.size(); i++) {
			Gatherer parentGatherer = (Gatherer) parentGatherers.elementAt(i);
			
			parentGatherer.copyAttributesInto(urls, gets,
					posts, headers, cookieNames, cookieValues,
					fieldReaders, terminatorPatterns);
		}
		
		Utils.vectorIntoVector(thisUrls, urls);
		Utils.hashtableIntoHashtable(thisGets, gets);
		Utils.hashtableIntoHashtable(thisPosts, posts);
		Utils.hashtableIntoHashtable(thisHeaders, headers);
		Utils.vectorIntoVector(thisCookieNames, cookieNames);
		Utils.vectorIntoVector(thisCookieValues, cookieValues);
		Utils.vectorIntoVector(thisTerminatorPatterns, terminatorPatterns);
	}
	
	/**
	 * Execute a Gatherer within the context of an information.
	 * @param information The information used to populate this gatherer.  Also used as a callback to update
	 * @param preview If true, the raw HTML of the request will be logged.
	 * fields.
	 * @return
	 * @throws InsufficientInformationException if the attributes for this object are not fully set.
	 * @throws IOException if there is an error getting an output stream.
	 */
	public final void execute(Information information, boolean preview) throws InsufficientInformationException, IOException {
		logger.i("Executing gatherer '" + id + "'");

		// Allocating memory for execution
		Vector urls = new Vector();
		Hashtable gets = new Hashtable();
		Hashtable posts = new Hashtable();
		Hashtable headers = new Hashtable();
		Vector cookieNames = new Vector();
		Vector cookieValues = new Vector();
		Hashtable fieldReaders = new Hashtable();
		Vector terminatorPatterns = new Vector();
				
		// Not called with pregathered data, must collect.
		CookieStoreInterface cookieStore = information.getCookieStore();

		// Copy in parent attributes.
		
		copyAttributesInto(urls, gets, posts, headers, cookieNames,
				cookieValues, fieldReaders, terminatorPatterns);

		// Populate URLs.
		Vector populatedUrls = new Vector();
		for(int i = 0; i < urls.size(); i++) {
			// Encodes the value for transmission as a url.
			populatedUrls.addElement(populateValue((String) urls.elementAt(i), information, "UTF-8"));
		}
		
		// Populate tables.
		Hashtable populatedGets = populate(gets, information);
		Hashtable populatedPosts = populate(posts, information);
		Hashtable populatedHeaders = populate(headers, information);

		// Add required default header values.
		if(!populatedHeaders.containsKey(USER_AGENT_HEADER_NAME))
			populatedHeaders.put(USER_AGENT_HEADER_NAME, DEFAULT_USER_AGENT_HEADER_VALUE);
		if(!populatedHeaders.containsKey(ACCEPT_HEADER_NAME))
			populatedHeaders.put(ACCEPT_HEADER_NAME, DEFAULT_ACCEPT_HEADER_VALUE);
		
		if(populatedUrls.size() == 0) {
			logger.i("Gatherer '" + id + "' + has no populated URLs.");
		}
		// Process each URL separately.
		for(int i = 0; i < populatedUrls.size(); i++) {
			String url = (String) populatedUrls.elementAt(i);
			
			CookieInterface[] populatedCookies = new CookieInterface[cookieNames.size()];
			for(int j = 0; j < cookieNames.size(); j++) {
				String name = (String) cookieNames.elementAt(j);
				String value = populateValue((String) cookieValues.elementAt(j), information);
				populatedCookies[j] = new CookieInterface.ScraperCookie(url, name, value);
			}
			
			if(!populatedHeaders.containsKey(REFERER_HEADER_NAME))
				populatedHeaders.put(REFERER_HEADER_NAME, url);
			
			byte[] sBuffer = new byte[bufferSize];
			
			logger.i("Requesting " + url + " with ");
			logger.i("Gets: " + populatedGets.toString());
			logger.i("Posts: " + populatedPosts.toString());
			logger.i(populatedCookies.length + " special Cookies: ");
			logger.i("CookieStore cookies: " + cookieStore.toString());
			for(int j = 0; j < populatedCookies.length; j++) {
				logger.i("\t\t" + populatedCookies[j].toString());
			}
			logger.i("Headers: " + populatedHeaders.toString());
			
			EntityInterface entity = httpInterface.attributesToEntity(url, cookieStore, populatedGets, populatedPosts, populatedCookies, populatedHeaders);
			
			InputStream inputStream = entity.getInputStream();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
	        
			// Read response into a buffered stream.
			// shared buffer should ensure this is synchronized.
			int readBytes = 0;
			int bufferFills = 0;
			String data;
			logger.i("Starting download of gatherer '" + id + "'");
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				if(preview == true)
					logger.i(new String(sBuffer));
				
				content.write(sBuffer, 0, readBytes);
				bufferFills++;
				
				// Check whether we should terminate when we reach this number of bufferFills.
				if(bufferFills >= bufferFillsForRead) {
					bufferFills = 0;
					data = new String(content.toByteArray());
					boolean terminate = false;
					for(int j = 0; j < terminatorPatterns.size(); j++) {
						PatternInterface terminatorPattern = (PatternInterface) terminatorPatterns.elementAt(j);
						if(terminatorPattern.matches(data) != false) {
							logger.i("Terminating gatherer " + id + " due to pattern " + terminatorPattern.toString());
							terminate = true;
						}
					}
					
					// If we have a true terminator, break into the actual reading section.
					if(terminate == true) {
						entity.consumeContent();
						break;
					}
				}
			}
			String rawDataField = "gatherer." + this.id;
			information.putField(rawDataField, content.toString());
						
			information.interpret();
			
			information.removeField(rawDataField);
		}
	}
	
	private final String populateValue(String input, Information information) throws InsufficientInformationException, UnsupportedEncodingException {
		return populateValue(input, information, null);
	}
	
	/**
	 * Take a value and substitute any {{variable}}.  This does not currently provide
	 * an escape mechanism.
	 * @param input
	 * @param information
	 * @param an encoding, leave null if none is desired.
	 * @return
	 * @throws InsufficientInformationException
	 */
	private final String populateValue(String input, Information information, String encoding) throws InsufficientInformationException, UnsupportedEncodingException {
		String output = "";
		// Scan for each "start of" reader ($R{)
		
		int index = -1;
		int prevIndex;
		boolean insideBrackets = false;
		do {
			prevIndex = index;
			if(insideBrackets == true) {
				index = input.indexOf("}}", prevIndex + 1);
				if(index == -1)
					throw new IndexOutOfBoundsException("Could not find end brackets for value.");
				String fieldName = input.substring(prevIndex + 3, index);
				String value = information.getField(fieldName);
				if(value == null) 
					throw new InsufficientInformationException(fieldName);
				if(encoding != null)
					value = URLEncoder.encode(value, encoding);
				logger.i("value: " + value);
				output += value;
				insideBrackets = false;

			} else if(insideBrackets == false) {
				index = input.indexOf("{{", prevIndex + 1);
				if(index == -1) {
					output += input.substring(prevIndex + 1);
				} else {
					output += input.substring(prevIndex + 1, index);
					insideBrackets = true;
				}
			}
		} while(index != -1);

		return output;
		/**
		 * Thrown if we try to execute a gatherer with an information that lacks a necessary field.
		 * @author john
		 *
		 */
	}
	
	/**
	 * Determine if a character is escaped by a backslash.  Takes into account the fact
	 * that a backslash, too, can be escaped.
	 * @param string
	 * @param charNum
	 * @return
	 */
	/*private static boolean isEscaped(String string, int charNum) {
		boolean isEscaped = false;
		while(charNum > 0) {
			if(string.charAt(charNum - 1) == '\\') {
				if(isEscaped == false) isEscaped = true;
				if(isEscaped == true) isEscaped = false;
				charNum--;
			} else { break; }
		}
		return isEscaped;
	}*/
	

	public static class InsufficientInformationException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6648522327721677231L;
		
		
		/**
		 * The name of the field that could not be found in information.
		 */
		public final String fieldName;
		public InsufficientInformationException(String name) {
			super("Caused by unpopulated '" + name + "'");
			fieldName = name;
		}
	}
}
