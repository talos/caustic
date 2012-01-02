package net.caustic.android.activity;

import net.caustic.android.service.CausticIntent;
import net.caustic.android.service.CausticIntent.CausticRefreshIntent;
import net.caustic.android.service.CausticIntent.CausticRequestIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class DataUpdateReceiver extends BroadcastReceiver {

	private final DataAdapter adapter;
	private String listenToScope;
	
	DataUpdateReceiver(DataAdapter adapter) {
		this.adapter = adapter;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(CausticIntent.REFRESH_DATA_INTENT)) {
			CausticRefreshIntent refresh = new CausticRefreshIntent(intent);
			if(refresh.getScope().equals(listenToScope)) {
				adapter.setData(refresh.getData(), refresh.getWaits(), refresh.getChildren());
			}
		}
	}
}
