package net.caustic.android.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public abstract class CausticServiceIntent {
	
	static final String REFRESH_INTENT  = "net.caustic.android.service.REFRESH";
	static final String REQUEST_INTENT  = "net.caustic.android.service.REQUEST";
	static final String FORCE_INTENT    = "net.caustic.android.service.FORCE";
	static final String RESPONSE_INTENT = "net.caustic.android.service.RESPONSE";
	
	public static final String SCHEME = "caustic";
	
	private static Uri uri(String id) {
		return Uri.fromParts(SCHEME, id, null);
	}
	
	private static Bundle mapToBundle(Map<String, String> map) {
		Bundle bundle = new Bundle();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			bundle.putString(entry.getKey(), entry.getValue());
		}
		return bundle;
	}
	
	private static Map<String, String> bundleToMap(Bundle bundle) {
		Map<String, String> map = new HashMap<String, String>();
		for(String key : bundle.keySet()) {
			map.put(key, bundle.getString(key));
		}
		return map;
	}
	
	private final String scope;
	
	private CausticServiceIntent(Intent intent) {
		scope = intent.getData().getSchemeSpecificPart();
	}
	
	public final String getScope() {
		return scope;
	}
	
	public static class CausticRequestIntent extends CausticServiceIntent {
		private static final String INSTRUCTION = "instruction";
		private static final String URI = "uri";
		private static final String FORCE = "force";
		private static final String TAGS = "tags";
		
		private final String instruction;
		private final String uri;
		private final boolean force;
		private final Map<String, String> tags = new HashMap<String, String>();

		public static Intent newRequest(String id, String instruction, String uri,
				Map<String, String> tags,
				String input, boolean force) {
			Bundle tagsBundle = new Bundle(tags.size());
			for(Map.Entry<String, String> entry : tags.entrySet()) {
				tagsBundle.putString(entry.getKey(), entry.getValue());
			}
			return new Intent(REQUEST_INTENT, uri(id))
				.putExtra(INSTRUCTION, instruction)
				.putExtra(URI, uri)
				.putExtra(TAGS, tagsBundle)
				.putExtra(FORCE, force);
				//.setClassName("net.caustic.android.service", "CausticService");
		}

		CausticRequestIntent(Intent intent) {
			super(intent);
			instruction = intent.getStringExtra(INSTRUCTION);
			uri = intent.getStringExtra(URI);
			force = intent.getBooleanExtra(FORCE, false);
			Bundle tagsBundle = intent.getBundleExtra(TAGS);
			Set<String> keys = tagsBundle.keySet();
			for(String key : keys) {
				tags.put(key, tagsBundle.getString(key));
			}
		}
		
		String getInstruction() {
			return instruction;
		}
		
		String getURI() {
			return uri;
		}
		
		boolean getForce() {
			return force;
		}
		
		Map<String, String> getTags() {
			return tags;
		}
	}
	
	public static class CausticResponseIntent extends CausticServiceIntent {

		static Intent newResponse(String scope, Map<String, String> data,
				Map<String, String> waits,
				Map<String, Map<String, String>> children) {
			Bundle dataBundle = mapToBundle(data);
			Bundle waitsBundle = mapToBundle(waits);
			
			Bundle childrenBundle = new Bundle(children.size());
			for(Map.Entry<String, Map<String, String>> entry : children.entrySet()) {
				childrenBundle.putBundle(entry.getKey(), mapToBundle(entry.getValue()));
			}
			
			return new Intent(RESPONSE_INTENT, uri(scope))
					.putExtra(DATA, dataBundle)
					.putExtra(WAITS, waitsBundle)
					.putExtra(CHILDREN, childrenBundle);
		}
		
		private static final String DATA = "data";
		private static final String WAITS = "waits";
		private static final String CHILDREN = "children";
		
		private final Map<String, String> data;
		private final Map<String, String> waits;
		private final Map<String, Map<String, String>> children = new HashMap<String, Map<String, String>>();

		public CausticResponseIntent(Intent intent) {
			super(intent);
			this.data = bundleToMap(intent.getBundleExtra(DATA));
			this.waits = bundleToMap(intent.getBundleExtra(WAITS));
			Bundle childrenBundle = intent.getBundleExtra(CHILDREN);
			for(String key : childrenBundle.keySet()) {
				children.put(key, bundleToMap(childrenBundle.getBundle(key)));
			}
		}
		
		public Map<String, String> getData() {
			return data;
		}
		
		public Map<String, String> getWaits() {
			return waits;
		}
		
		public Map<String, Map<String, String>> getChildren() {
			return children;
		}
	}
	
	public static class CausticForceIntent extends CausticServiceIntent {
		
		public static Intent newForce(String id) {
			return new Intent(FORCE_INTENT, uri(id));
		}
		
		CausticForceIntent(Intent intent) {
			super(intent);
		}
	}
	
	public static class CausticRefreshIntent extends CausticServiceIntent {
		
		public static Intent newRefresh(String scope) {
			return new Intent(REFRESH_INTENT, uri(scope));
		}
		
		CausticRefreshIntent(Intent intent) {
			super(intent);
		}
	}
}
