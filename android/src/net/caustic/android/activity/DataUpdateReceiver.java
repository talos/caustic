package net.caustic.android.activity;

import net.caustic.android.service.CausticServiceIntent.CausticResponseIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This {@link BroadcastReceiver} implementation will update a {@link DataAdapter}.
 * @author talos
 *
 */
public class DataUpdateReceiver extends BroadcastReceiver {

	private final DataAdapter adapter;
	private String listenToScope;
	
	/**
	 * 
	 * @param adapter The {@link DataAdapter} to update when a broadcast is received.
	 */
	public DataUpdateReceiver(DataAdapter adapter) {
		this.adapter = adapter;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("caustic-update", "received intent");
		CausticResponseIntent refresh = new CausticResponseIntent(intent);
		Log.i("caustic-update", refresh.getChildren().toString());
		if(refresh.getScope().equals(listenToScope)) {
			adapter.setData(refresh.getData(), refresh.getWaits(), refresh.getChildren());
		}
	}
	
	/**
	 * 
	 * @param scope The {@link String} scope to listen to responses for.
	 */
	public void listenTo(String scope) {
		this.listenToScope = scope;
	}
}
