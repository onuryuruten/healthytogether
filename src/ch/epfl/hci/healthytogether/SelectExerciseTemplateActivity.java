package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author Onur Yuruten
 * 
 *         TODO List: - Check XML/parent bindings - Check rooms for
 *         modification.
 */
public class SelectExerciseTemplateActivity extends Activity {

	private static final int REQUEST_CODE_COMPOSE_MESSAGE = 1;

	private ListView mListView;
	private ArrayAdapter<String> mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_cheer_template);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Show the Up button in the action bar.
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		// Find the ListView resource.
		mListView = (ListView) findViewById(R.id.mainListView);

		// Create and populate a List of cheer templates.
		final String[] templates = new String[] { "Jogging", "Swimming",
				"Weight Lifting", "Cycling", "Skiing", "Working", "Rest" };

		// Create ArrayAdapter using the list.
		mListAdapter = new ArrayAdapter<String>(this,
				R.layout.simple_list_item, templates);

		// Set the ArrayAdapter as the ListView's adapter.
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String selected = (String) mListView
						.getItemAtPosition(position);
				Log.d(SelectCheerTemplateActivity.class.toString(),
						"Selected template: " + selected);

				// TODO: here, I should apply the change on the icon
				Intent i = new Intent(SelectExerciseTemplateActivity.this,
						ComposeMessageActivity.class);
				i.putExtra(ComposeMessageActivity.EXTRA_MESSAGE_TEMPLATE,
						selected);
				startActivityForResult(i, REQUEST_CODE_COMPOSE_MESSAGE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_COMPOSE_MESSAGE
				&& resultCode == Activity.RESULT_OK) {
			// the message has been sent successfully and we return to the main
			// activity
			finish();
		} else {
			// default behavior (user navigated back, message not sent)
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_select_cheer_template, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
