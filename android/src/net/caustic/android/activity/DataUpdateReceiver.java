package net.caustic.android.activity;

import java.util.HashSet;
import java.util.Set;

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

	private final Set<DataView> dataViews = new HashSet<DataView>();
	//private String listenToScope;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		CausticResponseIntent response = new CausticResponseIntent(intent);
		for(DataView view : dataViews) {
			view.receiveResponse(response);
		}
	}
	
	public void registerView(DataView view) {
		this.dataViews.add(view);
	}
	
	public void unregisterView(DataView view) {
		this.dataViews.remove(view);
	}
}
