/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package net.microscraper.client.deprecated;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.CookieStoreInterface;
import net.microscraper.client.Utils;
import net.microscraper.client.interfaces.LogInterface;

/**
 * Abstract information store.  Implementations must also execute gatherers.
 * @author john
 *
 */
public class Information {
	public final String namespace;
	public final String type;
	
	/**
	 * An incremented integer ID within the Information's type.
	 */
	public final int id;
	
	private final LogInterface logger;
	
	/**
	 * Number of gatherers added.
	 */
	public int numGatherers = 0;
	
	/**
	 * Publisher to publish information.
	 */
	private final Publisher publisher;
	
	/**
	 * A cookie store associated with this information.
	 */
	private final CookieStoreInterface cookieStore;
	
	private final Hashtable fields = new Hashtable();
	//private final Hashtable allAttemptedFields = new Hashtable();
	private final Hashtable childInformationVectors = new Hashtable();
	public final String[] fieldsToPublish;
	private final Vector interpreters;
	public final Vector gatherers;
	
	private final Collector collector;
	
	public Information(Collector collect, String ns, String t, int n_id,
			String[] n_fieldsToPublish,
			Interpreter[] n_interpreters, Gatherer[] n_gatherers,
			Publisher p, CookieStoreInterface c, LogInterface l) {
		collector = collect;
		id = n_id;
		namespace = ns;
		if(n_fieldsToPublish == null)
			fieldsToPublish = new String[0];
		else
			fieldsToPublish = n_fieldsToPublish;
		interpreters = new Vector(n_interpreters.length, 1);
		Utils.arrayIntoVector(n_interpreters, interpreters);
		gatherers = new Vector(n_gatherers.length, 1);
		Utils.arrayIntoVector(n_gatherers, gatherers);
		type = t;
		publisher = p;
		logger = l;
		cookieStore = c;
	}
	/**
	 * Collect information.  Subclasses can modify this to make it asynchronous.
	 * @param publishAfter publishes child after collection is finished.
	 */
	public void collect(boolean publishAfter) throws InterruptedException {
		interpret();
		collector.collect(this);
		if(publishAfter == true)
			publish();
	}
	
	/**
	 * Collect information, and then recursively collect Information children.
	 * @param publishAfter publishes each child after collection is finished.
	 */
	public void collectAll(boolean publishAfter) throws InterruptedException {
		collect(publishAfter);
		Information[] children = children();
		for(int i = 0; i < children.length; i++) {
			children[i].collect(publishAfter);
			children[i].collectChildren(publishAfter);
		}
	}
	
	/**
	 * Collects only the children, but does it recursively.
	 * @param publishAfter publishes the child after collection is finished.
	 * @throws InterruptedException
	 */
	public void collectChildren(boolean publishAfter) throws InterruptedException {
		Information[] children = children();
		for(int i = 0; i < children.length; i++) {
			children[i].collectAll(publishAfter);
		}
	}
	
	/**
	 * Interpret this information's fields with patterns.  Called internally by Collectors, too.
	 */
	public void interpret() {
		int interpretersLeft = interpreters.size();
		int prevInterpretersLeft;
		do {
			prevInterpretersLeft = interpretersLeft;
			int i;
			for(i = 0; i < interpreters.size(); i++) {
				Interpreter interpreter = (Interpreter) interpreters.elementAt(i);
				if(interpreter.read(this) != null) {
					interpreters.removeElementAt(i);
					i--;
				}
			}
			interpretersLeft = interpreters.size();
		} while(interpretersLeft != prevInterpretersLeft);
	}
	
	/**
	 * GetField() should be used to populate the fields of a Gatherer, thus strings only.
	 * @param attributeName
	 * @return
	 */
	public String getField(String fieldName) {
		return (String) fields.get(fieldName);
	}
	/*
	public Information[] getInformations(String informationType) {		
		Vector childInformationsVector = (Vector) childInformationVectors.get(informationType);
		if(childInformationsVector == null)
			return null;
		Information[] childInformations = new Information[childInformationsVector.size()];
		for(int i = 0 ; i < childInformations.length; i++) {
			childInformations[i] = (Information) childInformationsVector.elementAt(i);
		}
		return childInformations;
	}*/
	
