package net.caustic.android.service;

import android.content.IntentFilter;

/**
 * An {@link IntentFilter} that filters for Caustic scraper responses.
 * @author talos
 *
 */
public class CausticIntentFilter extends IntentFilter {

	public CausticIntentFilter() {
		super(CausticServiceIntent.RESPONSE_INTENT);
		addDataScheme(CausticServiceIntent.SCHEME);
	}
}
