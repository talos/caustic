/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import net.caustic.Response;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Used to process the "description" key of a Find.
 * @author talos
 *
 */
final class FindDescription {
	private final int visibility;
	
	private static final String VISIBILITY = "visibility";
	private static final String PUBLIC = "public";
	
	public static final int INTERNAL = 0x01;
	public static final int EXTERNAL = 0x02;
	
	FindDescription(Response.DoneFind response) {
		int vis;
		try {
			JSONObject obj = new JSONObject(response.getDescription());
			String visStr = obj.getString(VISIBILITY);
			if(visStr.equals(PUBLIC)) {
				vis = EXTERNAL + INTERNAL;
			} else {
				vis = INTERNAL;
			}
			
		} catch(JSONException e) {
			vis = INTERNAL;
		}
		visibility = vis;
	}
	
	public boolean isExternal() {
		return isExternal(visibility) > 0;
	}
	
	public boolean isInternal() {
		return isInternal(visibility) > 0;
	}
	
	/**
	 * 
	 * @param flags
	 * @return <code>1</code> if the flag permits internal visibility, <code>0</code> otherwise.
	 */
	public static int isInternal(int flags) {
		return (flags & INTERNAL) > 1 ? 1 : 0;
	}

	/**
	 * 
	 * @param flags
	 * @return <code>1</code> if the flag permits external visibility, <code>0</code> otherwise.
	 */
	public static int isExternal(int flags) {
		return (flags & EXTERNAL) > 1 ? 1 : 0;
	}
}
