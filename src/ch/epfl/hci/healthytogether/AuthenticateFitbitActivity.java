package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckFitbitAuthenticationTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveStepCountTask;
import ch.epfl.hci.healthytogether.util.Utils;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class AuthenticateFitbitActivity extends Activity {

	public static final String TAG = AuthenticateFitbitActivity.class
			.getSimpleName();
	private boolean isActive = false;
	private ProgressDialog initialSyncDialog;

	// override default behaviour of the browser
	// private class MyWebViewClient extends WebViewClient {
	// @Override
	// public boolean shouldOverrideUrlLoading(WebView view, String url) {
	// view.loadUrl(url);
	// return true;
	// }
	// }

	private boolean mAuthorizationStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isActive = true;
		setContentView(R.layout.activity_authenticate_fitbit);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Show the Up button in the action bar.
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		// int uid = AppContext.getInstance().getUserId();
		// webView = (WebView) findViewById(R.id.authorizeView);
		// webView.setWebViewClient(new MyWebViewClient());
		// webView.getSettings().setJavaScriptEnabled(true);
		// webView.loadUrl(".php?uid="+uid);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;

		if (initialSyncDialog != null) {
			initialSyncDialog.dismiss();
		}

		showProgressDialog(true);
		// start progress bar: "Checking Fitbit Authentication..."

		CheckFitbitAuthenticationTask task = new CheckFitbitAuthenticationTask(
				AppContext.getInstance().getUserId()) {

			@Override
			protected void onPostExecute(Boolean success) {
				
				if(this.safeExecution)
				{
					if (success) {
						showProgressDialog(false);
						onAuthorizeCompleteButtonClicked(null);
					}
				}
				showProgressDialog(false);

			}
		};

		task.execute();

		/*
		 * if (mAuthorizationStarted) { // the user came back from the fitbit
		 * website and has (hopefully) performed the authorization
		 * onAuthorizeCompleteButtonClicked(null); }
		 */
	}

	protected void onDestroy() {
		super.onDestroy();
		isActive = false;
	}

	protected void onPause() {
		super.onPause();
		isActive = false;
	}

	private void showProgressDialog(final boolean show) {
		if (show) {
			if (isActive) {
				initialSyncDialog = ProgressDialog.show(this,
						getResources().getString(R.string.auth_control_dialog_title),
						getResources().getString(R.string.auth_control_dialog_content));
			}
		} else if (initialSyncDialog != null) {
			initialSyncDialog.dismiss();
			initialSyncDialog = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_authenticate_fitbit, menu);
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
		case R.id.menu_settings:
			Intent i_settings = new Intent(AuthenticateFitbitActivity.this,
					SettingsActivity.class);
			startActivity(i_settings);
			return true;
	 	case R.id.menu_settings_logout:
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
	 		return Utils.logout(this,prefs2);	
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Open the fitbit authentication website in the android browser
	 */
	public void onOpenFitbiWebsiteButtonClicked(View v) {
		int uid = AppContext.getInstance().getUserId();

		String fitbitUrl = "http://my/fitbit/url/synch4.php?uid="
				+ uid;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbitUrl));
		Log.d(TAG, "Authorizing on fitbit website: " + fitbitUrl);
		startActivity(i);
		mAuthorizationStarted = true;
	}

	/**
	 * User has performed fitbit authentication. We update our preferences.
	 */
	public void onAuthorizeCompleteButtonClicked(View v) {
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putBoolean(Constants.PROP_KEY_AUTHORIZED, true);
		edit.commit();
		setResult(Activity.RESULT_OK);
		Log.d(TAG, "authorization succeeded.");

		finish();
	}

	public void onDebugButtonClicked(View v) {
		RetrieveStepCountTask task = new RetrieveStepCountTask(AppContext
				.getInstance().getUserId(), 0) {

			@Override
			protected void onPostExecute(Integer stepCount) {
				
				if(!this.safeExecution)
				{
					Toast.makeText(AuthenticateFitbitActivity.this, getResources().getString(R.string.connection_toast_message), Toast.LENGTH_LONG).show();
					return;
				}
				Toast.makeText(AuthenticateFitbitActivity.this,
						"step count: " + stepCount, Toast.LENGTH_SHORT).show();
			}
		};
		task.execute();
	}

}
