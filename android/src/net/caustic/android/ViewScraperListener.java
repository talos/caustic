package net.caustic.android;

import java.util.HashMap;
import java.util.Map;

import net.caustic.ScraperListener;
import net.caustic.database.DatabaseListenerException;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;
import android.app.Activity;
import android.content.Context;
import android.opengl.Visibility;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Display the results of a scrape in a {@link TableLayout}.
 * @author talos
 *
 */
public class ViewScraperListener {
		
	private final ViewGroup parentView;
	private final Activity activity;
	
	private final Map<Scope, TableLayout> tables = new HashMap<Scope, TableLayout>();
	
	/**
	 * Use the provided table view.
	 * @param activity {@link Activity} to have access to UI thread.
	 * @param parent
	 */
	public ViewScraperListener(Activity activity, ViewGroup parent) {
		this.activity = activity;
		this.parentView = parent;
	}
	
	/**
	 * Add a row with key and value to a table corresponding to scope each time a key/value is found.
	 */
	public void put(final Scope scope, final String key, final String value)
			throws DatabaseListenerException {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				TableLayout table = tables.get(scope);
				table.setVisibility(View.VISIBLE);
				table.addView(newRow(newText(key), newText(value)));
			}
		});
	}

	/**
	 * Adds table directly to {@link #parentView}.
	 */
	public void newScope(final Scope scope) throws DatabaseListenerException {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				TableLayout table = new TableLayout(activity);
				parentView.addView(table);
				tables.put(scope, table);
			}
		});
	}

	/**
	 * Creates child table for key within parent.
	 */
	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		newScope(parent, key, "", child);
	}
	
	public void newScope(final Scope parent, final String key, final String value, final Scope child)
			throws DatabaseListenerException {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				TableLayout parentTable = tables.get(parent);
				TableLayout childTable = new TableLayout(activity);
				childTable.setVisibility(View.INVISIBLE);
				tables.put(child, childTable);
				
				parentTable.addView(newRow(newText(key), newText(value)));
				parentTable.addView(newRow(newText(""), childTable));
			}
		});
	}

	/**
	 * Create a new {@link TableRow} with the specified columns.
	 * @param columns
	 * @return the {@link TableRow}.
	 */
	private TableRow newRow(View... columns) {
		TableRow row = new TableRow(activity);
		
		for(View view : columns) {
			row.addView(view);
		}
		
		return row;
	}
	
	/**
	 * Create a new {@link TextView} with the specified text.
	 * @param text
	 * @return
	 */
	private TextView newText(String text) {
		TextView tv = new TextView(activity);
		tv.setText(text);
		return tv;
	}
}
