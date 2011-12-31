/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import net.caustic.android.ServerPinger;

/* * 
 * @author talos
 *
 */
public interface ServerPingerListener {

	/**
	 * Called when {@link ServerPinger} gets a successful response.
	 * @return <code>true</code> to keep making requests, <code>
	 * false</code> to stop making requests.
	 */
	abstract boolean onAlive();
	
	/**
	 * Called when {@link ServerPinger} does not get a successful
	 * response.
	 * @return <code>true</code> to stop making requests, <code>
	 * false</code> to keep making requests.
	 */
	abstract boolean onDead();
}
