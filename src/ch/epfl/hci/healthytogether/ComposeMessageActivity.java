package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ComposeMessageActivity extends Activity {

	public static final String EXTRA_MESSAGE_TEMPLATE = "message_template";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_message);
		// Show the Up button in the action bar.
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// first check if the user selected a template
		String template = getIntent().getStringExtra(EXTRA_MESSAGE_TEMPLATE);
		if (template != null) {
			Log.d(ComposeMessageActivity.class.toString(),
					"Prefilling template: " + template);
			EditText text = (EditText) findViewById(R.id.editTextMessage);
			text.setText(template);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_compose_message, menu);
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

	public void onSendButtonClicked(View v) {
		// For demonstration purposes we simply show a toast here.
		Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
		setResult(Activity.RESULT_OK);
		finish();
	}

}
