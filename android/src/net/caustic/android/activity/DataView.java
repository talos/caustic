package net.caustic.android.activity;

import net.caustic.android.R;
import net.caustic.android.service.CausticServiceIntent.CausticRefreshIntent;
import net.caustic.android.service.CausticServiceIntent.CausticResponseIntent;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class DataView {

	private final DataAdapter adapter;
	
	private final ListView dataList;
	private final View loadingView;
	private final TextView dataHeader;
	
	private String curScope;
	
	/**
	 * Inflate and attach a new view to Caustic data.
	 * @param receiver The {@link DataUpdateReceiver} that will be watched for data updates.
	 * This receiver must be registered and unregistered elsewhere.
	 * @param parent The {@link ViewGroup} parent of the Caustic data view.  Must not be null.
	 */
	public DataView(DataUpdateReceiver receiver, ViewGroup parent) {
		receiver.registerView(this);
		
		this.adapter = new DataAdapter();
		
		View view = View.inflate(parent.getContext(), R.layout.data_view, parent);
		dataHeader = (TextView) View.inflate(parent.getContext(), R.layout.data_header, null);
		
		dataList = (ListView) view.findViewById(R.id.data_list);
		dataList.addHeaderView(dataHeader);
		dataList.setAdapter(adapter);
		loadingView = view.findViewById(R.id.loading);		
	}
	
	/**
	 * Update the {@link DataView} for a <code>scope</code>.  If the <code>scope</code>
	 * is different than the prior one, a waiting display will be shown.
	 * @param context The {@link Context} to start the refresh service on.
	 * @param scope The {@link String} scope to refresh.
	 */
	public void update(Context context, String title, String scope) {
		loadingView.setVisibility(View.VISIBLE);
		if(scope.equals(curScope)) {
			
		} else {
			this.curScope = scope;
			dataList.setVisibility(View.GONE);
		}
		dataHeader.setText(title);
		context.startService(CausticRefreshIntent.newRefresh(scope));
	}
	
	final void receiveResponse(CausticResponseIntent response) {
		if(response.getScope().equals(curScope)) {
			adapter.setData(response.getData(), response.getWaits(), response.getChildren());
			dataList.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
