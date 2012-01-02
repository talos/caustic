/**
_ * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;


import net.caustic.Request;
import net.caustic.http.Cookies;
import net.caustic.http.HashtableCookies;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author talos
 *
 */
final class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "caustic";
	private static final int DATABASE_VERSION = 7;
	
	private static final String DATA = "data";
	private static final String RELATIONSHIPS = "relationships";
	private static final String COOKIES = "cookies";
	private static final String WAIT = "wait";
	private static final String RETRY = "retry";
	
	private static final String SOURCE = "source";
	private static final String SCOPE = "scope";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String INPUT = "input";
	private static final String MISSING_TAGS = "missing_tags";
	private static final String URI = "uri";
	private static final String INSTRUCTION = "instruction";
	private static final String COOKIE = "cookie";
	private static final String HOST = "host";
	//private static final String DESCRIPTION = "description";
	private static final String INTERNAL_VISIBILITY = "internal";
	private static final String EXTERNAL_VISIBILITY = "external";
	
	private SQLiteDatabase db;
	
	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.db = getWritableDatabase();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// relationship table
		db.execSQL("CREATE TABLE IF NOT EXISTS " + RELATIONSHIPS +
				" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SOURCE + " VARCHAR, " +
				SCOPE  + " VARCHAR, " +
				NAME   + " VARCHAR, " +
				VALUE  + " VARCHAR)");
		
		// data table
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DATA +
				" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SCOPE      + " VARCHAR, " +
				NAME       + " VARCHAR, " +
				VALUE      + " VARCHAR, " +
				INTERNAL_VISIBILITY + " INTEGER, " +
				EXTERNAL_VISIBILITY + " INTEGER)");
		
		// wait table
		db.execSQL("CREATE TABLE IF NOT EXISTS " + WAIT +
				" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SCOPE       + " VARCHAR, " +
				INSTRUCTION + " VARCHAR, " +
				NAME        + " VARCHAR, " +
				URI         + " VARCHAR)");

		// retry table
		db.execSQL("CREATE TABLE IF NOT EXISTS " + RETRY +
				" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SCOPE        + " VARCHAR, " +
				INSTRUCTION  + " VARCHAR, " +
				INPUT        + " VARCHAR, " +
				URI          + " VARCHAR, " +
				MISSING_TAGS + " VARCHAR)");

		// cookies (browser state) table
		db.execSQL("CREATE TABLE IF NOT EXISTS " + COOKIES +
				" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SCOPE  + " VARCHAR, " +
				HOST   + " VARCHAR, " +
				COOKIE + " VARCHAR)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.i(DATABASE_NAME, "upgrading, this throws away old db");
		db.execSQL("DROP TABLE IF EXISTS " + RELATIONSHIPS);
		db.execSQL("DROP TABLE IF EXISTS " + DATA);
		db.execSQL("DROP TABLE IF EXISTS " + WAIT);
		db.execSQL("DROP TABLE IF EXISTS " + RETRY);
		db.execSQL("DROP TABLE IF EXISTS " + COOKIES);
		onCreate(db); // re-create db
	}
	
	@Override
	public void close() {
		super.close();
		db.close();
	}
	
	/**
	 * Save an arbitrary tag for internal display only.
	 * @param scope
	 * @param name
	 * @param value
	 */
	void saveTag(String scope, String name, String value) {
		saveData(scope, name, value, false, false);
	}
	
	/**
	 * Save the result of a find.
	 * @param scope The {@link String} scope of the saved data.
	 * @param name The {@link String} name of the saved data.
	 * @param value The array of {@link String} values of the saved data.
	 * @param desc The {@link FindDescription} of the find.
	 */
	void saveFind(String scope, String name, String value, FindDescription desc) {
		saveData(scope, name, value, desc.isInternal(), desc.isExternal());
	}

	void saveRelationship(String scope, String source, String name, String value, FindDescription desc) {
		ContentValues cv = new ContentValues(2);
		cv.put(SOURCE, source);
		cv.put(SCOPE, scope);
		cv.put(NAME, name);
		cv.put(VALUE, value);
		db.insert(RELATIONSHIPS, null, cv);
	}
	
	void saveCookies(String scope, Cookies cookies) {
		ContentValues cv = new ContentValues(3);
		cv.put(SCOPE, scope);
		String[] hosts = cookies.getHosts();
		for(String host : hosts) {
			cv.put(HOST, host);
			String[] cookiesForHost = cookies.get(host);
			for(String cookie : cookiesForHost) {
				cv.put(COOKIE, cookie);
				db.insert(COOKIES, null, cv);
			}
		}
	}
	
	void saveWait(String scope, String instruction, String uri, String name) {
		ContentValues cv = new ContentValues(3);
		cv.put(SCOPE, scope);
		cv.put(INSTRUCTION, instruction);
		cv.put(URI, uri);
		cv.put(NAME, name);
		db.insert(WAIT, null, cv);
		
		//notifyListeners(scope);
	}
	
	void saveMissingTags(String scope, String instruction, String uri, String input, String[] missingTags) {

		ContentValues cv = new ContentValues(4);
		cv.put(SCOPE, scope);
		cv.put(INSTRUCTION, instruction);
		cv.put(URI, uri);
		cv.put(INPUT, input);
		cv.put(MISSING_TAGS, new JSONArray(Arrays.asList(missingTags)).toString()); // serialize missing tags via JSON
		db.insert(RETRY, null, cv);
	}
	
	/**
	 * The returned Wait request will have force enabled.
	 * @param scope
	 * @return A map of {@link Request}s keyed by name.
	 */
	Map<String, RequestBundle> getWait(String scope) {
		Cursor cursor = db.query(WAIT, new String[] { INSTRUCTION, URI, NAME }, 
				SCOPE + " = ?", new String[] { scope },
				null, null, null);
		
		//StringMap data = null; // only pull this if we need it
		Map<String, RequestBundle> waits = new HashMap<String, RequestBundle>(cursor.getCount(), 1);
		while(cursor.moveToNext()) {
			String instruction = cursor.getString(0);
			String uri = cursor.getString(1);
			String name = cursor.getString(2);
			
			// input is null
			/*if(data == null) {
				data = new CollectionStringMap(getData(scope, FindDescription.INTERNAL));
			}
			waits.put(name, new RequestBundle(scope, instruction, uri, null,
					data, getCookies(scope), true));*/
			waits.put(name, new RequestBundle(scope, instruction, uri, null, true));
		}
		cursor.close();
		return waits;
	}
	
	/**
	 * This will only return Requests that were missing tags that can now be executed.
	 * They will be removed from the database.  They will not be forced.
	 * @param scope
	 * @return
	 */
	List<Request> popMissingTags(String scope) {
		Cursor cursor = db.query(RETRY, new String[] { INSTRUCTION, URI, INPUT, MISSING_TAGS },
				SCOPE + " = ?", new String[] { scope },
				null, null, null);
		
		StringMap tags = new CollectionStringMap(getData(scope, FindDescription.INTERNAL));
		Cookies cookies = null; // these are lazily loaded in the event that there actually are requests
		List<Request> result = new ArrayList<Request>(cursor.getCount());
		while(cursor.moveToNext()) {
			String instruction = cursor.getString(0);
			String uri = cursor.getString(1);
			String input = cursor.getString(2);

			// only return requests that are no longer missing tags.
			boolean isReady = true;
			try {
				JSONArray missingTagsJSON = new JSONArray(cursor.getString(3));
				for(int i = 0 ; i < missingTagsJSON.length() ; i ++) {
					if(tags.get(missingTagsJSON.getString(i)) == null) {
						isReady = false;
						break;
					}
				}
			} catch(JSONException e) { // this shouldn't happen!!
				throw new RuntimeException("Invalid JSON in database", e);
			}
			
			if(isReady) {
				if(cookies == null) {
					cookies = getCookies(scope);
				}
				result.add(new Request(scope, instruction, uri, input, tags, cookies, false));
			}
		}
		cursor.close();
		
		return result;
	}
	
	/**
	 * 
	 * @param scope The {@link String} scope of the data to be returned,
	 * including parents.
	 * @param visibilityFlags The visibility of the data to get.  All data
	 * with at least one of these flags will be returned.
	 * @return A {@link Map} of {@link String} keys to {@link String} values.
	 * @see #EXTERNAL
	 * @see #INTERNAL
	 */
	Map<String, String> getData(String scope, int visibilityFlags) {
		Map<String, String> parentData = new HashMap<String, String>();
		Map<String, String> thisData = getDataInScope(scope, visibilityFlags);
		
		// loop through source scopes.
		while((scope = getSource(scope)) != null) {
			// This ensures that child keys overwrite parent keys.
			Map<String, String> interData = getDataInScope(scope, visibilityFlags);
			interData.putAll(parentData);
			parentData = interData;
		}
		
		parentData.putAll(thisData);
		return parentData;
	}
	
	/**
	 * 
	 * @param source
	 * @return A map of children ID : branch value maps, keyed by name.
	 */
	Map<String, Map<String, String>> getChildren(String source) {
		Cursor cursor = db.query(RELATIONSHIPS, new String[] { SCOPE, NAME, VALUE }, 
				SOURCE + " = ?", new String[] { source }, 
				null, null, null);
		
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		while(cursor.moveToNext()) {
			String scope = cursor.getString(0);
			String name = cursor.getString(1);
			String value = cursor.getString(2);
			final Map<String, String> child;
			
			// create list if it doesn't already exist in result
			if(!result.containsKey(name)) {
				child = new HashMap<String, String>();
				result.put(name, child);
			} else {
				child = result.get(name);
			}
			child.put(scope, value);
		}
		cursor.close();
		
		return result;
	}
	
	/**
	 * 
	 * @param scope The {@link String} scope whose source should be found.
	 * @return A {@link String} source scope, if one exists; <code>null</code> otherwise.
	 */
	String getSource(String scope) {
		Cursor cursor = db.query(RELATIONSHIPS, new String[] { SOURCE },
				SCOPE + " = ?", new String[] { scope },
				null, null, null);
		
		String source = cursor.moveToFirst() ? cursor.getString(0) : null;
		cursor.close();
		return source;
	}
	
	Cookies getCookies(String scope) {
		Cookies thisCookies = getCookiesInScope(scope);
		HashtableCookies parentCookies = new HashtableCookies();
		
		while((scope = getSource(scope)) != null) {
			parentCookies.extend(getCookiesInScope(scope));
		}
		
		parentCookies.extend(thisCookies);
		return parentCookies;
	}
	
	Cookies getCookiesInScope(String scope) {
		HashtableCookies cookies = new HashtableCookies();
		Cursor cursor = db.query(COOKIES, new String[] { HOST, COOKIE },
				SCOPE + " = ?", new String[] { scope },
				null, null, null);
		
		while(cursor.moveToNext()) {
			String host = cursor.getString(0);
			cookies.add(host, cursor.getString(1));
		}
		cursor.close();
		return cookies;
	}
	
	Map<String, String> getDataInScope(String scope, int visibilityFlags) {
		Map<String, String> data = new HashMap<String, String>();

		Cursor cursor = db.query(
				DATA, new String[] { NAME, VALUE },
				SCOPE + " = ? AND " + INTERNAL_VISIBILITY + " >= ? AND " + EXTERNAL_VISIBILITY + " >= ?",
				new String[] {
						scope,
						String.valueOf(FindDescription.isInternal(visibilityFlags)),
						String.valueOf(FindDescription.isExternal(visibilityFlags))
				},
				null, null, null);

		while(cursor.moveToNext()) {
			data.put(cursor.getString(0), cursor.getString(1));
		}
		cursor.close();
		
		return data;
	}
	
	private void saveData(String scope, String name, String value, boolean internal, boolean external) {
		ContentValues cv = new ContentValues(3);
		cv.put(SCOPE, scope);
		cv.put(NAME, name);
		cv.put(VALUE, value);
		cv.put(INTERNAL_VISIBILITY, false);
		cv.put(EXTERNAL_VISIBILITY, false);
		db.insert(DATA, null, cv);		
	}
}