	/**
	 * putField() can be used to populate any kind of Gatherer field, including one that
	 * may not interface directly with a Gatherer.  Can therefore take any Object value. 
	 * Returns true if anything changed.
	 */
	public void putField(String fieldName, Object fieldValue) {
		try {
		//	allAttemptedFields.put(fieldName, true);
			try {
				fields.put(fieldName, fieldValue);
			} catch(NullPointerException e) {
				logger.e("Field " + fieldName + " was not put in Information " + type + " because it is null.",e);
			}
		} catch(NullPointerException e) {
			logger.e("Attempted to add a field with a null name in Information " + type + "; skipping.",e);
		}
	}
	
	/**
	 * A bulk version of PutField().
	 */
	public void putFields(Hashtable newFields) {
		Utils.hashtableIntoHashtable(newFields, fields);
	}
	
	/**
	 * Get all the possible field names -- even those that could be null will be returned.
	 */
	/*
	public String[] fieldNames() {
		Enumeration fieldsEnum = allAttemptedFields.keys();
		String[] fieldNames = new String[allAttemptedFields.size()];
		int i = 0;
		while(fieldsEnum.hasMoreElements()) {
			fieldNames[i] = (String) fieldsEnum.nextElement();
			i++;
		}
		return fieldNames;
	}
	*/
	/**
	 * Get all children of all types.
	 */
	public Information[] children() {
		Vector allChildrenVector = new Vector();
		Enumeration keys = childInformationVectors.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Vector childInformations = (Vector) childInformationVectors.get(key);
			for(int i = 0; i < childInformations.size(); i++) {
				Information childInformation = (Information) childInformations.elementAt(i);
				allChildrenVector.add(childInformation);
			}
		}
		Information[] children = new Information[allChildrenVector.size()];
		allChildrenVector.copyInto(children);
		return children;
	}
	
	/**
	 * Get children of one type.  Returns null if that type does not exist.
	 * 
	 * @param namespace The namespace to use.  Probably want to use the same namespace as the parent, but this is not guaranteed!
	 * @param type The type of child to get (sans namespace.)
	 * 
	 * @throws NullPointerException if namespace or type are null.
	 */
	/*public Information[] children(String namespace, String type) throws NullPointerException {
		if(namespace == null || type == null)
			throw new NullPointerException("Null namespace or type.");
		Vector childrenVector = (Vector) childInformationVectors.get(type);
		if(childrenVector == null)
			return null;
		Information[] children = new Information[childrenVector.size()];
		childrenVector.copyInto(children);
		return children;
	}*/
	/**
	 * Get children without a namespace.
	 * @param type
	 * @return
	 * @throws NullPointerException
	 */
	/*public Information[] children(String type) throws NullPointerException {
		return children("", type);
	}*/
	
	/**
	 * Publish our information while in progress.
	 */
	public void publishProgress(int gatherersFinished) {
		publishToLog();
		
		publisher.publishProgress(this, gatherersFinished, numGatherers);
	}
	
	/**
	 * Publish our information, should be done when we're happy with its internal state (this may write a row to a DB).
	 */
	public void publish() {
		publishToLog();

		publisher.publish(this);
	}
	
	/**
	 * Publish this information and all its children.
	 */
	public void publishAll() {
		publish();
		Information[] children = children();
		for(int i = 0; i < children.length; i++) {
			children[i].publishAll();
		}
	}
	
	/**
	 * Keep a log of the publishing process.
	 */
	public void publishToLog() {
		logger.i("Publishing information " + type + " ID " + id + " with values: ");
		Enumeration keys = fields.keys();
		
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) fields.get(key);
			logger.i("\t\t" + key + ": " + value);
		}
		
		keys = childInformationVectors.keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Vector childInformations = (Vector) childInformationVectors.get(key);
			logger.i("\t\t" + key + ": " + childInformations.size() + " informations");			//}
		}
	}
	
	public CookieStoreInterface getCookieStore() {
		return cookieStore;
	}

	public void addChildInformations(String type, Information[] childInformations) {
		Vector childInformationVector = (Vector) childInformationVectors.get(type);
		
		if(childInformationVector == null) {
			childInformationVector = new Vector(childInformations.length, 1);
			childInformationVectors.put(type, childInformationVector);
		}
		Utils.arrayIntoVector(childInformations, childInformationVector);
	}

	public void removeField(String rawDataField) {
		fields.remove(rawDataField);
	}
	
	/**
	 * Get an array, composed by a single field across an array of informations.
	 * 
	 * @return null if informations or field is null.  Will return an array which may contain null elements otherwise.
	 */
	public static String[] getFieldArray(Information[] informations, String field) {
		if(informations == null || field == null)
			return null;
		String[] fieldArray = new String[informations.length];
		for(int i = 0; i < informations.length; i++) {
			
			fieldArray[i] = informations[i].getField(field);
		}
		return fieldArray;
	}
	/**
	 * Eliminate redundant information elements in an array along a certain set of fields.
	 * Must be absolute equality after a trim() to trigger redundancy.
	 * Also eliminates any fields with a null value for that field, and any where a trim() yields "".
	 * 
	 * @throws NullPointerException if fields or informations is null.
	 */
	public static Information[] eliminateRedundancyAlong(String[] fields, Information[] informations, LogInterface l) {
		if(fields == null || informations == null)
			throw new NullPointerException();
		Hashtable fieldsHash = new Hashtable(fields.length, 1);
		for(int i = 0; i < fields.length; i++) {
			if(fields[i] != null)
				fieldsHash.put(fields[i], "");
		}
		Vector informationsVector = new Vector(informations.length, 1);
		Utils.arrayIntoVector(informations, informationsVector);
		for(int i = 0; i < informationsVector.size(); i++) {
			Enumeration fieldNames = fieldsHash.keys();
			// Allow us to break the first loop.
			boolean invalidOuter = false;
			while(fieldNames.hasMoreElements()) {
				String fieldName = (String) fieldNames.nextElement();
				String value = informations[i].getField(fieldName);
				if(value == null) {
					invalidOuter = true;
					l.i("outer value for " + fieldName + " is null.");
					break;
				}
				value = value.trim();
				if(value.equals("")) {
					l.i("outer value for " + fieldName + " is blank.");
					invalidOuter = true;
						break;
				}
				fieldsHash.put(fieldName, value);
			}
			if(invalidOuter == true) {
				informationsVector.removeElementAt(i);
				i--;
			} else {
				for(int j = i + 1; j < informationsVector.size(); j++) {
					fieldNames = fieldsHash.keys();
					boolean invalidInner = false;
					while(fieldNames.hasMoreElements()) {
						String fieldName = (String) fieldNames.nextElement();
						String value = (String) fieldsHash.get(fieldName);
						String compValue = informations[j].getField(fieldName);
						if(compValue == null) {
							invalidInner = true;
							l.i("compValue against " + value + " in field " + fieldName + " is null.");
							break;
						}
						compValue = compValue.trim();
						if(compValue.equals("") || compValue.equals(value)) {
							invalidInner = true;
							l.i("compValue  " + compValue + " equals " + value + " for field " + fieldName);
							break;
						}
					}
					if(invalidInner == true) {
						informationsVector.removeElementAt(j);
						j--;
					}
				}
			}
		}
		Information[] results = new Information[informationsVector.size()];
		informationsVector.copyInto(results);
		return results;

	}
	
	/**
	 * Log an array of Informations.
	 */
	public static void arrayPublishToLog(Information[] informations) {
		if(informations == null)
			return;
		if(informations.length == 0)
			return;
		for(int i = 0 ; i < informations.length; i++) {
			informations[i].publishToLog();
		}
	}
}
